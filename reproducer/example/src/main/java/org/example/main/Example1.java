package org.example.main;

import java.io.IOException;
import java.io.InputStream;

public class Example1 {

    public static void main(String[] args) throws Exception {
        ClassLoader classLoader = new ClassLoader() {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("org.example")) {
                    final InputStream resourceAsStream = getResourceAsStream(
                            name.replace('.', '/') + ".class");
                    final byte[] bytes;
                    try {
                        bytes = resourceAsStream.readAllBytes();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return defineClass(name, bytes, 0, bytes.length);
                }
                final Class<?> cls = super.loadClass(name, resolve);
                if (name.startsWith("java.lang") && cls.getModule() != Object.class.getModule()) {
                    throw new ClassNotFoundException();
                }
                return cls;
            }
        };
        classLoader.loadClass(Example1.class.getName()).getConstructor().newInstance();
    }

}
