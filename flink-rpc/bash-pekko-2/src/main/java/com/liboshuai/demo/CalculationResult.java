package com.liboshuai.demo;

import java.io.Serializable;

public class CalculationResult implements Serializable {
    public final int result;

    public CalculationResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "CalculationResult{" +
                "result=" + result +
                '}';
    }
}
