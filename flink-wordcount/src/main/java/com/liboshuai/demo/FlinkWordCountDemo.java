package com.liboshuai.demo;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * Hello world!
 *
 */
public class FlinkWordCountDemo
{
    public static void main(String[] args) throws Exception {
        // 1. 获取 Flink 执行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2. 创建数据源：直接从代码中的元素创建
        // 您可以使用 fromElements 或 fromCollection
        DataStream<String> text = env.fromElements(
                "Hello Flink world count",
                "This is a simple Flink example",
                "Flink is powerful for stream processing",
                "Word count is a classic example for Flink"
        ).name("in-memory-source"); // 为操作命名是一个好习惯，便于调试和监控

        // 3. 执行转换操作
        DataStream<Tuple2<String, Integer>> counts =
                // 使用 Tokenizer 将每一行文本分割成 (单词, 1) 的形式
                text.flatMap(new Tokenizer())
                        .name("tokenizer")
                        // 按单词 (Tuple2 的第一个元素, f0) 分组
                        .keyBy(value -> value.f0)
                        // 对每个单词的计数 (Tuple2 的第二个元素, f1) 进行求和
                        .sum(1)
                        .name("word-aggregator");

        // 4. 将结果打印到控制台
        counts.print().name("print-to-console-sink");

        // 5. 执行 Flink 作业
        // "Minimal WordCount" 是作业的名称，会显示在 Flink UI 或日志中
        env.execute("Minimal Flink WordCount Example");
    }

    /**
     * 自定义的 FlatMapFunction，用于将输入的字符串（行）分割成单词，
     * 并为每个单词输出一个 (单词, 1) 的 Tuple2。
     */
    public static final class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // 将文本转换为小写，并按非单词字符分割
            String[] tokens = value.toLowerCase().split("\\W+");

            // 遍历所有单词
            for (String token : tokens) {
                // 如果单词不为空，则输出 (单词, 1)
                if (!token.isEmpty()) {
                    out.collect(new Tuple2<>(token, 1));
                }
            }
        }
    }
}
