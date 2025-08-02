package com.liboshuai.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ask 模式的回复消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GreetingReply implements CborSerializable {
    public String message;
}
