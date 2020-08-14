package com.balazskreith.vcollections.builders;

import io.lettuce.core.RedisURI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class RedisURIBuilder extends AbstractBuilder {

	public static final String SOCKET_CONFIG_KEY = "socket";
	public static final String LOCALHOST_CONFIG_KEY = "localhost";
	public static final String PORT_CONFIG_KEY = "port";
	public static final String SENTINEL_MASTER_ID_CONFIG_KEY = "sentinelMasterId";
	public static final String PASSWORD_CONFIG_KEY = "password";
	public static final String DATABASE_CONFIG_KEY = "database";
	public static final String CLIENT_NAME_CONFIG_KEY = "clientName";
	public static final String SSL_CONFIG_KEY = "ssl";
	public static final String VERIFY_PEER_CONFIG_KEY = "verifyPeer";
	public static final String START_TLS_CONFIG_KEY = "startTls";
	public static final String TIMEOUT_CONFIG_KEY = "timeout";
	public static final String SENTINEL_CONFIGURATION_CONFIG_KEY = "sentinelsConfiguration";

	/**
	 * Puts all configuration to the inside holder in the builder.
	 * These configurations are used when the {@link this#build()} is called.
	 *
	 * @param configs The provided configurations
	 * @return {@link this} builder.
	 */
	public RedisURIBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	/**
	 * Sets socket to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withSocket(String value) {
		this.configs.put(SOCKET_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets localhost to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withLocalhost(String value) {
		this.configs.put(LOCALHOST_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets port to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withPort(Short value) {
		this.configs.put(PORT_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets sentinel master id to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withSentinelMasterID(String value) {
		this.configs.put(SENTINEL_MASTER_ID_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets password to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withPassword(String value) {
		this.configs.put(PASSWORD_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets database name to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withDatabase(Integer value) {
		this.configs.put(DATABASE_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets client name to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withClientName(String value) {
		this.configs.put(CLIENT_NAME_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets ssl has to be used to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withSSL(Boolean value) {
		this.configs.put(SSL_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets if peer need to be verified to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withVerifyPeerConfig(Boolean value) {
		this.configs.put(VERIFY_PEER_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets if tls will be started tls to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withStartTLS(Boolean value) {
		this.configs.put(START_TLS_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets timeout to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withTimeout(Duration value) {
		this.configs.put(TIMEOUT_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets a Sentinel to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link RedisURIBuilder}.
	 */
	public RedisURIBuilder withSentinel(Map<String, Object> value) {
		this.configs.put(SENTINEL_CONFIGURATION_CONFIG_KEY, value);
		return this;
	}


	/**
	 * Build a {@link RedisURIBuilder}
	 *
	 * @return A {@link RedisURI} that built.
	 */
	public RedisURI build() {
		Config config = this.convertAndValidate(Config.class);
		RedisURI.Builder resultBuilder;
		if (config.socket != null) {
			resultBuilder = RedisURI.Builder.socket(config.socket);
		} else {
			resultBuilder = RedisURI.builder();
		}

		resultBuilder.withHost(config.host)
				.withPort(config.port);

		if (config.sentinelMasterId != null) {
			resultBuilder.withSentinelMasterId(config.sentinelMasterId);
		}
		if (config.password != null) {
			resultBuilder.withPassword(config.password);
		}
		if (config.database != null) {
			resultBuilder.withDatabase(config.database);
		}
		if (config.ssl != null) {
			resultBuilder.withSsl(config.ssl);
		}
		if (config.clientName != null) {
			resultBuilder.withClientName(config.clientName);
		}
		if (config.verifyPeer != null) {
			resultBuilder.withVerifyPeer(config.verifyPeer);
		}
		if (config.startTls != null) {
			resultBuilder.withStartTls(config.startTls);
		}
		if (config.timeout != null) {
			resultBuilder.withTimeout(config.timeout);
		}
		if (config.sentinelsConfiguration != null) {
			config.sentinelsConfiguration.stream().forEach(
					configuration -> {
						RedisURIBuilder sentinelBuilder = new RedisURIBuilder();
						sentinelBuilder.withConfiguration(configuration);
						RedisURI sentinel = sentinelBuilder.build();
						resultBuilder.withSentinel(sentinel);
					}
			);
		}

		RedisURI result = resultBuilder.build();
		return result;
	}

	public static class Config {

		public String host = "localhost";

		@Min(0)
		@Max(65535)
		public int port = 6379;

		public String socket = null;
		public String sentinelMasterId = null;
		public String password = null;
		public Integer database = null;
		public String clientName = null;
		public Boolean ssl = null;
		public Boolean verifyPeer = null;
		public Boolean startTls = null;
		public Duration timeout = null;
		public List<Map<String, Object>> sentinelsConfiguration = null;
	}
}
