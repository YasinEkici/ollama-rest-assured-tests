package com.example.ollamatests.client;

import com.example.ollamatests.config.TestConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public final class OllamaSpecs {

    private OllamaSpecs() {
    }

    public static RequestSpecification defaultRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(TestConfig.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    public static ResponseSpecification defaultResponseSpec() {
        return new ResponseSpecBuilder()
                .build();
    }
}
