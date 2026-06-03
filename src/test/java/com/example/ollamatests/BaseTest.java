package com.example.ollamatests;

import com.example.ollamatests.client.OllamaSpecs;
import com.example.ollamatests.config.TestConfig;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseTest {

    // Ortak altyapı sınıfı; tek başına test olarak çalıştırılmaması için abstract tutulur.
    @BeforeAll
    static void setup() {
        RestAssured.baseURI = TestConfig.getBaseUrl();
        RestAssured.requestSpecification = OllamaSpecs.defaultRequestSpec();
        RestAssured.responseSpecification = OllamaSpecs.defaultResponseSpec();
    }
}
