package org.jacoco.core.internal.flow;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class AsmUtils {

	private AsmUtils() {
	}

	public static ClassNode classBytesToClassNode(final byte[] classBytes) {
		final ClassNode classNode = new ClassNode();
		new ClassReader(classBytes).accept(classNode, ClassReader.SKIP_FRAMES);
		return classNode;
	}

	public static String instrumentToString(final ClassNode classNode) {
		final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(
				new Textifier());
		final MethodProbesVisitor methodProbesVisitor = new MethodProbesVisitor(
				traceMethodVisitor) {
			@Override
			public void visitProbe(final int probeId) {
				traceMethodVisitor.p.text
						.add("    // Probe[" + probeId + "]\n");
			}

			@Override
			public void visitJumpInsnWithProbe(final int opcode,
					final Label label, final int probeId, final IFrame frame) {
				traceMethodVisitor.p.text
						.add("    // JumpInsnWithProbe[" + probeId + "]:\n");
				visitJumpInsn(opcode, label);
			}

			@Override
			public void visitInsnWithProbe(final int opcode,
					final int probeId) {
				traceMethodVisitor.p.text
						.add("    // InsnWithProbe[" + probeId + "]:\n");
				visitInsn(opcode);
			}

			@Override
			public void visitTableSwitchInsnWithProbes(final int min,
					final int max, final Label dflt, final Label[] labels,
					final IFrame frame) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void visitLookupSwitchInsnWithProbes(final Label dflt,
					final int[] keys, final Label[] labels,
					final IFrame frame) {
				throw new UnsupportedOperationException();
			}
		};
		final MethodProbesAdapter methodProbesAdapter = new MethodProbesAdapter(
				methodProbesVisitor, new IProbeIdGenerator() {
					private int id;

					public int nextId() {
						return id++;
					}
				});
		final StringWriter stringWriter = new StringWriter();
		traceMethodVisitor.p.visit(classNode.version, classNode.access,
				classNode.name, classNode.signature, classNode.superName, null);
		for (final MethodNode methodNode : classNode.methods) {
			traceMethodVisitor.p.visitMethod(methodNode.access, methodNode.name,
					methodNode.desc, methodNode.signature, null);
			LabelFlowAnalyzer.markLabels(methodNode);
			methodNode.accept(methodProbesAdapter);
		}
		traceMethodVisitor.p.print(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

}
