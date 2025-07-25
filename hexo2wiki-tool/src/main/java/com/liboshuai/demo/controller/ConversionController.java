package com.liboshuai.demo.controller;

import com.liboshuai.demo.service.ConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/convert")
@RequiredArgsConstructor
public class ConversionController {

    private final ConversionService conversionService;

    /**
     * 执行Hexo到Wiki.js格式的转换
     * @param sourcePath 包含Hexo markdown文件的源目录路径
     * @param targetPath 转换后文件输出的目标目录路径
     * @return 包含处理结果的响应
     */
    @PostMapping("/hexo-to-wiki")
    public ResponseEntity<Map<String, Object>> convertHexoToWiki(
            @RequestParam("sourcePath") String sourcePath,
            @RequestParam("targetPath") String targetPath) {

        log.info("收到转换请求。源路径：[{}], 目标路径：[{}]", sourcePath, targetPath);
        try {
            long processedCount = conversionService.processDirectory(sourcePath, targetPath);
            String message = String.format(
                    "成功将 %d 个文件从 '%s' 转换为 '%s'",
                    processedCount, sourcePath, targetPath
            );
            log.info(message);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("processedFiles", processedCount);
            response.put("message", message);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("转换失败。源路径：[{}], 目标路径：[{}]", sourcePath, targetPath, e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}