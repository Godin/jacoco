package org.example.agent;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;

public class Agent {
    public static void run(MethodHandles.Lookup lookup) throws Exception {
        Class<?> cls = lookup.defineClass(createClass("org.example.main.Test"));
        System.out.println(cls + " getModule: " + cls.getModule());
    }

    public static void premain(String args, Instrumentation instrumentation) throws Exception {
        System.err.println("premain");

        System.err.println(Agent.class + " getModule: " + Agent.class.getModule());
        System.err.println(Agent.class + " getClassLoader.getUnnamedModule: " + Agent.class.getClassLoader().getUnnamedModule());

        Class<?> cls = MethodHandles
                .privateLookupIn(Object.class, MethodHandles.lookup())
                .defineClass(createClass("java.lang.Test"));
        System.err.println(cls + " getModule: " + cls.getModule());
        System.err.println(Object.class + " getModule: " + Object.class.getModule());
    }

    private static byte[] createClass(String name) {
        ClassNode classNode = new ClassNode();
        classNode.version = Opcodes.V1_5;
        classNode.name = name.replace('.', '/');
        classNode.superName = "java/lang/Object";
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

}
