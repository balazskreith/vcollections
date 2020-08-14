package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.aaaaa.AbstractBuilder;
import com.balazskreith.vcollections.adapters.SerDe;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Responsible to create an object based on class name in string
 *
 * @param <T> the type of the object
 */
public class ObjectBuilder<T> extends AbstractBuilder<T, ObjectBuilder<T>> implements SerDe<T> {
	private String className;
	private final List<String> packages;
	private final Map<String, Object> params;
	private String methodName;

	public ObjectBuilder(ObjectMapper objectMapper) {
		this.packages = new LinkedList<>();
		this.params = new LinkedHashMap<>();
	}

	public ObjectBuilder() {
		this(new ObjectMapper());
	}

	public ObjectBuilder<T> withClassName(String className) {
		this.className = className;
		return this;
	}

	/**
	 * TERM: package.path.to.class#OPTIONAL_METHOD( Value : TypeOfClass )
	 *
	 * @param parsableConfig
	 * @return
	 */
	public ObjectBuilder<T> withParsableConfig(String parsableConfig) {
		this.className = className;
		return this;
	}

	public ObjectBuilder<T> withParameter(String typeName, Object value) {
		this.params.put(typeName, value);
		return this;
	}

	public ObjectBuilder<T> withPackage(String packageName) {
		this.packages.add(packageName);
		return this;
	}

	public ObjectBuilder<T> withPackages(String... packageNames) {
		this.packages.addAll(Arrays.asList(packageNames));
		return this;
	}

	public ObjectBuilder<T> withPackages(List<String> packageNames) {
		this.packages.addAll(packageNames);
		return this;
	}


	@Override
	public T build() {
		T result;
		if (this.methodName != null) {
			result = this.invokeMethod();
		} else {
			result = this.invokeConstructor();
		}
		return result;
	}

	@Override
	public T deserialize(byte[] data) throws IOException {
		// TODO: learn regular expression a bit
		return null;
	}

	@Override
	public byte[] serialize(T data) throws IOException {
		if (Objects.isNull(data)) {
			throw new NullPointerException("the data intended to be serialized cannot be null");
		}

		String result = data.getClass().getName();
		if (this.methodName != null) {
			result = result.concat("#").concat(this.methodName);
		}
		if (0 < this.params.size()) {
			List<String> paramsList = this.params.entrySet().stream().map(
					entry -> String.format("%s:%s", entry.getValue().toString(), entry.getKey())
			).collect(Collectors.toList());
			result = result.concat("(").concat(String.join(",", paramsList)).concat(")");
		}
		return result.getBytes();
	}

	/**
	 * Gets a klass corresponding to the name of the class
	 *
	 * @param <T> the type of the class
	 * @return the class type
	 * @throws RuntimeException if the type of the klass does not exists
	 */
	protected <T> Class<T> getClassFor(String klassName) {
		Class<T> result = null;
		List<String> classes = new LinkedList<>();
		classes.add(klassName);

		this.packages.stream().forEach(packageName -> classes.add(
				packageName.concat(".").concat(className
				)));
		// first let's try with resolver
		for (Iterator<String> it = classes.iterator(); it.hasNext(); ) {
			String candidateName = it.next();
			Class<T> candidate;
			try {
				candidate = (Class<T>) Class.forName(candidateName);
			} catch (ClassNotFoundException e) {
				continue;
			}

			if (result != null && result.getName().equals(candidate.getName()) == false) {
				throw new RuntimeException("Duplicated class found for "
						.concat(className).concat(": ").concat(result.getName()).concat(" and ").concat(
								candidate.getName()
						));
			}
			result = candidate;
		}

		if (result == null) {
			throw new RuntimeException("Class type for " + className + " does not exist");
		}

		return result;
	}

	/**
	 * Invokes a constructor for the given class
	 *
	 * @return An instantiated object with a type of {@link T}.
	 * @throws RuntimeException if there was a problem in invocation
	 */
	protected T invokeMethod() {
		Class klass = this.getClassFor(this.className);
		List<String> types = new ArrayList<>(this.params.keySet());
		Object[] params = this.params.values().toArray();
		Method declaredMethod;
		try {
			switch (types.size()) {
				case 0:
					declaredMethod = klass.getDeclaredMethod(this.methodName);
					break;
				case 1:
					Class<?> klass11 = this.getClassFor(types.get(0));
					declaredMethod = klass.getDeclaredMethod(this.methodName, klass11);
					break;
				case 2:
					Class<?> klass21 = this.getClassFor(types.get(0));
					Class<?> klass22 = this.getClassFor(types.get(1));
					declaredMethod = klass.getDeclaredMethod(this.methodName, klass21, klass22);
					break;
				default:
					throw new RuntimeException("Currently maximum 2 parameters are supported. Sorry.");
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Error by getting declared method for type " + klass.getName(), e);
		}

		Object constructed;

		try {
			constructed = declaredMethod.invoke(null, params);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		}
		return (T) constructed;
	}

	/**
	 * Invokes a constructor for the given class
	 *
	 * @return An instantiated object with a type of {@link T}.
	 * @throws RuntimeException if there was a problem in invocation
	 */
	protected T invokeConstructor() {
		Class<T> klass = this.getClassFor(this.className);
		Object[] params = this.params.values().toArray();
		List<String> types = new ArrayList<>(this.params.keySet());
		Constructor<T> constructor;
		try {
			switch (types.size()) {
				case 0:
					constructor = klass.getConstructor();
					break;
				case 1:
					Class<?> klass11 = this.getClassFor(types.get(0));
					constructor = klass.getConstructor(klass11);
					break;
				case 2:
					Class<?> klass21 = this.getClassFor(types.get(0));
					Class<?> klass22 = this.getClassFor(types.get(1));
					constructor = klass.getConstructor(klass21, klass22);
					break;
				default:
					throw new RuntimeException("Currently maximum 2 parameters are supported. Sorry.");
			}

		} catch (NoSuchMethodException e) {
			throw new RuntimeException("No constructor exists which accept () for type " + klass.getName(), e);
		}
		Object constructed;

		try {
			constructed = constructor.newInstance(params);
		} catch (InstantiationException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		}
		return (T) constructed;
	}


}
