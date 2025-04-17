package org.example.videoapi.config;

import lombok.extern.slf4j.Slf4j;
import org.example.videoapi.util.AliOssUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssCreateConfiguration {

    /**
     * 创建AliOssUtil对象
     */
    @Bean
    //保证只有一个aliOssUtilCreate的bean对象
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtilCreate(AliOssProperties aliOssProperties){
        log.info("开始创建阿里云文件上传工具类对象：{}",aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccesskeyid(),
                aliOssProperties.getAccesskeysecret(),
                aliOssProperties.getBucketName());

    }
}