package com.liboshuai.demo;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.Collector;

import org.slf4j.Logger; // 推荐使用 SLF4J 门面
import org.slf4j.LoggerFactory; // 推荐使用 SLF4J 门面

/**
 * Hello world!
 *
 */
public class FlinkWordCountDemo
{

    // 推荐使用 SLF4J Loggers，因为它们是日志门面，可以轻松切换底层实现（如 Log4j2, Logback 等）
    private static final Logger LOG = LoggerFactory.getLogger(FlinkWordCountDemo.class);

    public static void main(String[] args) throws Exception {
        // 1. 获取 Flink 执行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 2. 创建数据源：直接从代码中的元素创建
        DataStream<String> text = env.fromElements(
                "Hello Flink world count",
                "This is a simple Flink example",
                "Flink is powerful for stream processing",
                "Word count is a classic example for Flink"
        ).name("in-memory-source");

        // 3. 执行转换操作
        DataStream<Tuple2<String, Integer>> counts =
                text.flatMap(new Tokenizer())
                        .name("tokenizer")
                        .keyBy(value -> value.f0)
                        .sum(1)
                        .name("word-aggregator");

        // 4. 将结果使用自定义的 SinkFunction 打印到控制台（通过 Log4j2）
        counts.addSink(new LogPrintSink<>()).name("log4j-print-sink");

        // 5. 执行 Flink 作业
        env.execute("Minimal Flink WordCount Example (Log4j2 Output)");
    }

    /**
     * 自定义的 FlatMapFunction，用于将输入的字符串（行）分割成单词，
     * 并为每个单词输出一个 (单词, 1) 的 Tuple2。
     */
    public static final class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            String[] tokens = value.toLowerCase().split("\\W+");

            for (String token : tokens) {
                if (!token.isEmpty()) {
                    out.collect(new Tuple2<>(token, 1));
                }
            }
        }
    }

    /**
     * 自定义的 SinkFunction，用于将数据流中的每个元素通过 SLF4J/Log4j2 打印出来。
     * @param <T> 数据类型
     */
    public static class LogPrintSink<T> implements SinkFunction<T> {

        @Override
        public void invoke(T value, Context context) throws Exception {
            // 在这里使用 Log4j2 记录每个到达 Sink 的元素
            LOG.info("Flink Result: {}", value);
            // 注意：生产环境中，直接将所有数据打印到日志可能导致日志量过大，
            // 应该考虑更复杂的日志策略或将数据写入外部存储系统（如 Kafka, HDFS, 数据库等）
        }
    }
}