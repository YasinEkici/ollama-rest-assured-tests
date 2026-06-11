package com.example.ollamatests.chat;

import com.example.ollamatests.BaseTest;
import com.example.ollamatests.client.Endpoints;
import com.example.ollamatests.config.TestConfig;
import com.example.ollamatests.model.chat.ChatRequest;
import com.example.ollamatests.model.chat.Message;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
class ChatNegativeTest extends BaseTest {

    private static final Map<String, Object> DETERMINISTIC_OPTIONS = Map.of(
            "temperature", 0,
            "seed", 42
    );

    @Test
    void shouldHandleEmptyMessagesArray() {
        ChatRequest request = new ChatRequest(TestConfig.getTestModel(), List.of(), false, DETERMINISTIC_OPTIONS);

        Response response = given()
                .body(request)
                .when()
                .post(Endpoints.CHAT);

        System.out.printf("emptyMessages status=%d body=%s%n", response.statusCode(), truncateBody(response.asString()));

        assertEquals(200, response.statusCode());
        response.then()
                .body("done", notNullValue())
                .body("message.role", notNullValue())
                .body("message.content", notNullValue());
    }

    @Test
    void shouldHandleInvalidRole() {
        ChatRequest request = new ChatRequest(
                TestConfig.getTestModel(),
                List.of(new Message("invalid_role", "Return one short sentence about API testing.")),
                false,
                DETERMINISTIC_OPTIONS
        );

        Response response = given()
                .body(request)
                .when()
                .post(Endpoints.CHAT);

        System.out.printf("invalidRole status=%d body=%s%n", response.statusCode(), truncateBody(response.asString()));

        assertEquals(200, response.statusCode());
        response.then()
                .body("done", notNullValue())
                .body("message.role", notNullValue())
                .body("message.content", notNullValue());
    }

    @Test
    void shouldFailForMissingModel() {
        Map<String, Object> request = Map.of(
                "messages", List.of(Map.of("role", "user", "content", "Return one short sentence about API testing.")),
                "stream", false,
                "options", DETERMINISTIC_OPTIONS
        );

        Response response = given()
                .body(request)
                .when()
                .post(Endpoints.CHAT);

        System.out.printf("missingModel status=%d body=%s%n", response.statusCode(), truncateBody(response.asString()));

        assertEquals(400, response.statusCode());
        response.then()
                .body("error", notNullValue())
                .body("error", not(emptyString()));
    }
}
