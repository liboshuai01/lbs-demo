package cn.liboshuai.demo.jvm.demo1;

/**
 * 演示JVM类加载器和双亲委派模型
 * 编译并运行:
 * 1. 保存文件为 ClassLoaderDemo.java
 * 2. javac ClassLoaderDemo.java
 * 3. java ClassLoaderDemo
 */
public class ClassLoaderDemo {

    public static void main(String[] args) {

        System.out.println("--- 1. 展示类加载器层次结构 ---");

        // 1. 获取当前类（ClassLoaderDemo）的类加载器
        // 这是 应用程序类加载器 (AppClassLoader)
        ClassLoader appClassLoader = ClassLoaderDemo.class.getClassLoader();
        System.out.println("应用程序类加载器 (AppClassLoader): \n" + appClassLoader);

        // 2. 获取 应用程序类加载器 的父加载器
        // 这是 扩展类加载器 (ExtClassLoader)
        ClassLoader extClassLoader = appClassLoader.getParent();
        System.out.println("\n扩展类加载器 (ExtClassLoader): \n" + extClassLoader);

        // 3. 获取 扩展类加载器 的父加载器
        // 这是 启动类加载器 (BootstrapClassLoader)
        // BootstrapClassLoader 是用 C++ 实现的，在 Java 中获取不到它的引用，所以会返回 null
        ClassLoader bootstrapClassLoader = extClassLoader.getParent();
        System.out.println("\n启动类加载器 (BootstrapClassLoader): \n" + bootstrapClassLoader + " (返回null, 因为它是由C++实现的)");


        System.out.println("\n--- 2. 演示不同类的加载器 ---");

        // 演示一：加载核心库中的类 (java.lang.String)
        // String 类是 Java 核心库的一部分，由 启动类加载器 (BootstrapClassLoader) 加载
        // 因此，getProtectionDomain().getClassLoader() 也会返回 null
        System.out.println("\nString.class 是由哪个加载器加载的?");
        System.out.println(String.class.getClassLoader());


        // 演示二：加载当前应用程序中的类 (ClassLoaderDemo)
        // ClassLoaderDemo 是我们自己写的类，由 应用程序类加载器 (AppClassLoader) 加载
        System.out.println("\nClassLoaderDemo.class 是由哪个加载器加载的?");
        System.out.println(ClassLoaderDemo.class.getClassLoader());


        System.out.println("\n--- 3. 演示双亲委派（尝试加载核心类） ---");

        try {
            // 当 AppClassLoader 尝试加载 "java.lang.String" 时:
            // 1. AppClassLoader 委托给父加载器 ExtClassLoader
            // 2. ExtClassLoader 委托给父加载器 BootstrapClassLoader
            // 3. BootstrapClassLoader 发现自己可以加载 (在 rt.jar 中)
            // 4. BootstrapClassLoader 加载 String.class 并返回
            // 5. AppClassLoader 最终返回的是 BootstrapClassLoader 加载的类

            Class<?> stringClass = appClassLoader.loadClass("java.lang.String");
            System.out.println("使用 AppClassLoader 成功加载: " + stringClass.getName());
            System.out.println("实际加载器: " + stringClass.getClassLoader() + " (null 代表 Bootstrap)");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
