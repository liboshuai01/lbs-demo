package com.liboshuai.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 不再是 'implements Serializable'
// 而是实现我们自己的标记接口
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Greet implements CborSerializable {
    public String name;
}
