package com.example.ollamatests.generate;

import com.example.ollamatests.BaseTest;
import com.example.ollamatests.client.Endpoints;
import com.example.ollamatests.config.TestConfig;
import com.example.ollamatests.model.generate.GenerateRequest;
import com.example.ollamatests.model.generate.GenerateResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenerateApiTest extends BaseTest {

    private static final Map<String, Object> DETERMINISTIC_OPTIONS = Map.of(
            "temperature", 0,
            "seed", 42
    );

    @Test
    @Tag("smoke")
    void shouldGenerateResponseForSimplePrompt() {
        given()
                .body(defaultRequest("Return one short sentence about automated API testing."))
                .when()
                .post(Endpoints.GENERATE)
                .then()
                .statusCode(200)
                .body("response", notNullValue())
                .body("response", not(emptyString()));
    }

    @Test
    void shouldReturnDoneTrue() {
        given()
                .body(defaultRequest("Return a short phrase."))
                .when()
                .post(Endpoints.GENERATE)
                .then()
                .statusCode(200)
                .body("done", equalTo(true));
    }

    @Test
    void shouldReportEvalCountGreaterThanZero() {
        given()
                .body(defaultRequest("Return a short phrase."))
                .when()
                .post(Endpoints.GENERATE)
                .then()
                .statusCode(200)
                .body("eval_count", greaterThan(0));
    }

    @Test
    void shouldReturnModelNameInResponse() {
        given()
                .body(defaultRequest("Return a short phrase."))
                .when()
                .post(Endpoints.GENERATE)
                .then()
                .statusCode(200)
                .body("model", equalTo(TestConfig.getTestModel()));
    }

    @Test
    void shouldRespondUnderTimeout() {
        given()
                .body(defaultRequest("Return a short phrase."))
                .when()
                .post(Endpoints.GENERATE)
                .then()
                .time(lessThan(TestConfig.getTimeoutMs()));
    }

    @Test
    void shouldHandleDeterministicOptions() {
        GenerateResponse response = given()
                .body(defaultRequest("Return a short phrase."))
                .when()
                .post(Endpoints.GENERATE)
                .then()
                .statusCode(200)
                .body("done", equalTo(true))
                .body("response", notNullValue())
                .body("context", not(nullValue()))
                .extract()
                .as(GenerateResponse.class);

        assertNotNull(response);
        assertEquals(Boolean.TRUE, response.getDone());
        assertNotNull(response.getResponse());
    }

    private GenerateRequest defaultRequest(String prompt) {
        return new GenerateRequest(TestConfig.getTestModel(), prompt, false, DETERMINISTIC_OPTIONS);
    }
}
