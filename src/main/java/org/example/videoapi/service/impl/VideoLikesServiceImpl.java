package org.example.videoapi.service.impl;

import org.example.videoapi.exception.ApiException;
import org.example.videoapi.mapper.VideoLikesMapper;
import org.example.videoapi.mapper.VideoMapper;
import org.example.videoapi.service.VideoLikesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoLikesServiceImpl implements VideoLikesService {
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private VideoLikesMapper videoLikesMapper;
    @Override
    public void saveVideoLikes(Long userId, Long videoId) {
        if (!videoLikesMapper.isLiked(userId, videoId)) {
            videoLikesMapper.saveVideoLikes(userId, videoId);
            videoMapper.incrementLikesCount(videoId);
        } else {
            throw new ApiException("您已经点赞过了", 400);
        }
    }

    @Override
    public void removeVideoLikes(Long userId, Long videoId) {
        if (videoLikesMapper.isLiked(userId, videoId)) {
            videoLikesMapper.removeVideoLikes(userId, videoId);
            videoMapper.decrementLikesCount(videoId);
        } else {
            throw new ApiException("您没有点赞过", 400);
        }
    }


}
