package org.example.videoapi.service;

public interface BlockService {

    void block(Long userId, Long targetId);

    void unblock(Long userId, Long targetId);

    boolean isBlocked(Long userId, Long targetId);
}