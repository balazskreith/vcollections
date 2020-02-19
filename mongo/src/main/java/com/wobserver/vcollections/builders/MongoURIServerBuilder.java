package com.wobserver.vcollections.builders;

import java.util.Map;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

class MongoURIServerBuilder extends AbstractBuilder {

	public static final String PORT_CONFIG_KEY = "port";
	public static final String HOST_CONFIG_KEY = "host";

	/**
	 * Puts all configuration to the inside holder in the builder.
	 * These configurations are used when the {@link this#build()} is called.
	 *
	 * @param configs The provided configurations
	 * @return {@link this} builder.
	 */
	public MongoURIServerBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	/**
	 * Builds the string to connect to  the deddicated server
	 *
	 * @return
	 */
	public String build() {
		Config config = this.convertAndValidate(Config.class);
		String result = String.join(":", config.host, config.port.toString());
		return result;
	}

	/**
	 * Sets the port to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoURIServerBuilder}.
	 */
	public MongoURIServerBuilder withLocalhost(String value) {
		this.configs.put(HOST_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the port to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoURIServerBuilder}.
	 */
	public MongoURIServerBuilder withPort(Integer value) {
		this.configs.put(PORT_CONFIG_KEY, value);
		return this;
	}

	public static class Config {

		@NotNull
		public String host;

		@Min(0)
		@Max(65535)
		public Integer port = 27017;
	}
}
