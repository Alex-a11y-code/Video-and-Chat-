package org.example.videoapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.BloomFilter;
import org.example.videoapi.mapper.UserMapper;
import org.example.videoapi.mapper.VideoMapper;
import org.example.videoapi.pojo.entity.Video;
import org.example.videoapi.pojo.vo.VideoVO;
import org.example.videoapi.service.VideoService;
import org.example.videoapi.util.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements VideoService {
    private static final String REDIS_KEY_VIEWS = "video:views";
    private static final String REDIS_KEY_SEARCH_HISTORY = "search:history";
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private BloomFilter<String> bloomFilter;
    @Override
    public void saveVideo(Video video) {
        if (video.getCategory() == null) {
            video.setCategory("默认分类");
        }
        if (video.getStatus() == null) {
            video.setStatus("PENDING");
        }
        video.setViewsCount(Optional.ofNullable(video.getViewsCount()).orElse(0L));
        video.setLikesCount(Optional.ofNullable(video.getLikesCount()).orElse(0L));
        if (video.getCreateTime() == null) {
            video.setCreateTime(LocalDateTime.now());
        }
        videoMapper.saveVideo(video);

    }

    @Override
    public void removeVideoById(Long videoId) {

        videoMapper.removeVideoById(videoId);
    }

    @Override
    public VideoVO findVideoById(Long videoId) {
        String idStr = videoId.toString();
        Video video = videoMapper.findVideoById(videoId);
        if (video == null) {
            return null;
        }
        VideoVO vo = new VideoVO();
        vo.setVideoId(video.getVideoId());
        vo.setUserId(video.getUserId());
        vo.setTitle(video.getTitle());
        vo.setIntroduction(video.getIntroduction());
        vo.setViewsCount(video.getViewsCount());
        vo.setLikesCount(video.getLikesCount());
        vo.setSurfacePicture(video.getSurfacePicture());
        vo.setVideoAddress(video.getVideoAddress());
        String username = userMapper.findUsernameById(video.getUserId());
        vo.setUsername(username);
        return vo;
    }

    @Override
    public List<VideoVO> getTopVideos(int limit) {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        // 取出 top N 的 videoId（按 score 降序）
        Set<Object> ids = zset.reverseRange(REDIS_KEY_VIEWS, 0, limit - 1);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        // 转成 Long 列表
        List<Long> videoIds = ids.stream()
                .map(Object::toString)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        List<VideoVO> list = videoIds.stream()
                .map(id -> {
                    String idStr = id.toString();
                    if (!bloomFilter.mightContain(idStr)) {
                        return null;
                    }
                    Video v = videoMapper.findVideoById(id);
                    if (v == null) return null;
                    VideoVO vo = new VideoVO();

                    vo.setVideoId(v.getVideoId());
                    vo.setUserId(v.getUserId());
                    vo.setTitle(v.getTitle());
                    vo.setIntroduction(v.getIntroduction());
                    vo.setViewsCount(v.getViewsCount());
                    vo.setLikesCount(v.getLikesCount());
                    vo.setSurfacePicture(v.getSurfacePicture());
                    vo.setVideoAddress(v.getVideoAddress());
                    vo.setUsername(userMapper.findUsernameById(v.getUserId()));
                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return list;
    }

    @Override
    public List<VideoVO> searchVideos(Map<String, Object> params) throws JsonProcessingException {
        // 1. 记录搜索历史
        String json = new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(params);
        redisTemplate.opsForList().leftPush(REDIS_KEY_SEARCH_HISTORY, json);
        redisTemplate.opsForList().trim(REDIS_KEY_SEARCH_HISTORY, 0, 99);//限制搜索长度

        List<Video> videos = videoMapper.searchVideos(params);


        return videos.stream().map(v -> {
            VideoVO vo = new VideoVO();
            vo.setVideoId(v.getVideoId());
            vo.setUserId(v.getUserId());
            vo.setTitle(v.getTitle());
            vo.setIntroduction(v.getIntroduction());
            vo.setViewsCount(v.getViewsCount());
            vo.setLikesCount(v.getLikesCount());
            vo.setSurfacePicture(v.getSurfacePicture());
            vo.setVideoAddress(v.getVideoAddress());
            vo.setUsername(userMapper.findUsernameById(v.getUserId()));
            return vo;
        }).collect(Collectors.toList());
    }
    @Override
    public List<Map<String, Object>> getSearchHistory(int limit) {
        List<Object> rawList = redisTemplate
                .opsForList()
                .range(REDIS_KEY_SEARCH_HISTORY, 0, limit - 1);
        if (rawList == null) return Collections.emptyList();

        ObjectMapper mapper = new ObjectMapper();
        return rawList.stream()
                .map(obj -> {
                    try {
                        // 用 TypeReference 保留泛型
                        return mapper.readValue(
                                obj.toString(),
                                new TypeReference<Map<String, Object>>() {}
                        );
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    @Override
    public void incrementViewsCount(Long videoId) {
        // 增加 Redis 中对应视频的点击量
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        // 给视频点击量增加 1，score 即为点击量
        zset.incrementScore(REDIS_KEY_VIEWS, videoId, 1);
        // 同时更新数据库中的点击量
        videoMapper.incrementViewsCount(videoId);
    }
    @Override
    public List<VideoVO> listPendingVideos() {
        List<Video> videos = videoMapper.findPendingVideos();
        return videos.stream().map(v -> {
            VideoVO vo = new VideoVO();
            vo.setVideoId(v.getVideoId());
            vo.setUserId(v.getUserId());
            vo.setTitle(v.getTitle());
            vo.setUsername(userMapper.findUsernameById(v.getUserId()));
            vo.setIntroduction(v.getIntroduction());
            vo.setViewsCount(v.getViewsCount());
            vo.setLikesCount(v.getLikesCount());
            vo.setSurfacePicture(v.getSurfacePicture());
            vo.setVideoAddress(v.getVideoAddress());
            vo.setStatus(v.getStatus());
            vo.setReviewerId(v.getReviewerId());
            vo.setReviewTime(v.getReviewTime());
            return vo;
        }).collect(Collectors.toList());
    }
    @Override
    public void reviewVideo(Long videoId, boolean approve, Long reviewerId) {
        String status = approve ? "APPROVED" : "REJECTED";
        // 1. 更新审核状态
        videoMapper.updateStatus(videoId, status, reviewerId);

        // 2. 如果审核不通过，删除 OSS 上的文件
        if (!approve) {
            Video v = videoMapper.findVideoById(videoId);
            if (v != null) {
                // 提取并删除封面
                String coverObject = aliOssUtil.extractObjectName(v.getSurfacePicture());
                aliOssUtil.deleteFile(coverObject);
                // 提取并删除视频文件
                String videoObject = aliOssUtil.extractObjectName(v.getVideoAddress());
                aliOssUtil.deleteFile(videoObject);
            }
        }
    }





}
