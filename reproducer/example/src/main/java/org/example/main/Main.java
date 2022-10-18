package org.example.main;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class Main {
    private static byte[] createClass(String name) {
        ClassNode classNode = new ClassNode();
        classNode.version = Opcodes.V1_5;
        classNode.name = name.replace('.', '/');
        classNode.superName = "java/lang/Object";
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    static class AccessibleObjectMirror {
        boolean override;
        volatile Object accessCheckCache;
        private static volatile boolean printStackWhenAccessFails;
        private static volatile boolean printStackPropertiesSet;
    }

    private static Class<?> defineUsingUnsafe(ClassLoader classLoader, String name, byte[] classBytes) throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Field override = AccessibleObjectMirror.class.getDeclaredField("override");

        Method putBooleanMethod = unsafeClass.getMethod("putBoolean", Object.class, long.class, boolean.class);

        long offset = (Long) unsafeClass
          .getMethod("objectFieldOffset", Field.class)
          .invoke(unsafe, override);

        Class<?> internalUnsafeClass = Class.forName("jdk.internal.misc.Unsafe");
        Field theUnsafe = internalUnsafeClass.getDeclaredField("theUnsafe");
        putBooleanMethod.invoke(unsafe, theUnsafe, offset, true);
        Object internalUnsafe = theUnsafe.get(null);
        Method defineClassMethod = internalUnsafeClass
          .getMethod("defineClass",
            String.class,
            byte[].class,
            int.class,
            int.class,
            ClassLoader.class,
            ProtectionDomain.class);
        putBooleanMethod.invoke(unsafe, defineClassMethod, offset, true);
        return (Class<?>) defineClassMethod.invoke(internalUnsafe, name, classBytes, 0, classBytes.length, classLoader, null);
    }

    private static Class<?> defineUsingLookup(Class<?> targetClass, byte[] classBytes) throws Exception {
        return MethodHandles
          .privateLookupIn(targetClass, MethodHandles.lookup())
          .defineClass(classBytes);
    }

    private static void define(Class<?> targetClass) throws Exception {
        System.err.println(targetClass + " getModule: " + targetClass.getModule());
        {
            String className = targetClass.getPackageName() + ".UsingUnsafe";
            byte[] classBytes = createClass(className);
            Class<?> definedClass = defineUsingUnsafe(null, className, classBytes);
            System.err.println(definedClass.getName() + " getModule: " + definedClass.getModule());
        }
        {
            String className = targetClass.getPackageName() + ".UsingLookup";
            byte[] classBytes = createClass(className);
            Class<?> definedClass = defineUsingLookup(targetClass, classBytes);
            System.err.println(definedClass.getName() + " getModule: " + definedClass.getModule());
        }
    }

    // https://github.com/eclipse-openj9/openj9/commit/09617d90296bc85edf82528f3367eb850560642b

    // Java_java_lang_ClassLoader_defineClassImpl
    // https://github.com/eclipse-openj9/openj9/blob/master/runtime/jcl/common/clsldr.cpp#L52

    // defineClassCommon
    // https://github.com/eclipse-openj9/openj9/blob/da77f9f134bd46b7a5e0a608101f4d55eb4da8c9/runtime/jcl/common/jcldefine.c#L30

    // https://github.com/eclipse-openj9/openj9/blob/6af5b7b4d469418141b92e7f4621fdeacb51a75f/runtime/bcutil/defineclass.c#L58

    public static void main(String[] args) throws Exception {
        define(Object.class);
        define(java.sql.Array.class);
    }
}
