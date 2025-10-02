package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;
import java.sql.ResultSet;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest extends BaseTest{

    private static String jsessionId;
    private Response response;
    boolean tableExist = false;

    @BeforeAll
    static void initSession() {
        jsessionId =
                given()
                        .baseUri("http://localhost:8080")
                        .when()
                        .get("/food")
                        .then()
                        .statusCode(200)
                        .extract()
                        .cookie("JSESSIONID");

        System.out.println("Получен JSESSIONID = " + jsessionId);
    }

    @Test
    void correctDataTest() {
        response = addProduct("Кихкххкхххкихикх", "FRUIT", "false");
        response.then().log().all();
        checkSuccessfullRequest(response);
    }

    @Test
    void copyDataTest() {
        response = addProduct("Малинки", "FRUIT", "false");
        response = addProduct("Малинки", "FRUIT", "false");
        response.then().log().all();
        checkFailRequest(response);
    }

    @Test
    void withoutDataTest() {
        response = addProduct("", "", "");
        response.then().log().all();
        checkFailRequest(response);

    }

    @Test
    void withSpaceInNameTest() {
        response = addProduct(" ", "FRUIT", "false");
        response.then().log().all();
        checkFailRequest(response);
    }
    @Test
    void withActiveExoticTest() {
        response = addProduct("Радужная капуста", "FRUIT", "true");
        response.then().log().all();
        checkSuccessfullRequest(response);
    }
    @Test
    void sqlQueryTest() {
        //создание тестовой таблицы для удаления
        String queryCreateTable = "CREATE TABLE test_table (\n" +
                "    id INT\n" +
                ");";
        //поиск тестовой таблицы
        String queryCheckTable = "SELECT COUNT(*) AS cnt\n" +
                "FROM INFORMATION_SCHEMA.TABLES\n" +
                "WHERE TABLE_SCHEMA = 'PUBLIC'\n" +
                "  AND TABLE_NAME = 'test_table';\n";

        // создаём пустую тестовую таблицу для удаления
        try{
            statement.executeUpdate(queryCreateTable);
            tableExist = true;
            System.out.println("Таблица создана");
        } catch (SQLException sqlExc) {
            System.err.println("Не удалось создать таблицу: " + sqlExc.getMessage());
        }

        //выполняем запрос
        response = addProduct("вишенка', 'FRUIT', false); DROP TABLE test_table; --", "FRUIT", "false");
        response.then().log().all();

        try(ResultSet resultSet = statement.executeQuery(queryCheckTable)) {
            if (resultSet.next()) {
                tableExist = resultSet.getInt("cnt") > 0;;
            }
        } catch (SQLException sqlExc) {
            System.err.println("Возникла ошибка при поиске таблицы: " + sqlExc.getMessage());
        }

        //удаляем, если таблица не была удалена инъекцией,
        if (tableExist){
            try{
                statement.executeUpdate("DROP TABLE test_table");
                System.out.println("Таблица удалена");
            } catch (SQLException sqlExc) {
                System.err.println("Возникла ошибка при удалении таблицы: " + sqlExc.getMessage());
            }
        }

        //проверяем что запрос был провален
        // и таблица существует (tableExist == true)
        assertAll(
                "Проверка что запрос провален и что таблица существует",
                () -> assertTrue( tableExist, "Таблицы не существует!"),
                () -> checkFailRequest(response)
        );
    }

    @Test
    void testingGetListOfProduct() {
        given()
                .baseUri("http://localhost:8080")
                .cookie("JSESSIONID", jsessionId)
                .accept("application/json")
                .when()
                .get("/api/food")
                .then()
                .statusCode(200)
                .log().all();
    }



    Response addProduct(String name, String type, String exotic) {
        return given()
                .baseUri("http://localhost:8080")
                .cookie("JSESSIONID", jsessionId)
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \""+ name +"\", \"type\": \""+ type +"\", \"exotic\": "+ exotic +"}")
                .when()
                .post("/api/food"); // должно работать
    }

    void checkSuccessfullRequest(Response response){
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300,
                "Ожидался успешный код, а пришёл " + response.statusCode());
    }
    void checkFailRequest(Response response){
        assertTrue(response.statusCode() >= 400 && response.statusCode() < 600,
                "Ожидался код ошибки, а пришёл " + response.statusCode());
    }

}
