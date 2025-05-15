package org.jacoco.benchmark;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.WarmupMode;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * bash configure --with-hsdis=llvm
 * make build-hsdis
 * ls build/macosx-aarch64-server-release/support/hsdis/
 * </pre>
 *
 * According to
 * https://ondrej-kvasnovsky.medium.com/how-to-install-hotspot-disassembler-hsdis-on-macos-40c711a233c9
 * 
 * <pre>
 * cp build/macosx-aarch64-server-release/support/hsdis/libhsdis.dylib $JAVA_HOME/lib/server/libhsdis.dylib
 * cp build/macosx-aarch64-server-release/support/hsdis/hsdis-aarch64.dylib $JAVA_HOME/lib/server/hsdis-aarch64.dylib
 * </pre>
 *
 * TODO idea: show that no dead code elimination in interpreter, branch
 * prediction? see
 * https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_08_DeadCode.java
 * see
 * https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_36_BranchPrediction.java
 *
 * <pre>
 * java -jar ~/.m2/repository/org/openjdk/jmh/jmh-core-benchmarks/1.38-SNAPSHOT/jmh-core-benchmarks-1.38-SNAPSHOT-full.jar
 * </pre>
 * 
 * TODO study performance of CONDY https://bugs.openjdk.org/browse/JDK-8189917
 * TODO check interpreter
 * 
 * <pre>
 * https://progdoc.de/papers/Joker2014/joker2014.html#(3)
 * Only in debug version of VM
 * -XX:+TraceBytecodes
 * -XX:+CountBytecodes
 * -XX:+PrintBytecodeHistogram
 * -XX:+PrintBytecodePairHistogram
 * </pre>
 */
// @OutputTimeUnit(TimeUnit.MICROSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class CondyBenchmark {

	@Param("1")
	private int operations;

	// TODO can be "void" for interpreter
	public interface ITarget {
		Object ldc();

		Object condy();

		Object condy_checkcast();

		Object getstatic();

		Object aconst_null();

		void invokestatic();

		void indy();

		void aload();
	}

	private ITarget target;

	private static final String B_DESC = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)[Z";
	// private static final String B_DESC =
	// "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;";

	@CompilerControl(CompilerControl.Mode.INLINE)
	public static void m(boolean[] b, int i) {
	}

	public static void m() {
	}

	public static CallSite bootstrap(final MethodHandles.Lookup caller,
			final String name, final MethodType type) throws Exception {
		return new ConstantCallSite(caller.findStatic(CondyBenchmark.class, "m",
				MethodType.methodType(void.class)));
	}

	@Setup
	public void setup() throws Exception {
		ClassNode cn = new ClassNode();
		cn.version = Opcodes.V11;
		cn.access = Opcodes.ACC_PUBLIC;
		cn.name = "Target";
		cn.superName = "java/lang/Object";
		cn.interfaces = Collections
				.singletonList(Type.getInternalName(ITarget.class));
		{
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V",
					null, null);
			m.visitVarInsn(Opcodes.ALOAD, 0);
			m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
					"<init>", "()V", false);
			m.visitInsn(Opcodes.RETURN);
			cn.methods.add(m);
		}
		{ // indy
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "indy", "()V",
					null, null);
			final MethodType methodType = MethodType.methodType(CallSite.class,
					MethodHandles.Lookup.class, String.class, MethodType.class);
			final Handle handle = new Handle(Opcodes.H_INVOKESTATIC,
					this.getClass().getCanonicalName().replace('.', '/'),
					"bootstrap", methodType.toMethodDescriptorString(), false);
			for (int i = 0; i < operations; i++) {
				m.visitInvokeDynamicInsn("invoke", "()V", handle);
			}
			m.visitInsn(Opcodes.RETURN);
			cn.methods.add(m);
		}
		{ // invokestatic
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "invokestatic",
					"()V", null, null);
			for (int i = 0; i < operations; i++) {
				m.visitInsn(Opcodes.ACONST_NULL);
				m.visitIntInsn(Opcodes.SIPUSH, Short.MAX_VALUE);
				m.visitMethodInsn(Opcodes.INVOKESTATIC,
						CondyBenchmark.class.getName().replace('.', '/'), "m",
						"([ZI)V", false);
			}
			m.visitInsn(Opcodes.RETURN);
			cn.methods.add(m);
		}
		{ // aload
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "aload", "()V",
					null, null);
			m.visitInsn(Opcodes.ACONST_NULL);
			m.visitVarInsn(Opcodes.ASTORE, 1);
			for (int i = 0; i < operations; i++) {
				m.visitVarInsn(Opcodes.ALOAD, 1);
			}
			m.visitInsn(Opcodes.RETURN);
			cn.methods.add(m);
		}
		{ // ldc
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "ldc",
					"()Ljava/lang/Object;", null, null);
			for (int i = 0; i < operations; i++) {
				m.visitLdcInsn("");
				m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
			}
			m.visitInsn(Opcodes.ARETURN);
			cn.methods.add(m);
		}
		{ // condy BSM
			MethodNode m = new MethodNode(
					Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "bsm", B_DESC,
					null, null);
			m.visitInsn(Opcodes.ICONST_1);
			m.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
			// m.visitLdcInsn("");
			m.visitInsn(Opcodes.ARETURN);
			cn.methods.add(m);
		}
		{ // condy
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "condy",
					"()Ljava/lang/Object;", null, null);
			Handle bsm = new Handle(Opcodes.H_INVOKESTATIC, cn.name, "bsm",
					B_DESC, false);
			ConstantDynamic condy = new ConstantDynamic("condy", "[Z", bsm);
			for (int i = 0; i < operations; i++) {
				m.visitLdcInsn(condy);
			}
			m.visitInsn(Opcodes.ARETURN);
			cn.methods.add(m);
		}
		{ // condy + checkcast
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "condy_checkcast",
					"()Ljava/lang/Object;", null, null);
			Handle bsm = new Handle(Opcodes.H_INVOKESTATIC, cn.name, "bsm",
					B_DESC, false);
			ConstantDynamic condy = new ConstantDynamic("condy",
					"Ljava/lang/Object;", bsm);
			for (int i = 0; i < operations; i++) {
				m.visitLdcInsn(condy);
				m.visitTypeInsn(Opcodes.CHECKCAST, "[Z");
			}
			m.visitInsn(Opcodes.ARETURN);
			cn.methods.add(m);
		}
		cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
				"o", "Ljava/lang/Object;", null, null));
		{ // field + clinit
			MethodNode m = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V",
					null, null);
			m.visitLdcInsn("");
			m.visitFieldInsn(Opcodes.PUTSTATIC, cn.name, "o",
					"Ljava/lang/Object;");
			m.visitInsn(Opcodes.RETURN);
			cn.methods.add(m);
		}
		{ // aconst_null
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "aconst_null",
					"()Ljava/lang/Object;", null, null);
			for (int i = 0; i < operations; i++) {
				m.visitInsn(Opcodes.ACONST_NULL);
			}
			m.visitInsn(Opcodes.ARETURN);
			cn.methods.add(m);
		}
		{ // getstatic
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "getstatic",
					"()Ljava/lang/Object;", null, null);
			for (int i = 0; i < operations; i++) {
				m.visitFieldInsn(Opcodes.GETSTATIC, cn.name, "o",
						"Ljava/lang/Object;");
			}
			m.visitInsn(Opcodes.ARETURN);
			cn.methods.add(m);
		}
		ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cn.accept(classWriter);
		byte[] classBytes = classWriter.toByteArray();
		// Files.write(Paths.get("/tmp/class.class"), classBytes);
		target = load(classBytes);
	}

	@SuppressWarnings("unchecked")
	static <T> T load(final byte[] classBytes) throws Exception {
		final String name = new ClassReader(classBytes).getClassName()
				.replace('/', '.');
		return (T) new ClassLoader() {
			{
				defineClass(name, classBytes, 0, classBytes.length);
			}
		}.loadClass(name).getDeclaredConstructor().newInstance();
	}

	@Benchmark
	public void indy() {
		target.indy();
	}

	@Benchmark
	public void invokestatic() {
		target.invokestatic();
	}

	@Benchmark
	public Object aconst_null() {
		return target.aconst_null();
	}

	@Benchmark
	public Object ldc() {
		return target.ldc();
	}

	@Benchmark
	public Object getstatic() {
		return target.getstatic();
	}

	@Benchmark
	public void aload() {
		target.aload();
	}

	@Benchmark
	public Object condy_checkcast() {
		return target.condy_checkcast();
	}

	@Benchmark
	public Object condy() {
		return target.condy();
	}

	public static void main(String[] args) throws RunnerException {
		final int operations = 100;
		new Runner(new OptionsBuilder() //
				.forks(1) //
				.warmupIterations(3) //
				.measurementIterations(3) //
				.mode(Mode.AverageTime) //
				// .mode(Mode.Throughput) //
				.param("operations", Integer.toString(operations)) //
				.operationsPerInvocation(operations) //
				.jvmArgsAppend( //
						"-XX:CompileCommand=exclude,Target.*" //
						, "-XX:+UnlockDiagnosticVMOptions" //
				) //
					// FIXME printed hottest region is not interpreted code?
					// .addProfiler(org.openjdk.jmh.profile.XCTraceNormProfiler.class)
					// .addProfiler(org.openjdk.jmh.profile.XCTraceAsmProfiler.class)
					// "intelSyntax=true;hotThreshold=0.03") //
					// .include(CondyBenchmark.class.getName() +
					// ".(invokestatic)")
					// .include(CondyBenchmark.class.getName() + ".indy") //
				.include(CondyBenchmark.class.getName()
						+ ".(condy|condy_checkcast|ldc)") //
				// .include(CondyBenchmark.class.getName() +
				// ".(getstatic|aload|condy)") //
				.build()).run();
	}

}
