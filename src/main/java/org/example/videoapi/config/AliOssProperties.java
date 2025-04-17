package org.example.videoapi.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里oss配置类,用于读取配置文件的配置
 */
@Component
@Data
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliOssProperties {
    public String endpoint;
    public String accesskeyid;
    public String accesskeysecret;
    public String bucketName;
    private String avatarDir;
    private String videoDir;
    private String chatDir;
}