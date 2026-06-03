package com.example.ollamatests.chat;

import com.example.ollamatests.BaseTest;
import com.example.ollamatests.client.Endpoints;
import com.example.ollamatests.config.TestConfig;
import com.example.ollamatests.model.chat.ChatRequest;
import com.example.ollamatests.model.chat.Message;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEmptyString.emptyString;

@Tag("regression")
class ChatApiTest extends BaseTest {

    private static final Map<String, Object> DETERMINISTIC_OPTIONS = Map.of(
            "temperature", 0,
            "seed", 42
    );

    @Test
    @Tag("smoke")
    void shouldReturnAssistantMessageForUserPrompt() {
        given()
                .body(defaultRequest(List.of(new Message("user", "Return one short sentence about API testing."))))
                .when()
                .post(Endpoints.CHAT)
                .then()
                .statusCode(200)
                .body("message.role", equalTo("assistant"));
    }

    @Test
    void shouldReturnNonEmptyContent() {
        given()
                .body(defaultRequest(List.of(new Message("user", "Return one short sentence about API testing."))))
                .when()
                .post(Endpoints.CHAT)
                .then()
                .statusCode(200)
                .body("message.content", notNullValue())
                .body("message.content", not(emptyString()));
    }

    @Test
    void shouldHandleMultiTurnConversation() throws IOException {
        given()
                .body(readPayload("payloads/chat-multi-turn.json"))
                .when()
                .post(Endpoints.CHAT)
                .then()
                .statusCode(200)
                .body("message.role", equalTo("assistant"))
                .body("message.content", notNullValue());
    }

    @Test
    void shouldRespondUnderTimeout() {
        given()
                .body(defaultRequest(List.of(new Message("user", "Return one short sentence about API testing."))))
                .when()
                .post(Endpoints.CHAT)
                .then()
                .time(lessThan(TestConfig.getTimeoutMs()));
    }

    @Test
    void shouldReturnDoneTrue() {
        given()
                .body(defaultRequest(List.of(new Message("user", "Return one short sentence about API testing."))))
                .when()
                .post(Endpoints.CHAT)
                .then()
                .statusCode(200)
                .body("done", equalTo(true));
    }

    private ChatRequest defaultRequest(List<Message> messages) {
        return new ChatRequest(TestConfig.getTestModel(), messages, false, DETERMINISTIC_OPTIONS);
    }

    private String readPayload(String resourceName) throws IOException {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException("Payload not found: " + resourceName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
