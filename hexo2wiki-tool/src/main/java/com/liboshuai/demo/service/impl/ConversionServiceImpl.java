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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ConversionServiceImpl implements ConversionService {

    private static final DateTimeFormatter HEXO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter WIKI_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * 处理指定目录下的所有Markdown文件，并输出到目标目录
     *
     * @param sourceDirectoryPath 源目录路径
     * @param targetDirectoryPath 目标目录路径
     * @return 处理的文件数量
     * @throws IOException 如果发生IO错误
     */
    public long processDirectory(String sourceDirectoryPath, String targetDirectoryPath) throws IOException {
        Path sourceRoot = Paths.get(sourceDirectoryPath);
        Path targetRoot = Paths.get(targetDirectoryPath);

        if (!Files.isDirectory(sourceRoot)) {
            throw new IllegalArgumentException("提供的源路径不是一个有效的目录: " + sourceDirectoryPath);
        }

        // 自动创建目标目录（如果不存在）
        Files.createDirectories(targetRoot);
        log.info("目标目录已确保存在: {}", targetRoot);

        try (Stream<Path> stream = Files.walk(sourceRoot)) {
            List<Path> markdownFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".md"))
                    .collect(Collectors.toList());

            log.info("找到 {} 个Markdown文件待处理。", markdownFiles.size());

            long count = 0;
            for (Path sourceFile : markdownFiles) {
                try {
                    if (convertAndSaveFile(sourceFile, sourceRoot, targetRoot)) {
                        count++;
                    }
                } catch (Exception e) {
                    log.error("转换文件失败: {}", sourceFile, e);
                }
            }
            return count;
        }
    }

    /**
     * 转换单个文件并保存到目标位置
     *
     * @param sourceFile 源文件路径
     * @param sourceRoot 源根目录
     * @param targetRoot 目标根目录
     * @return 如果成功转换则返回true
     * @throws IOException 如果发生IO错误
     */
    private boolean convertAndSaveFile(Path sourceFile, Path sourceRoot, Path targetRoot) throws IOException {
        log.debug("正在处理源文件: {}", sourceFile);

        // --- 1. 读取和解析源文件 ---
        List<String> lines = Files.readAllLines(sourceFile, StandardCharsets.UTF_8);

        if (lines.size() < 2 || !lines.get(0).trim().equals("---")) {
            log.warn("跳过文件，因为它似乎没有一个有效的前置元数据块: {}", sourceFile);
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
                log.warn("跳过具有空前置元数据的文件: {}", sourceFile);
                return false;
            }
        } catch (Exception e) {
            log.error("解析文件前置元数据失败: {}. 错误: {}", sourceFile, e.getMessage());
            return false;
        }

        // --- 2. 应用转换规则 ---
        String title = (String) hexoData.get("title");
        if (title == null || title.trim().isEmpty()) {
            log.warn("由于缺少标题，跳过文件: {}", sourceFile);
            return false;
        }

        Map<String, Object> wikiData = new LinkedHashMap<>();
        wikiData.put("title", title);
        wikiData.put("description", title);
        wikiData.put("published", true);

        Object hexoDateObj = hexoData.get("date");
        if (hexoDateObj != null) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(hexoDateObj.toString(), HEXO_DATE_FORMATTER);
                ZonedDateTime utcDateTime = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
                String wikiDateString = utcDateTime.format(WIKI_DATE_FORMATTER);
                wikiData.put("date", wikiDateString);
                wikiData.put("dateCreated", wikiDateString);
            } catch (DateTimeParseException e) {
                log.warn("无法解析文件 {} 中的日期 '{}'。跳过日期转换。", hexoDateObj, sourceFile);
            }
        }

        Object categoriesObj = hexoData.get("categories");
        if (categoriesObj instanceof List && !((List<?>) categoriesObj).isEmpty()) {
            wikiData.put("tags", ((List<?>) categoriesObj).get(0).toString());
        }

        wikiData.put("editor", "markdown");

        // --- 3. 生成新内容并写入目标文件 ---
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        String wikiYamlContent = new Yaml(options).dump(wikiData);
        String newFileContent = "---\n" + wikiYamlContent + "---\n" + bodyContent;

        // 计算目标文件路径，保持目录结构
        Path relativePath = sourceRoot.relativize(sourceFile);
        Path targetFile = targetRoot.resolve(relativePath);

        // 确保目标文件的父目录存在
        Files.createDirectories(targetFile.getParent());

        // 写入新文件到目标路径
        Files.write(targetFile, newFileContent.getBytes(StandardCharsets.UTF_8));
        log.info("成功转换 [{}] -> [{}]", sourceFile, targetFile);
        return true;
    }
}