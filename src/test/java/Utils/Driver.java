package Utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class Driver {
    private static WebDriver driver;

    private Driver () {
    }
    public static WebDriver getDriver(){
        String browserType= dataReader.getPropertyValue("browserType");

        if(driver == null){
            switch (browserType.toLowerCase()){
                case "chrome":
                    driver = new ChromeDriver();
                    break;
                case "chrome-headless":
                    ChromeOptions chromeOptions= new ChromeOptions();
                    chromeOptions.addArguments("--headless");
                    driver= new ChromeDriver(chromeOptions);
                    break;

                case "firefox":
                    driver= new FirefoxDriver();
                    break;
                case "firefox-headless":
                    FirefoxOptions firefoxOptions =new FirefoxOptions();
                    firefoxOptions.addArguments("--headless");
                    driver= new FirefoxDriver(firefoxOptions);
                    break;

                case "edge":
                    driver= new EdgeDriver();
                    break;

                case "safari":
                    driver= new EdgeDriver();
                    break;
                default:
                    driver = new ChromeDriver(); //Instantiate only once
                    break;
            }
        }
        return driver;

    }

    public static void closeDriver(){
        if (driver!=null){
            driver.quit();
            driver = null;
        }
    }
}
