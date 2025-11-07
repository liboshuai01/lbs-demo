package cn.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;

public class GenericArrayError {

    private static final Logger log = LoggerFactory.getLogger(GenericArrayError.class);

    static class Generator<T> {
        private final T[] array;

        public Generator(Class<T> clazz, int size) {
            @SuppressWarnings("unchecked")
            T[] array = (T[]) Array.newInstance(clazz, size);
            this.array = array;
        }

        public void setValue(int index, T value) {
            array[index] = value;
        }

        public T getValue(int index) {
            return array[index];
        }
    }

    public static void main(String[] args) {
        Generator<Integer> integerGenerator = new Generator<>(Integer.class, 10);
        Generator<String> stringGenerator = new Generator<>(String.class, 10);

        integerGenerator.setValue(0, 1);
        Integer integerGeneratorValue = integerGenerator.getValue(0);
        log.info("integerGeneratorValue: {}", integerGeneratorValue);

        stringGenerator.setValue(0, "one");
        String stringGeneratorValue = stringGenerator.getValue(0);
        log.info("stringGeneratorValue: {}", stringGeneratorValue);
    }
}
