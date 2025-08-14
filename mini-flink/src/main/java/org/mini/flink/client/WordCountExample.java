package org.mini.flink.client;

import org.mini.flink.api.*;
import org.mini.flink.runtime.jobgraph.JobGraph;
import org.mini.flink.runtime.jobgraph.JobVertex;
import org.mini.flink.runtime.jobmanager.JobManager;
import org.mini.flink.runtime.state.Stateful;
import org.mini.flink.runtime.taskmanager.TaskManager;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 一个完整的 WordCount 示例，用于演示和测试 mini-flink。
 */
public class WordCountExample {
    private static final Logger LOG = Logger.getLogger(WordCountExample.class.getName());

    /**
     * Source: 模拟一个不断产生句子的数据源
     */
    public static class SentenceSource implements Source<String> {
        private volatile boolean isRunning = true;
        private static final String[] sentences = {
                "the quick brown fox jumps over the lazy dog",
                "the lazy cat sleeps in the sun",
                "a quick brown rabbit hops away"
        };
        private final Random random = new Random();

        @Override
        public void run(Collector<String> collector) throws Exception {
            while (isRunning) {
                String sentence = sentences[random.nextInt(sentences.length)];
                collector.collect(sentence);
                TimeUnit.MILLISECONDS.sleep(500); // 每秒产生2个句子
            }
        }
    }

    /**
     * Operator: 切分句子为单词
     */
    public static class Splitter implements Operator<String, String> {
        @Override
        public void process(String sentence, Collector<String> collector) {
            for (String word : sentence.toLowerCase().split("\\s+")) {
                collector.collect(word);
            }
        }
    }

    /**
     * Operator: 单词计数，这是一个有状态的算子
     */
    public static class WordCounter implements Operator<String, String>, Stateful {
        private Map<String, Object> state;

        @Override
        public void initializeState(Map<String, Object> state) {
            this.state = state;
        }

        @Override
        public Map<String, Object> snapshotState() {
            // 创建状态的深拷贝用于快照
            return new ConcurrentHashMap<>(this.state);
        }

        @Override
        public void process(String word, Collector<String> collector) {
            Integer count = (Integer) state.getOrDefault(word, 0);
            count++;
            state.put(word, count);
            collector.collect(word + ": " + count);
        }
    }

    /**
     * Sink: 打印最终结果到控制台
     */
    public static class ConsoleSink implements Sink<String> {
        @Override
        public void invoke(String value) {
            LOG.info("Sink > " + value);
        }
    }

    public static void main(String[] args) throws Exception {
        // 1. 创建 mini-flink 集群环境 (本地单机版)
        TaskManager taskManager = new TaskManager(4); // 4个任务槽
        JobManager jobManager = new JobManager(taskManager);

        // 2. 构建作业图 (JobGraph)
        JobGraph jobGraph = new JobGraph("WordCount Job");

        // 创建 Source 顶点
        JobVertex sourceVertex = new JobVertex("SentenceSource", new SentenceSource(), 1);
        // 创建 Splitter 顶点
        JobVertex splitterVertex = new JobVertex("Splitter", new Splitter(), 2);
        // 创建 Counter 顶点
        JobVertex counterVertex = new JobVertex("WordCounter", new WordCounter(), 2);
        // 创建 Sink 顶点
        JobVertex sinkVertex = new JobVertex("ConsoleSink", new ConsoleSink(), 1);

        jobGraph.addVertex(sourceVertex);
        jobGraph.addVertex(splitterVertex);
        jobGraph.addVertex(counterVertex);
        jobGraph.addVertex(sinkVertex);

        // 连接顶点，形成DAG
        jobGraph.addEdge(sourceVertex, splitterVertex);
        jobGraph.addEdge(splitterVertex, counterVertex);
        jobGraph.addEdge(counterVertex, sinkVertex);

        // 3. 提交作业
        jobManager.submitJob(jobGraph).join(); // join() 会阻塞直到作业部署完成

        LOG.info("作业已部署，正在运行中... (程序将持续运行，可手动停止)");

        // 保持主线程存活，以便观察
        Thread.currentThread().join();

        // 正常情况下需要优雅关闭
        // jobManager.shutdown();
    }
}