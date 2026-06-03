package com.example.ollamatests.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {

    private static final String CONFIG_FILE = "test-config.properties";

    private static final String BASE_URL_PROPERTY = "ollama.base.url";
    private static final String TEST_MODEL_PROPERTY = "ollama.test.model";
    private static final String TIMEOUT_MS_PROPERTY = "ollama.timeout.ms";

    private static final String BASE_URL_ENV = "OLLAMA_BASE_URL";
    private static final String TEST_MODEL_ENV = "OLLAMA_TEST_MODEL";
    private static final String TIMEOUT_MS_ENV = "OLLAMA_TIMEOUT_MS";

    private static final String DEFAULT_BASE_URL = "http://localhost:11434";
    private static final String DEFAULT_TEST_MODEL = "qwen2.5:0.5b";
    private static final long DEFAULT_TIMEOUT_MS = 60_000L;

    private static final Properties PROPERTIES = loadProperties();

    private TestConfig() {
    }

    public static String getBaseUrl() {
        return resolve(BASE_URL_PROPERTY, BASE_URL_ENV, DEFAULT_BASE_URL);
    }

    public static String getTestModel() {
        return resolve(TEST_MODEL_PROPERTY, TEST_MODEL_ENV, DEFAULT_TEST_MODEL);
    }

    public static long getTimeoutMs() {
        String value = resolve(TIMEOUT_MS_PROPERTY, TIMEOUT_MS_ENV, String.valueOf(DEFAULT_TIMEOUT_MS));
        return Long.parseLong(value);
    }

    private static String resolve(String propertyKey, String envKey, String defaultValue) {
        String systemPropertyValue = normalize(System.getProperty(propertyKey));
        if (systemPropertyValue != null) {
            return systemPropertyValue;
        }

        String envValue = normalize(System.getenv(envKey));
        if (envValue != null) {
            return envValue;
        }

        String fileValue = normalize(PROPERTIES.getProperty(propertyKey));
        if (fileValue != null) {
            return fileValue;
        }

        return defaultValue;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = TestConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, exception);
        }

        return properties;
    }
}
