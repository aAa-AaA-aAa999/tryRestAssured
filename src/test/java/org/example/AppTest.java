package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class AppTest {

    private static String jsessionId;

    @BeforeAll
    static void initSession() {
        jsessionId =
                given()
                        .baseUri("https://drt-chameleon.ibs.ru")
                        .when()
                        .get("/food")
                        .then()
                        .statusCode(200)
                        .extract()
                        .cookie("JSESSIONID");

        System.out.println("Получен JSESSIONID = " + jsessionId);
    }

    @Test
    void testAddProduct() {
        given()
                .baseUri("https://drt-chameleon.ibs.ru")
                .cookie("JSESSIONID", jsessionId)
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \"Кикикиик\", \"type\": \"FRUIT\", \"exotic\": false}")
                .when()
                .post("/api/food")
                .then()
                .statusCode(200)
                .log().all();
    }

    @Test
    void testGetAllProducts() {
        given()
                .baseUri("https://drt-chameleon.ibs.ru")
                .cookie("JSESSIONID", jsessionId)
                .accept("application/json")
                .when()
                .get("/api/food")
                .then()
                .statusCode(200)
                .log().all();
    }
}
