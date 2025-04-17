package org.example.videoapi.service;

public interface VideoLikesService {
    void saveVideoLikes(Long userId, Long videoId);
    void removeVideoLikes(Long userId, Long videoId);
}
