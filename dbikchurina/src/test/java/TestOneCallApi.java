import com.jayway.jsonpath.JsonPath;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.*;

import java.util.LinkedHashMap;
import java.util.List;

public class TestOneCallApi {

    String token = "2c491e00c21e55d2c412714b1eacf7d6";

    ResponseSpecification responseSpec = new ResponseSpecBuilder().
            expectStatusCode(200).expectContentType("application/json").build();

    //Проверяем, что таймзона соответствует координатам
    @Test
    @DisplayName("Timezone matches")
    public void TimeZoneIsRight() {

        String json =
                given().
                        param("lat","56.866557").
                        param("lon", "53.2094166").
                        param("appid", token).
                        param("units", "metric").
                when().
                        get( "https://api.openweathermap.org/data/2.5/onecall").
                        then().
                        spec(responseSpec).
                        extract().
                        response().asString();

        String timezone = JsonPath.parse(json).read("timezone");
        assertEquals("Europe/Samara", timezone, "invalid timezone");

    }

    //Проверяем, что температура в ближайший час больше 20 градусов
    @Test
    @DisplayName("Temperature in the next hour is above 0")
    public void CityIsRight() {
        Response response =
                given().
                        param("lat","56.866557").
                        param("lon", "53.2094166").
                        param("appid", token).
                        param("units", "metric").

                when().
                        get("https://api.openweathermap.org/data/2.5/onecall").
                        then().
                        spec(responseSpec).
                        extract().
                        response();
        float temperature = response.jsonPath().get("hourly.temp[0]");
        assertTrue(temperature > 0, "It's cold in the next hour " + temperature);
    }

    //Проверяем, что если исключили какие-то параметры - их нет
    @Test
    public void ExcludeParameters() {

        String allParam =
                given().
                        param("lat","33.441792").
                        param("lon", "-94.037689").
                        param("appid", token).
                when().
                        get("https://api.openweathermap.org/data/2.5/onecall").
                        then().
                        spec(responseSpec).
                        extract().response().asString();

        LinkedHashMap json = JsonPath.parse(allParam).read("$");
        System.out.println(json);
        assertTrue(json.containsKey("current"), "no current weather");
        assertTrue(json.containsKey("minutely"), "no minutely weather");
        assertTrue(json.containsKey("hourly"), "no hourly weather");
        assertTrue(json.containsKey("daily"), "no daily weather");
    }

}