package com.liboshuai.demo;

import java.io.Serializable;

public class Subtract implements Serializable {
    public final int a;
    public final int b;

    public Subtract(int a, int b) {
        this.a = a;
        this.b = b;
    }
}
