package org.example.videoapi.service.impl;

import com.google.common.hash.BloomFilter;
import org.example.videoapi.service.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class BlockServiceImpl implements BlockService {

    private static final String KEY_PREFIX = "block:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private BloomFilter<String> bloomFilter;

    @Override
    public void block(Long userId, Long targetId) {
        String redisKey = KEY_PREFIX + userId;
        String memberKey = userId + ":" + targetId;
        redisTemplate.opsForSet().add(redisKey, targetId.toString());
        bloomFilter.put(memberKey);
    }

    @Override
    public void unblock(Long userId, Long targetId) {
        String redisKey = KEY_PREFIX + userId;
        redisTemplate.opsForSet().remove(redisKey, targetId.toString());
    }

    @Override
    public boolean isBlocked(Long userId, Long targetId) {
        String redisKey = KEY_PREFIX + userId;
        String memberKey = userId + ":" + targetId;
        //布隆过滤器先判断
        if (!bloomFilter.mightContain(memberKey)) {
            return false;
        }
        //再去redis中具体判断
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(redisKey, targetId.toString())
        );
    }
}
