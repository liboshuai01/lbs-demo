package com.liboshuai.demo;

import java.io.Serializable;

public class Add implements Serializable {
    public final int a;
    public final int b;

    public Add(int a, int b) {
        this.a = a;
        this.b = b;
    }
}
