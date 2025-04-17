package org.example.videoapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.videoapi.pojo.entity.Video;
import org.example.videoapi.pojo.vo.VideoVO;

import java.util.List;
import java.util.Map;

public interface VideoService {
    void saveVideo(Video video);
    void removeVideoById(Long videoId);
    VideoVO findVideoById(Long videoId);
    List<VideoVO> getTopVideos(int limit);
    List<VideoVO> searchVideos(Map<String, Object> params) throws JsonProcessingException;

    List<Map<String, Object>> getSearchHistory(int limit);
    void incrementViewsCount(Long videoId) ;

    List<VideoVO> listPendingVideos();
    void reviewVideo(Long videoId, boolean approve, Long reviewerId);



}
