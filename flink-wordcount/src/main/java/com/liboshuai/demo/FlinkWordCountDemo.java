package com.liboshuai.demo;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flink 单词计数示例
 */
public class FlinkWordCountDemo {

    private static final Logger LOG = LoggerFactory.getLogger(FlinkWordCountDemo.class);

    // 定义参数键
    private static final String KEY_HOSTNAME = "hostname";
    private static final String KEY_PORT = "port";

    // 定义默认值
    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 9999;

    public static void main(String[] args) throws Exception {
        // 1. 设置执行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 为了演示方便，这里设置为单并行度。在生产环境中，建议移除此行，让 Flink 自动管理或通过配置指定。
        env.setParallelism(1);

        // 2. 使用 ParameterTool 解析命令行参数
        // 示例: --hostname my-host --port 12345
        final ParameterTool params = ParameterTool.fromArgs(args);

        // 检查是否提供了必要的参数，并提供友好的提示
        if (args.length == 0) {
            LOG.warn("未提供任何命令行参数，将使用默认主机与端口: {}:{}", DEFAULT_HOSTNAME, DEFAULT_PORT);
        }

        // 3. 构建并执行 Flink 作业
        buildAndExecuteJob(env, params);
    }

    /**
     * 构建 Flink 数据处理管道并执行。
     * @param env Flink 流执行环境
     * @param params 命令行参数工具
     */
    private static void buildAndExecuteJob(StreamExecutionEnvironment env, ParameterTool params) throws Exception {
        String hostname = params.get(KEY_HOSTNAME, DEFAULT_HOSTNAME);
        int port = params.getInt(KEY_PORT, DEFAULT_PORT);

        LOG.info("开始构建 Flink 作业，数据源 Socket: {}:{}", hostname, port);

        // 4. 创建数据源：从 Socket 读取文本流
        DataStream<String> textStream = env.socketTextStream(hostname, port)
                .name("socket-数据源");

        // 5. 执行转换操作 (ETL)
        DataStream<Tuple2<String, Integer>> counts = textStream
                // 使用 Lambda 表达式进行 FlatMap, 更简洁
                .flatMap((String line, Collector<Tuple2<String, Integer>> out) -> {
                    // 按非字母数字字符分割
                    String[] tokens = line.toLowerCase().split("\\W+");
                    for (String token : tokens) {
                        if (!token.isEmpty()) {
                            out.collect(new Tuple2<>(token, 1));
                        }
                    }
                })
                // [最佳实践] 显式提供类型信息，避免 Java 类型擦除导致的问题
                .returns(Types.TUPLE(Types.STRING, Types.INT))
                .name("分词器(Tokenizer)")
                .keyBy(value -> value.f0) // 根据单词 (Tuple 的第0个元素) 进行分组
                .sum(1) // 对计数值 (Tuple 的第1个元素) 进行累加
                .name("单词聚合器(Aggregator)");

        // 6. 添加 Sink：将结果使用自定义的 SinkFunction 打印到日志
        counts.addSink(new LogPrintSink<>()).name("日志打印(Sink)");

        // 7. 执行 Flink 作业，并指定一个有意义的作业名称
        LOG.info("作业图构建完成，开始执行 Flink 作业...");
        env.execute("Flink WordCount 示例 (Socket 输入)");
    }

    /**
     * 自定义的 SinkFunction，用于将数据流中的每个元素通过 SLF4J/Log4j2 打印出来。
     * @param <T> 数据类型
     */
    public static class LogPrintSink<T> implements SinkFunction<T> {

        @Override
        public void invoke(T value, Context context) throws Exception {
            // 使用中文日志模板记录每个到达 Sink 的元素
            LOG.info("Flink 计算结果: {}", value);
        }
    }
}