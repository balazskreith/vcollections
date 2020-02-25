package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.storages.FieldAccessor;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FieldAccessBuilder<TField, TObject> extends AbstractBuilder {

	public static final String VALUE_TYPE_CONFIG_KEY = "valueType";
	public static final String KEY_FIELD_CONFIG_KEY = "keyField";

	private Function<TObject, TField> getter;
	private BiConsumer<TObject, TField> setter;
	private AnnotatedElement annotatedElement;
	private boolean run;

	public List<Class> annotations() {
		return null;
	}

	/**
	 * Build a {@link Function< TObject ,  TField >} to extract values from a POJO
	 *
	 * @return A {@link Function< TObject ,  TField >} that built.
	 */
	public Function<TObject, TField> getKeyExtractor() {
		this.run();
		return this.getter;
	}

	/**
	 * Build a {@link Function< TObject ,  TField >} to set values to the POJO
	 *
	 * @return A {@link Function< TObject ,  TField >} that built.
	 */
	public BiConsumer<TObject, TField> getKeySetter() {
		this.run();
		return this.setter;
	}
	
	public FieldAccessor getFieldAccessor() {
		this.run();
		return new FieldAccessor(this.setter, this.getter);
	}

	/**
	 * Build a {@link Function< TObject ,  TField >} to set values to the POJO
	 *
	 * @return A {@link Function< TObject ,  TField >} that built.
	 */
	public AnnotatedElement getAnnotatedElement() {
		this.run();
		return this.annotatedElement;
	}


	private void run() {
		if (this.run) {
			return;
		}
		Config config = this.convertAndValidate(Config.class);
		String keyFieldName = config.keyField;
		Class<TObject> valueType = this.getClassFor(config.valueType);

		Field[] fields = valueType.getFields();
		Optional<Field> findField = Arrays.asList(fields).stream().filter(field -> field.getName().equals(keyFieldName)).findFirst();
		if (findField.isPresent()) {
			final Field keyField = findField.get();
			final int modifier = keyField.getModifiers();
			if (Modifier.isPublic(modifier)) {
				this.getter = new Function<TObject, TField>() {
					@Override
					public TField apply(TObject value) {
						try {
							return (TField) keyField.get(value);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}
				};
				this.setter = new BiConsumer<TObject, TField>() {
					@Override
					public void accept(TObject obj, TField value) {
						try {
							keyField.set(obj, value);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}
				};
				this.annotatedElement = keyField;

				// We have every information we need, so we are done;
				this.run = true;
				return;
			}
		}

		// If we ae here, the keyfield is either not available or not accessable
		Method[] methodsArray = valueType.getMethods();
		if (methodsArray != null) {
			final String methodSuffix = Character.toUpperCase(keyFieldName.charAt(0)) + (keyFieldName.length() < 2 ? "" : keyFieldName.substring(1));
			final String setMethodName = "set" + methodSuffix;
			final String getMethodName = "get" + methodSuffix;
			List<Method> methods = Arrays.asList(methodsArray);
			Optional<Method> findMethod = methods.stream().filter(method -> method.getName().equals(setMethodName)).findFirst();
			if (findMethod.isPresent()) {
				final Method keyMethod = findMethod.get();
				final int modifier = keyMethod.getModifiers();
				if (Modifier.isPublic(modifier)) {
					this.setter = new BiConsumer<TObject, TField>() {
						@Override
						public void accept(TObject obj, TField value) {
							try {
								keyMethod.invoke(obj, value);
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e);
							} catch (InvocationTargetException e) {
								throw new RuntimeException(e);
							}
						}
					};
				}
			}

			findMethod = methods.stream().filter(method -> method.getName().equals(getMethodName)).findFirst();
			if (findMethod.isPresent()) {
				final Method keyMethod = findMethod.get();
				final int modifier = keyMethod.getModifiers();
				if (Modifier.isPublic(modifier)) {
					this.getter = new Function<TObject, TField>() {
						@Override
						public TField apply(TObject value) {
							try {
								return (TField) keyMethod.invoke(value);
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e);
							} catch (InvocationTargetException e) {
								throw new RuntimeException(e);
							}
						}
					};
					this.annotatedElement = keyMethod;
					this.run = true;
					return;
				}
			}
		}

		// No proper methods to extract, the run fails, but it finished
		this.run = true;
	}

	/**
	 * Puts all configuration to the inside holder in the builder.
	 *
	 * @param configs The provided configurations
	 * @return {@link this} builder.
	 */
	public FieldAccessBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	/**
	 * Sets the class for the {@link FieldAccessBuilder}
	 *
	 * @param value the configuration URI for the "{@link FieldAccessBuilder}
	 * @return A {@link FieldAccessBuilder} to set options further
	 */
	public <T> FieldAccessBuilder withClass(String value) {
		this.configs.put(VALUE_TYPE_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the keyfield for the {@link FieldAccessBuilder}
	 *
	 * @param value the configuration URI for the "{@link FieldAccessBuilder}
	 * @return A {@link FieldAccessBuilder} to set options further
	 */
	public FieldAccessBuilder withField(String value) {
		this.configs.put(KEY_FIELD_CONFIG_KEY, value);
		return this;
	}


	public static class Config {
		public String valueType;
		public String keyField;
	}

}
