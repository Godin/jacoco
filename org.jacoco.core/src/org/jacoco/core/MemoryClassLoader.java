package org.jacoco.core;

import org.objectweb.asm.ClassReader;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

class MemoryClassLoader extends ClassLoader {
	private final Map<String, byte[]> definitions = new HashMap<String, byte[]>();

	public void add(byte[] bytes) {
		definitions.put(new ClassReader(bytes).getClassName(), bytes);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] def = definitions.get(name);
		if (def == null) {
			throw new ClassNotFoundException(name);
		} else {
			return defineClass(name, def, 0, def.length);
		}
	}

	/**
	 * Convenience method to create a instance of the class with the given name.
	 */
	public <T> T newInstance(String name) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InstantiationException {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) loadClass(name);
		return clazz.getConstructor().newInstance();
	}
}
