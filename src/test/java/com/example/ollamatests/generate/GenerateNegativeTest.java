package com.example.ollamatests.generate;

import com.example.ollamatests.BaseTest;
import com.example.ollamatests.client.Endpoints;
import com.example.ollamatests.config.TestConfig;
import com.example.ollamatests.model.generate.GenerateRequest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
class GenerateNegativeTest extends BaseTest {

    private static final Map<String, Object> DETERMINISTIC_OPTIONS = Map.of(
            "temperature", 0,
            "seed", 42
    );

    @Test
    void shouldFailForInvalidModelName() {
        GenerateRequest request = new GenerateRequest(
                "non-existent-model-for-regression-tests",
                "Return a short phrase.",
                false,
                DETERMINISTIC_OPTIONS
        );

        Response response = given()
                .body(request)
                .when()
                .post(Endpoints.GENERATE);

        System.out.printf("invalidModel status=%d body=%s%n", response.statusCode(), response.asString());

        assertEquals(404, response.statusCode());
        response.then()
                .body("error", notNullValue())
                .body("error", not(emptyString()));
    }

    @Test
    void shouldHandleEmptyPrompt() {
        GenerateRequest request = new GenerateRequest(
                TestConfig.getTestModel(),
                "",
                false,
                DETERMINISTIC_OPTIONS
        );

        Response response = given()
                .body(request)
                .when()
                .post(Endpoints.GENERATE);

        System.out.printf("emptyPrompt status=%d body=%s%n", response.statusCode(), response.asString());

        assertEquals(200, response.statusCode());
        response.then()
                .body("done", notNullValue())
                .body("response", notNullValue());
    }

    @Test
    void shouldReturn400ForMalformedJson() {
        Response response = given()
                .body("{\"model\":\"" + TestConfig.getTestModel() + "\",\"prompt\":")
                .when()
                .post(Endpoints.GENERATE);

        System.out.printf("malformedJson status=%d body=%s%n", response.statusCode(), response.asString());

        assertEquals(400, response.statusCode());
        response.then()
                .body("error", notNullValue())
                .body("error", not(emptyString()));
    }
}
