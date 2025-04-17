package org.example.videoapi.service;

public interface CommentLikesService {
    void saveCommentLikes(Long userId, Long videoCommentId);

    void removeCommentLikes(Long userId, Long videoCommentId);
}
