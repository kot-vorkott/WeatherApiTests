import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestCurrentWeatherApi.class,
        TestOneCallApi.class
})

public class JunitTestSuite {
}