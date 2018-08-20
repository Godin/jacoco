package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ConcurrencyTest {

	public static void main(String[] args) {
		final Analyzer analyzer = new Analyzer(new ExecutionDataStore(),
				new ICoverageVisitor() {
					public void visitCoverage(IClassCoverage coverage) {
					}
				});

		final Thread[] threads = new Thread[10];
		final CountDownLatch latch = new CountDownLatch(threads.length);
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread() {
				@Override
				public void run() {
					try {
						analyzer.analyzeAll(new File("target/classes"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					latch.countDown();
					try {
						latch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}
		for (Thread thread : threads) {
			thread.start();
		}
	}

}
