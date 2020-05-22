import com.jayway.jsonpath.JsonPath;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.*;

import org.junit.jupiter.api.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.*;

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
                        request("GET", "https://api.openweathermap.org/data/2.5/onecall").
                        then().
                        spec(responseSpec).
                        extract().
                        response().asString();

        String timezone = JsonPath.parse(json).read("timezone");
        assertEquals("Europe/Samara", timezone, "invalid timezone");

    }

    //Проверяем, что температура в ближайший час больше 20 градусов
    @Test
    @DisplayName("Temperature in the next hour is above 20")
    public void CityIsRight() {
        Response response =
                given().
                        param("lat","56.866557").
                        param("lon", "53.2094166").
                        param("appid", token).
                        param("units", "metric").

                when().
                        request("GET", "https://api.openweathermap.org/data/2.5/onecall").
                        then().
                        spec(responseSpec).
                        extract().
                        response();
        float temperature = response.jsonPath().get("hourly.temp[0]");
        assertTrue(temperature > 0, "It's cold in the next hour " + temperature);
    }

    //Проверяем, что если исключили какие-то параметры - их нет
    /*@Test
    @DisplayName("Temperature in the next hour is above 20")
    public void ExcludeParameters() {
        Response response =
                when().
                        request("GET", "https://api.openweathermap.org/data/2.5/onecall?lat=56.866557&lon=53.2094166&exclude=current,minutely&appid=2c491e00c21e55d2c412714b1eacf7d6&units=metric").
                        then().
                        assertThat().statusCode(200).
                        contentType("application/json").
                        extract().
                        response();

        Float temperature = response.jsonPath().get("hourly.temp[1]");
        //assertTrue(temperature > 20, "It's cold in the next hour " + temperature);
    }*/
//еще добавить может регион ру и тд проверки
    //какую-нибудь ошибку?
    //вынести данные в файл?

    //get("/lotto").then().body("lotto.lottoId", equalTo(5));

}