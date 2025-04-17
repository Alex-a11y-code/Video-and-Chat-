package org.example.videoapi.config;


import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import com.github.houbb.sensitive.word.support.resultcondition.WordResultConditions;
import com.github.houbb.sensitive.word.support.tag.WordTags;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Configuration
public class SensitiveWordConfig {
    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        SensitiveWordBs wordBs = SensitiveWordBs.newInstance()
                .ignoreCase(true)           // 忽略大小写
                .ignoreWidth(true)          // 忽略半角圆角
                .ignoreNumStyle(true)       // 忽略数字的写法
                .ignoreChineseStyle(true)   // 忽略中文的书写格式
                .ignoreEnglishStyle(true)   // 忽略英文的书写格式
                .ignoreRepeat(false)        // 忽略重复词
                .enableNumCheck(true)      // 是否启用数字检测
                .enableEmailCheck(false)    // 是有启用邮箱检测
                .enableUrlCheck(false)      // 是否启用链接检测
                .enableIpv4Check(false)     // 是否启用IPv4检测
                .enableWordCheck(true)      // 是否启用敏感单词检测
                .numCheckLen(8)             // 数字检测，自定义指定长度
                .wordTag(WordTags.none())   // 词对应的标签，
                .charIgnore(SensitiveWordCharIgnores.defaults())            // 忽略的字符，
                .wordResultCondition(WordResultConditions.alwaysTrue())     // 针对匹配的敏感词额外加工，比如可以限制英文单词必须全匹配，默认为真
                .init();
        try {
            Resource resource = new ClassPathResource("sensitive_words.txt");
            List<String> words = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);

            System.out.println("成功加载 " + words.size() + " 个敏感词");
        } catch (IOException e) {
            e.getMessage();
        }

        return wordBs;
    }




}
