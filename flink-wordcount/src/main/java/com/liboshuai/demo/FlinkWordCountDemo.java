package com.liboshuai.demo;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.Collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flink 单词计数示例，从 Socket 读取数据。
 */
public class FlinkWordCountDemo {

    private static final Logger LOG = LoggerFactory.getLogger(FlinkWordCountDemo.class);

    // 定义 Socket 数据源的默认主机和端口
    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 9999;

    public static void main(String[] args) throws Exception {
        // 1. 获取 Flink 执行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); // 为了演示方便，这里设置为单并行度

        // 2. 创建数据源：从 Socket 读取数据
        //    参数：
        //    - 主机名 (命令行或默认)
        //    - 端口号 (命令行或默认)
        String hostname = DEFAULT_HOSTNAME;
        int port = DEFAULT_PORT;

        // 允许从命令行参数传入主机名和端口号
        try {
            if (args.length == 2) {
                hostname = args[0];
                port = Integer.parseInt(args[1]);
            } else if (args.length == 0) {
                LOG.warn("No hostname and port provided. Using default: {}:{}", DEFAULT_HOSTNAME, DEFAULT_PORT);
            } else {
                System.err.println("Usage: FlinkWordCountDemo <hostname> <port>");
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[1]);
            System.err.println("Usage: FlinkWordCountDemo <hostname> <port>");
            return;
        }

        LOG.info("Reading data from Socket: {}:{}", hostname, port);
        DataStream<String> text = env.socketTextStream(hostname, port)
                .name("socket-source"); // 为数据源命名

        // 3. 执行转换操作
        DataStream<Tuple2<String, Integer>> counts =
                text.flatMap(new Tokenizer())
                        .name("tokenizer")
                        .keyBy(value -> value.f0) // 根据单词进行分组
                        .sum(1) // 对单词出现的次数（第二个元素）进行求和
                        .name("word-aggregator"); // 为聚合操作命名

        // 4. 将结果使用自定义的 SinkFunction 打印到控制台（通过 SLF4J/Log4j2）
        counts.addSink(new LogPrintSink<>()).name("log4j-print-sink"); // 为 Sink 命名

        // 5. 执行 Flink 作业
        env.execute("Minimal Flink WordCount Example (Socket Input)");
    }

    /**
     * 自定义的 FlatMapFunction，用于将输入的字符串（行）分割成单词，
     * 并为每个单词输出一个 (单词, 1) 的 Tuple2。
     */
    public static final class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // 将输入行转换为小写，并按非字母数字字符分割
            String[] tokens = value.toLowerCase().split("\\W+");

            for (String token : tokens) {
                if (!token.isEmpty()) {
                    // 对于每个非空单词，发出一个 (单词, 1) 的元组
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
            // 在这里使用 SLF4J 记录每个到达 Sink 的元素
            LOG.info("Flink Result: {}", value);
        }
    }
}