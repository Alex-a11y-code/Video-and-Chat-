package org.example.videoapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.videoapi.config.AliOssProperties;
import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.pojo.entity.User;
import org.example.videoapi.pojo.entity.Video;
import org.example.videoapi.pojo.vo.VideoVO;
import org.example.videoapi.service.VideoService;
import org.example.videoapi.util.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoController {
    @Autowired
    private VideoService videoService;
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private AliOssProperties aliOssProperties;

    /**
     * 上传视频及封面，并保存 Video 记录
     */
    @PostMapping(value = "/upload/{userId}", consumes = "multipart/form-data")
    public ResultResponse<String> uploadVideo(
            @PathVariable Long userId,
            @RequestParam("videoFile") MultipartFile videoFile,
            @RequestParam("coverFile") MultipartFile coverFile,
            @RequestParam("title") String title,
            @RequestParam("introduction") String introduction) {
        // 1. 上传封面图到 OSS
        String coverUrl = aliOssUtil.uploadFile(
                coverFile, aliOssProperties.getVideoDir() + "cover/");
        // 2. 上传视频到 OSS
        String videoUrl = aliOssUtil.uploadFile(
                videoFile, aliOssProperties.getVideoDir() + "videos/");
        // 3. 构造 Video 对象并保存
        Video video = new Video();
        video.setUserId(userId);
        video.setTitle(title);
        video.setIntroduction(introduction);
        video.setSurfacePicture(coverUrl);
        video.setVideoAddress(videoUrl);
        video.setViewsCount(0L);
        video.setLikesCount(0L);
        videoService.saveVideo(video);
        return ResultResponse.success("上传视频成功", videoUrl);
    }

    @PostMapping("/delete/{videoId}")
    public ResultResponse<String> deleteVideo(@PathVariable Long videoId) {
        videoService.removeVideoById(videoId);
        return ResultResponse.success("删除视频成功", null);
    }

    @GetMapping("/detail/{videoId}")
    public ResultResponse<VideoVO> getVideoDetail(@PathVariable Long videoId) {
        VideoVO videoVO = videoService.findVideoById(videoId);
        if (videoVO == null) {
            return ResultResponse.error(400, "视频不存在");
        }
        return ResultResponse.success("获取成功", videoVO);
    }
    /** 点击量排行榜 */
    @GetMapping("/leaderboard")
    public ResultResponse<List<VideoVO>> leaderboard(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<VideoVO> top = videoService.getTopVideos(limit);
        return ResultResponse.success("排行榜获取成功", top);
    }

    /**
     * 多条件搜索视频
     * 支持：year, category, startTime, endTime, keyword, sortBy(views/time), order(asc/desc)
     */
    @GetMapping("/search")
    public ResultResponse<List<VideoVO>> search  (
            @RequestParam Map<String, Object> params) throws JsonProcessingException {
        List<VideoVO> results = videoService.searchVideos(params);
        return ResultResponse.success("搜索完成", results);
    }
    @GetMapping("/search/history")
    public ResultResponse<List<Map<String, Object>>> getSearchHistory(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> history = videoService.getSearchHistory(limit);
        return ResultResponse.success("搜索记录获取成功", history);
    }
    @GetMapping("/view/{videoId}")
    public ResultResponse<String> viewVideo(@PathVariable Long videoId) {
        videoService.incrementViewsCount(videoId); // 增加点击量
        VideoVO videoVO = videoService.findVideoById(videoId);
        if (videoVO == null) {
            return ResultResponse.error(400, "视频不存在");
        }
        return ResultResponse.success("视频浏览量查询成功", videoVO.getVideoAddress());
    }
    /** 获取待审核列表（仅 ADMIN） */
    @GetMapping("/review/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultResponse<List<VideoVO>> pending() {
        return ResultResponse.success("待审核视频列表", videoService.listPendingVideos());
    }

    /** 审核视频（approve=true 通过） */
    @PostMapping("/review/{videoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultResponse<String> review(
            @PathVariable Long videoId,
            @RequestParam boolean approve,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        videoService.reviewVideo(videoId, approve, user.getId());
        return ResultResponse.success("审核完成", approve ? "APPROVED" : "REJECTED");
    }

}
