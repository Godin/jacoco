package org.jacoco.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.ClassInstrumenter;
import org.jacoco.core.internal.instr.DuplicateFrameEliminator;
import org.jacoco.core.internal.instr.IProbeArrayStrategy;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.internal.instr.MethodInstrumenter;
import org.jacoco.core.internal.instr.ProbeArrayStrategyFactory;
import org.jacoco.core.internal.instr.ProbeInserter;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(1)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode({ Mode.AverageTime, Mode.SingleShotTime })
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class InstrumentationBenchmark {

	ITarget original;
	ITarget instrumented;
	ITarget instrumented0;
	ITarget instrumented1;

	public interface ITarget {
		long run();
	}

	@Setup
	public void setup() throws Exception {
		final byte[] classBytes = Files.readAllBytes(
				Paths.get("target/classes/scenario/Methods.class"));
		original = load(classBytes);
		instrumented = load(instrument(classBytes, null));
		instrumented0 = load(instrument(classBytes, new ProbeInserter0()));
		instrumented1 = load(instrument(classBytes, new ProbeInserter1()));
	}

	/// 81 ns/op
	// @Benchmark
	public long original() {
		return original.run();
	}

	/// 126 ns/op 1.4 times
	// @Benchmark
	public long instrumented() {
		return instrumented.run();
	}

	/// 126 ns/ops 1.4 times
	// @Benchmark
	public long instrumented0() {
		return instrumented0.run();
	}

	/// 185 ns/op 2.3 times
	@Benchmark
	public long instrumented1() {
		return instrumented1.run();
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

	static byte[] instrument(final byte[] source,
			final ProbeStrategy probeInserter) throws IOException {
		IExecutionDataAccessorGenerator accessorGenerator = new IExecutionDataAccessorGenerator() {
			public int generateDataAccessor(final long classid,
					final String classname, final int probeCount,
					final MethodVisitor mv) {
				InstrSupport.push(mv, probeCount);
				mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
				return 2;
			}
		};

		final long classId = CRC64.classId(source);
		final ClassReader reader = InstrSupport.classReaderFor(source);
		final ClassWriter writer = new ClassWriter(reader, 0) {
			@Override
			protected String getCommonSuperClass(final String type1,
					final String type2) {
				throw new IllegalStateException();
			}
		};
		final IProbeArrayStrategy probeArrayStrategy = ProbeArrayStrategyFactory
				.createFor(classId, reader, accessorGenerator);
		final int version = InstrSupport.getMajorVersion(reader);
		final ClassVisitor visitor = new ClassProbesAdapter(
				new ClassInstrumenter(probeArrayStrategy, writer) {
					@Override
					public MethodProbesVisitor visitMethod(int access,
							String name, String desc, String signature,
							String[] exceptions) {
						// TODO allow to use different instrumentation
						// strategies
						// base returns MethodInstrumenter
						// InstrSupport.assertNotInstrumented(name, className);
						final MethodVisitor mv = cv.visitMethod(access, name,
								desc, signature, exceptions);

						if (mv == null) {
							return null;
						}
						final MethodVisitor frameEliminator = new DuplicateFrameEliminator(
								mv);

						// final IProbeArrayStrategy probeArrayStrategy2 =
						// probeInserter == null
						// ? probeArrayStrategy
						// : new NoneProbeArrayStrategy();
						IProbeArrayStrategy probeArrayStrategy2 = probeArrayStrategy;

						final ProbeInserter probeVariableInserter = new ProbeInserter(
								access, name, desc, frameEliminator,
								probeArrayStrategy2) {

							@Override
							public void visitCode() {
								// TODO do not insert variable
								super.visitCode();
							}

							@Override
							public void insertProbe(int id) {
								if (probeInserter == null) {
									super.insertProbe(id);
								} else {
									probeInserter.insertProbe(mv, variable, id);
								}
							}
						};
						return new MethodInstrumenter(probeVariableInserter,
								probeVariableInserter);
					}
				}, InstrSupport.needsFrames(version));
		reader.accept(visitor, ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
	}

	interface ProbeStrategy {
		void insertProbe(MethodVisitor mv, int variable, int id);
	}

	public static void main(String[] args) throws Exception {
		Options options = new OptionsBuilder()
				.jvmArgsAppend("-XX:+UnlockDiagnosticVMOptions",
						// "-XX:+LogCompilation",
						"-XX:+CompilerDirectivesPrint",
						// TODO seems that next option is overridden by JMH
						// option `-XX:CompileCommandFile=`
						"-XX:CompilerDirectivesFile=CompilerDirectives.json")
				.jvmArgsAppend("-XX:CompileCommand=exclude,scenario/Methods.*")
				.include(InstrumentationBenchmark.class.getName()) //
				.build();
		new Runner(options).run();
	}

}
