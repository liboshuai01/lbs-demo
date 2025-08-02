package com.liboshuai.demo;

import com.liboshuai.demo.CborSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ask 模式的请求消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AskForGreeting implements CborSerializable {
    public String name;
}
