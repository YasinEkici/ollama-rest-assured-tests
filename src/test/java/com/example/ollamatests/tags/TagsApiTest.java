package com.example.ollamatests.tags;

import com.example.ollamatests.BaseTest;
import com.example.ollamatests.client.Endpoints;
import com.example.ollamatests.model.tags.TagsResponse;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("regression")
class TagsApiTest extends BaseTest {

    @Test
    @Tag("smoke")
    void shouldReturn200WhenListingModels() {
        given()
                .when()
                .get(Endpoints.TAGS)
                .then()
                .statusCode(200);
    }

    @Test
    void shouldReturnJsonContentType() {
        given()
                .when()
                .get(Endpoints.TAGS)
                .then()
                .contentType(ContentType.JSON);
    }

    @Test
    void shouldContainModelsArray() {
        given()
                .when()
                .get(Endpoints.TAGS)
                .then()
                .body("models", notNullValue())
                .body("models", instanceOf(List.class));
    }

    @Test
    void shouldRespondUnderTimeout() {
        given()
                .when()
                .get(Endpoints.TAGS)
                .then()
                .time(lessThan(5_000L));
    }

    @Test
    void shouldDeserializeToTagsResponse() {
        TagsResponse response = given()
                .when()
                .get(Endpoints.TAGS)
                .then()
                .statusCode(200)
                .extract()
                .as(TagsResponse.class);

        assertNotNull(response);
        assertNotNull(response.getModels());
    }
}
