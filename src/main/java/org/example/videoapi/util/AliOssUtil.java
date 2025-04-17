package org.example.videoapi.util;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 原有：通过字节数组上传
     *
     * @param bytes      文件字节
     * @param objectName OSS 对象名称（完整路径）
     * @return 文件访问 URL
     */
    public String upload(byte[] bytes, String objectName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 创建PutObject请求
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder.append(bucketName).append(".").append(endpoint).append("/").append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());
        return stringBuilder.toString();
    }

    /**
     * 新增：MultipartFile 上传，自动拼接日期路径和 UUID
     *
     * @param file MultipartFile 文件
     * @param dir  存储目录前缀，如 "avatars/" 或 "videos/"
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file, String dir) {
        try {
            // 当前日期路径
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 原始文件名和后缀
            String original = file.getOriginalFilename();
            String suffix = (original != null && original.contains(".")) ?
                    original.substring(original.lastIndexOf('.')) : "";

            // 拼接完整 OSS 对象名称
            String objectName = dir + datePath + "/" + UUID.randomUUID() + suffix;

            // 上传并返回 URL
            return upload(file.getBytes(), objectName);
        } catch (Exception e) {
            log.error("上传文件失败：{}", e.getMessage(), e);
            throw new RuntimeException("上传文件失败", e);
        }
    }
    /**
     * 删除 OSS 上的单个对象
     *
     * @param objectName OSS 对象名称（即不含 https://.../ 前缀的那部分）
     */
    public void deleteFile(String objectName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.deleteObject(bucketName, objectName);
            log.info("已删除 OSS 对象：{}", objectName);
        } catch (Exception e) {
            log.error("删除 OSS 对象失败：{}", objectName, e);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 从完整 URL 中提取 OSS 对象名称
     */
    public String extractObjectName(String url) {
        int idx = url.indexOf('/', url.indexOf("://") + 3);
        return idx != -1 ? url.substring(idx + 1) : url;
    }
}
