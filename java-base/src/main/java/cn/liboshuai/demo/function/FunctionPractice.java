package cn.liboshuai.demo.function;

import java.util.function.Function;

/**
 * 示例 1: Function<T, R> (转换型)
 * * 演示 Function 接口，它接收一个输入（T类型），并返回一个输出（R类型）。
 */
public class FunctionPractice {

    public static void main(String[] args) {

        // 1. 定义一个 Function：
        //    输入类型为 String，输出类型为 Integer
        //    Lambda 表达式实现了 apply(T t) 方法
        Function<String, Integer> lengthCalculator = (String s) -> {
            System.out.println("正在计算 '" + s + "' 的长度...");
            return s.length();
        };

        // 2. 使用 .apply() 方法执行函数
        String name = "Java 8";
        Integer len = lengthCalculator.apply(name);

        System.out.println("计算结果: " + len); // 输出: 9

        System.out.println("---");

        // 3. 语法精简：
        //    我们通常会使用更简洁的 Lambda
        Function<String, Integer> simplifiedLengthCalc = s -> s.length();

        // 4. 再次执行
        Integer len2 = simplifiedLengthCalc.apply("Functional");
        System.out.println("精简版计算结果: " + len2); // 输出: 10

        // 5. 另一个例子：将 Integer 转换为 String
        Function<Integer, String> intToString = i -> "Value=" + i;
        String result = intToString.apply(123);
        System.out.println("数字转换结果: " + result); // 输出: Value=123
    }
}
