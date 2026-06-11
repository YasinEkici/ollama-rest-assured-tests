package com.example.ollamatests;

import com.example.ollamatests.client.OllamaSpecs;
import com.example.ollamatests.config.TestConfig;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseTest {

    private static final int FULL_BODY_LIMIT = 200;
    private static final int TRUNCATED_BODY_LENGTH = 150;

    // Ortak altyapı sınıfı; tek başına test olarak çalıştırılmaması için abstract tutulur.
    @BeforeAll
    static void setup() {
        RestAssured.baseURI = TestConfig.getBaseUrl();
        RestAssured.requestSpecification = OllamaSpecs.defaultRequestSpec();
        RestAssured.responseSpecification = OllamaSpecs.defaultResponseSpec();
    }

    protected static String truncateBody(String body) {
        if (body == null || body.length() <= FULL_BODY_LIMIT) {
            return body;
        }

        return body.substring(0, TRUNCATED_BODY_LENGTH) + "...(truncated)";
    }
}
