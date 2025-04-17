package org.example.videoapi.util;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import  java.util.concurrent.TimeUnit;
@Slf4j
public class IpAddressUtil {
    // ip2region.xdb 文件地址常量（本地xdb文件路径）
    public static String XDB_PATH = "D:\\Mine code\\Vedio\\src\\main\\resources\\ip2region.xdb";
    /**
     * 缓存 VectorIndex 索引，对用户ip地址进行转换
     * 注：每个线程需要单独创建一个独立的 Searcher 对象，但是都共享全局变量 vIndex 缓存。
     */
    public static String getCityInfoByVectorIndex(String ip) {
        if (StringUtils.isNotEmpty(ip)) {
            try {
                // 1、从 XDB_PATH 中预先加载 VectorIndex 缓存，并且作为全局变量，后续反复使用。
                byte[] vIndex = Searcher.loadVectorIndexFromFile(XDB_PATH);
                // 2、使用全局的 vIndex 创建带 VectorIndex 缓存的查询对象。
                Searcher searcher = Searcher.newWithVectorIndex(XDB_PATH, vIndex);
                // 3、查询
                long sTime = System.nanoTime();
                String region = searcher.search(ip);
                long cost = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - sTime);
                region = region.replace("|0", "");
                log.info("{地区: {}, IO操作数: {}, 耗时: {} μs}", region, searcher.getIOCount(), cost);
                return region;
            } catch (Exception e) {
                log.error("获取IP地址异常：{} ", e.getMessage());
                throw new RuntimeException("获取IP地址异常");
            }
        }
        return "未知";
    }
}
