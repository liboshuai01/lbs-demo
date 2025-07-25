package com.liboshuai.demo.service.impl;

import com.liboshuai.demo.service.ConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ConversionServiceImpl implements ConversionService {

    // --- 日期格式化器 ---
    // 用于 wiki.js 输出格式: 2025-07-25T'T'HH:mm:ss.SSS'Z'
    private static final DateTimeFormatter WIKI_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    // 用于解析 "Thu Dec 08 07:43:18 CST 2022" 格式
    private static final DateTimeFormatter GIT_COMMIT_DATE_FORMATTER = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

    // 用于解析 "2024-01-11 13:27:55" 格式
    private static final DateTimeFormatter HEXO_SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public long processDirectory(String sourceDirectoryPath, String targetDirectoryPath) throws IOException {
        // 此方法无需修改
        Path sourceRoot = Paths.get(sourceDirectoryPath);
        Path targetRoot = Paths.get(targetDirectoryPath);

        if (!Files.isDirectory(sourceRoot)) {
            throw new IllegalArgumentException("提供的源路径不是一个有效的目录: " + sourceDirectoryPath);
        }

        Files.createDirectories(targetRoot);
        log.info("目标目录已确保存在: {}", targetRoot);

        try (Stream<Path> stream = Files.walk(sourceRoot)) {
            List<Path> markdownFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".md"))
                    .collect(Collectors.toList());

            log.info("找到 {} 个 Markdown 文件待处理。", markdownFiles.size());

            long count = 0;
            for (Path sourceFile : markdownFiles) {
                try {
                    if (convertAndSaveFile(sourceFile, sourceRoot, targetRoot)) {
                        count++;
                    }
                } catch (Exception e) {
                    log.error("文件转换失败: {}", sourceFile, e);
                }
            }
            return count;
        }
    }

    private boolean convertAndSaveFile(Path sourceFile, Path sourceRoot, Path targetRoot) throws IOException {
        log.debug("正在处理源文件: {}", sourceFile);

        // --- 1. 读取和解析源文件 (未修改) ---
        List<String> lines = Files.readAllLines(sourceFile, StandardCharsets.UTF_8);

        if (lines.size() < 2 || !lines.get(0).trim().equals("---")) {
            log.warn("跳过文件，因为它似乎没有有效的前置元数据块: {}", sourceFile);
            return false;
        }
        int secondDashIndex = -1;
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).trim().equals("---")) {
                secondDashIndex = i;
                break;
            }
        }
        if (secondDashIndex == -1) {
            log.warn("跳过文件，因为它缺少前置元数据的结束 '---': {}", sourceFile);
            return false;
        }

        String hexoYamlContent = String.join("\n", lines.subList(1, secondDashIndex));
        String bodyContent = String.join("\n", lines.subList(secondDashIndex + 1, lines.size()));

        Yaml yaml = new Yaml();
        Map<String, Object> hexoData;
        try {
            hexoData = yaml.load(hexoYamlContent);
            if (hexoData == null) {
                log.warn("跳过文件，前置元数据为空: {}", sourceFile);
                return false;
            }
        } catch (Exception e) {
            log.error("解析文件的前置 YAML 元数据失败: {}. 错误: {}", sourceFile, e.getMessage());
            return false;
        }

        // --- 2. 应用转换规则 (主要修改部分) ---
        String title = (String) hexoData.get("title");
        if (title == null || title.trim().isEmpty()) {
            log.warn("跳过文件，缺少标题: {}", sourceFile);
            return false;
        }

        // ==================== 新增修改点 1: 获取和校验 abbrlink ====================
        String abbrlink = (String) hexoData.get("abbrlink");
        if (abbrlink == null || abbrlink.trim().isEmpty()) {
            log.warn("跳过文件，缺少或空的 'abbrlink' 用于生成文件名: {}", sourceFile);
            return false;
        }
        // ==================== 修改结束 ====================

        Map<String, Object> wikiData = new LinkedHashMap<>();
        wikiData.put("title", title);
        wikiData.put("description", title);
        wikiData.put("published", true);

        // 日期处理逻辑 (未修改)
        Object hexoDateObj = hexoData.get("date");
        if (hexoDateObj != null) {
            ZonedDateTime zonedDateTime = null;
            if (hexoDateObj instanceof Date) {
                log.debug("文件 {} 中的日期是 java.util.Date 类型，直接转换。", sourceFile.getFileName());
                zonedDateTime = ((Date) hexoDateObj).toInstant().atZone(ZoneId.of("UTC"));
            } else if (hexoDateObj instanceof String) {
                String dateString = (String) hexoDateObj;
                log.debug("文件 {} 中的日期是字符串: '{}'. 尝试解析。", sourceFile.getFileName(), dateString);
                try {
                    zonedDateTime = ZonedDateTime.parse(dateString, GIT_COMMIT_DATE_FORMATTER);
                } catch (DateTimeParseException e1) {
                    try {
                        LocalDateTime localDateTime = LocalDateTime.parse(dateString, HEXO_SIMPLE_DATE_FORMATTER);
                        zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
                    } catch (DateTimeParseException e2) {
                        log.warn("无法使用任何已知格式解析文件 {} 中的日期字符串 '{}'。跳过日期转换。", dateString, sourceFile);
                    }
                }
            }

            if (zonedDateTime != null) {
                String wikiDateString = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).format(WIKI_DATE_FORMATTER);
                wikiData.put("date", wikiDateString);
                wikiData.put("dateCreated", wikiDateString);
                log.debug("成功转换文件 {} 的日期。", sourceFile.getFileName());
            }
        }

        Object categoriesObj = hexoData.get("categories");
        if (categoriesObj instanceof List && !((List<?>) categoriesObj).isEmpty()) {
            wikiData.put("tags", ((List<?>) categoriesObj).get(0).toString());
        }

        wikiData.put("editor", "markdown");

        // --- 3. 生成新内容并写入目标文件 (主要修改部分) ---
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        String wikiYamlContent = new Yaml(options).dump(wikiData);
        String newFileContent = "---\n" + wikiYamlContent + "---\n" + bodyContent;


        // ==================== 新增修改点 2: 使用 abbrlink 构建目标文件路径 ====================
        // 获取源文件相对于源根目录的路径，以便找到其父目录
        Path relativeSourcePath = sourceRoot.relativize(sourceFile);
        // 获取相对路径中的父目录（如果存在）
        Path parentDir = relativeSourcePath.getParent();

        // 构建新的文件名
        String newFileName = abbrlink + ".md";

        // 构建最终的目标文件路径
        Path targetFile;
        if (parentDir != null) {
            // 如果原始文件在子目录中，则在目标根目录中创建相同的子目录结构
            targetFile = targetRoot.resolve(parentDir).resolve(newFileName);
        } else {
            // 如果原始文件直接在源根目录中，则新文件也直接放在目标根目录中
            targetFile = targetRoot.resolve(newFileName);
        }
        // ==================== 修改结束 ====================

        Files.createDirectories(targetFile.getParent());
        Files.write(targetFile, newFileContent.getBytes(StandardCharsets.UTF_8));
        log.info("成功转换 [{}] -> [{}]", sourceFile, targetFile);
        return true;
    }
}