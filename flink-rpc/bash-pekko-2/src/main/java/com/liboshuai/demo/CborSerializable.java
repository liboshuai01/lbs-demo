package com.liboshuai.demo;

import java.io.Serializable;

/**
 * 一个标记接口，用于所有希望通过 Pekko Jackson CBOR 序列化的类。
 * 这是一种安全最佳实践，用于明确指定哪些类可以被序列化，
 * 避免将宽泛的 java.io.Serializable 绑定到序列化器上。
 */
public interface CborSerializable extends Serializable {
    // 这是一个标记接口，所以内部不需要任何方法。
    // 继承 Serializable 是一个好习惯，但对于 Jackson 不是必须的。
}
