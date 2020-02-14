package com.wobserver.vcollections.builders;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

class MongoURIBuilder extends AbstractBuilder {

	public static final String USERNAME_CONFIG_KEY = "username";
	public static final String PASSWORD_CONFIG_KEY = "password";
	public static final String SERVERS_CONFIG_KEY = "servers";
	public static final String OPTIONS_CONFIG_KEY = "options";


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
	 * @return This {@link MongoURIBuilder}.
	 */
	public MongoURIBuilder withLocalhost(String value) {
		this.configs.put(HOST_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the port to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoURIBuilder}.
	 */
	public MongoURIBuilder withPort(Integer value) {
		this.configs.put(PORT_CONFIG_KEY, value);
		return this;
	}

	public static class Config {

		@NotNull
		public List<Map<String, Object>> servers;

		public Map<String, Object> options;

		public String username;

		public String password;
	}
}
