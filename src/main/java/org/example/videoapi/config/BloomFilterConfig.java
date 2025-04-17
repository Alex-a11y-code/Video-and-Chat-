package org.example.videoapi.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.example.videoapi.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.nio.charset.Charset;
import java.util.List;

@Component
public class BloomFilterConfig {

    @Value("${bloom-filter.expected-insertions}")
    private int expectedInsertions;

    @Value("${bloom-filter.fpp}")
    private double falsePositiveProbability;

    @Bean
    public BloomFilter<String> bloomFilter() {
        return BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), expectedInsertions, falsePositiveProbability);
    }

}
