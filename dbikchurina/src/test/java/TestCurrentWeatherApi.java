import com.jayway.jsonpath.JsonPath;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.*;

public class TestCurrentWeatherApi {

    String token = "2c491e00c21e55d2c412714b1eacf7d6";

    ResponseSpecification responseSpec = new ResponseSpecBuilder().
            expectStatusCode(200).expectContentType("application/json").build();

    //Если температура меньше 10 градусов = выводим ассерт.
    @Test
    @DisplayName("Temperature must be above 10")
    public void temperatureIsAbove10() {

        Response response =
                given().
                        param("q","London").
                        param("appid", token).
                        param("units", "metric").
                when().
                        get("http://api.openweathermap.org/data/2.5/weather").
                then().
                        spec(responseSpec).
                        extract().response();

        float weather = response.jsonPath().get("main.temp");
        assertTrue(weather > 10, "it's cold now: " + weather);
    }

    //Проверяем, что город в запросе и город в ответе совпадают.
    @Test
    @DisplayName("Name of the city must match")
    public void cityInResponseMatchesCityInRequest() {

        String json = given().
                param("q","London").
                param("appid", token).
                param("units", "metric").
        when().
                get("http://api.openweathermap.org/data/2.5/weather").
                then().
                spec(responseSpec).
                extract().response().asString();

        String city = JsonPath.parse(json).read("name");
        assertEquals("London", city, "invalid city");

    }

    //Проверяем, что по ID выдается правильный город
    @Test
    @DisplayName("ID and name of the city must match")
    public void CityIDIsRight() {

        Response response =
                given().
                        param("id", "524901").
                        param("appid", token).
                when().
                        request("GET", "http://api.openweathermap.org/data/2.5/weather").
                        then().
                        spec(responseSpec).
                        extract().response();

        String city = response.jsonPath().getString("name");
        int id =  response.jsonPath().getInt("id");
        assertEquals("Moscow", city, "invalid city");
        assertEquals(524901, id, "invalid ID");

    }

    //Проверяем, что выдается нужная ошибка при неправильном API key или его отсутствии.
    @Test
    @DisplayName("Message about invalid API key")
    public void invalidApiKey() {

        given().
                param("id", "524901").
        when().
                get("http://api.openweathermap.org/data/2.5/weather").
                then().
                statusCode(401).
                contentType("application/json").
                body("message", equalTo("Invalid API key. Please see http://openweathermap.org/faq#error401 for more info."));

        given().
                param("id", "524901").
                param("appid", "000");
                get("http://api.openweathermap.org/data/2.5/weather").
                then().
                statusCode(401).
                contentType("application/json").
                body("message", equalTo("Invalid API key. Please see http://openweathermap.org/faq#error401 for more info."));

    }

    //Проверяем, что доступны форматы xml, html
    @Test
    @DisplayName("Format availability")
    public void AllFormatsAreAvailable() {

        given().
                param("q", "Berlin").
                param("appid", token).
                param("mode", "xml").
        when().
                request("GET", "http://api.openweathermap.org/data/2.5/weather").
                then().
                statusCode(200).
                contentType("application/xml");

        given().
                param("q", "Berlin").
                param("appid", token).
                param("mode", "html").
        when().

                request("GET", "http://api.openweathermap.org/data/2.5/weather").
                then().
                statusCode(200).
                contentType("text/html");
    }


    //Проверяем, что при указании в запросе языка город и описание (description и name) на этом языке.
    @Test
    @DisplayName("Language must match")
    public void LanguageIsRight() {

        Pattern pattern = Pattern.compile("[" + "а-яА-ЯёЁ" + "\\h" + "\\p{Punct}" +"]" + "*");

        Response response =
                given().
                        param("q", "Paris").
                        param("appid", token).
                        param("lang","ru").

                when().
                        request("GET", "http://api.openweathermap.org/data/2.5/weather").
                        then().
                        spec(responseSpec).
                        extract().response();

        String city = response.jsonPath().getString("name");
        String description = response.jsonPath().getString("weather.description");
        assertEquals("Париж", city, "invalid city");

        Matcher matcher = pattern.matcher(description);
        assertTrue(matcher.matches(), "Language is not ru");

    }


    //Проверяем, что если город не найден, возвращается понятная ошибка
    @Test
    @DisplayName("City name is invalid")
    public void NoSuchCity() {

        Response response =
                given().
                        param("q", "hghgh").
                        param("appid", token).
                when().
                        request("GET", "http://api.openweathermap.org/data/2.5/weather").
                        then().
                        assertThat().statusCode(404).
                        contentType("application/json").
                        extract().
                        response();
        String message= response.jsonPath().getString("message");
        assertEquals("city not found", message );
    }

}