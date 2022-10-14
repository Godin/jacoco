package org.example.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;

class Agent {

  private static int count = 0;

  public static void agentmain(final String options, final Instrumentation inst) throws Exception {
    ClassFileTransformer transformer = new ClassFileTransformer() {
      @Override
      public byte[] transform(
        ClassLoader loader,
        String className,
        Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain,
        byte[] classfileBuffer
      ) throws IllegalClassFormatException {
        if (!className.endsWith("DebugProbesKt")) {
          return null;
        }
        System.out.println("Saving " + className + " classBeingRedefined:" + classBeingRedefined);
        try {
          count++;
          Files.write(Path.of("/tmp/j/" + count + ".class"), classfileBuffer);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    inst.addTransformer(transformer, true);
  }

}
