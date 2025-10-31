package com.liboshuai.spi.search;

import com.liboshuai.spi.search.api.SearchExecutor;
import com.liboshuai.spi.search.api.SearchExecutorFactory;
import com.liboshuai.spi.search.api.SearchExecutorServiceLoader;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        testDatabase();

        System.out.println("\n======================================================\n");

        testFile();
    }

    private static void testDatabase() {
        SearchExecutorFactory searchExecutorFactory = SearchExecutorServiceLoader.getExecutorFactory("database");
        SearchExecutor searchExecutor = searchExecutorFactory.getExecutor();
        List<String> results = searchExecutor.executor("短视频平台");
        System.out.println("数据库搜索客户端执行的结果：" + results);
    }

    private static void testFile() {
        SearchExecutorFactory searchExecutorFactory = SearchExecutorServiceLoader.getExecutorFactory("file");
        SearchExecutor searchExecutor = searchExecutorFactory.getExecutor();
        List<String> results = searchExecutor.executor("项目文件");
        System.out.println("文件搜索客户端执行的结果：" + results);
    }
}
