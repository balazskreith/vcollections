package com.wobserver.vcollections.builders;

import com.mongodb.MongoClientURI;
import java.util.*;
import javax.validation.constraints.NotNull;

class MongoURIBuilder extends AbstractBuilder {

	public static final String USERNAME_CONFIG_KEY = "username";
	public static final String PASSWORD_CONFIG_KEY = "password";
	public static final String SERVERS_CONFIG_KEY = "servers";
	public static final String OPTIONS_CONFIG_KEY = "options";

	/**
	 * Build the mongoURI
	 *
	 * @return
	 */
	public MongoClientURI build() {
		Config config = this.convertAndValidate(Config.class);
		String credential = String.join(":", config.username, config.password);
		List<String> servers = new ArrayList<>();
		for (Map<String, Object> server : config.servers) {
			MongoURIServerBuilder uriServerBuilder = new MongoURIServerBuilder();
			uriServerBuilder.withConfiguration(server);
			String serverUri = uriServerBuilder.build();
			servers.add(serverUri);
		}

		String options = null;
		if (config.options != null) {
			List<String> list = new LinkedList<>();
			Iterator<Map.Entry<String, Object>> it = config.options.entrySet().iterator();
			for (; it.hasNext(); ) {
				Map.Entry<String, Object> entry = it.next();
				list.add(String.join("=", entry.getKey(), entry.getValue().toString()));
			}
			options = String.join("&", list);
		}
		String uri = String.join("@", credential,
				String.join(",", servers)
		);

		if (options != null) {
			uri = String.join("?", uri, options);
		}

		return new MongoClientURI("mongodb://" + uri);
	}

	/**
	 * Puts all configuration to the inside holder in the builder.
	 * These configurations are used when the {@link this#build()} is called.
	 *
	 * @param configs The provided configurations
	 * @return {@link this} builder.
	 */
	public MongoURIBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	/**
	 * Sets the username for mongodb
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoURIBuilder}.
	 */
	public MongoURIBuilder withUsername(String value) {
		this.configs.put(USERNAME_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the password for mongodb
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoURIBuilder}.
	 */
	public MongoURIBuilder withPassword(String value) {
		this.configs.put(PASSWORD_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets an option for mongodb
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoURIBuilder}.
	 */
	public MongoURIBuilder withOption(Map.Entry<String, Object> value) {
		Map<String, Object> options;
		if (!this.configs.containsKey(OPTIONS_CONFIG_KEY)) {
			options = new HashMap<>();
			this.configs.put(OPTIONS_CONFIG_KEY, options);
		} else {
			options = (Map<String, Object>) this.configs.get(OPTIONS_CONFIG_KEY);
		}
		options.put(value.getKey(), value.getValue());
		return this;
	}

	/**
	 * Sets the server for mongodb
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoURIBuilder}.
	 */
	public MongoURIBuilder withServer(Map<String, Object> value) {
		List<Map<String, Object>> servers;
		if (!this.configs.containsKey(SERVERS_CONFIG_KEY)) {
			servers = new LinkedList<>();
			this.configs.put(SERVERS_CONFIG_KEY, servers);
		} else {
			servers = (List<Map<String, Object>>) this.configs.get(SERVERS_CONFIG_KEY);
		}
		servers.add(value);
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
