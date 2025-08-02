package com.liboshuai.demo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Greet implements Serializable {
    public final String name;
}
