package org.jacoco.cli.internal;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;

public class MyTest {

    public static void main(String[] args) throws Exception {
        final PrintWriter out = new PrintWriter(System.out, true);
        final PrintWriter err = new PrintWriter(System.err, true);
        new Main("instrument", "/tmp/j/dump/weblogic/ejb/container/manager/BaseEntityManager.class", "--dest", "/tmp/j/i2").execute(out, err);

//        ClassLoader cl =
        new URLClassLoader(
                new URL[]{new File("/tmp/j/instrumented").toURL()}
        ) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                System.out.println("Loading " + name);
                return super.loadClass(name);
            }
        }.loadClass("weblogic.ejb.container.manager.BaseEntityManager").getDeclaredMethods();
    }

}
