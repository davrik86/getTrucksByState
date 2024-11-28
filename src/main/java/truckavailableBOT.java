import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TelegramTruckBot extends TelegramLongPollingBot {
    // Set of allowed user IDs for access control
    private final Set<Long> allowedUsers = new HashSet<>();

    public TelegramTruckBot() {
        // Add allowed user IDs here
        allowedUsers.add(1039376742L);  // Master
        allowedUsers.add(120146603L);  // Bakhtiyorivich
        allowedUsers.add(6148859532L);//Ismoil


    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long userId = update.getMessage().getFrom().getId();

            // Check if the user is authorized
            if (!allowedUsers.contains(userId)) {
                sendMessage(update.getMessage().getChatId(), "You are not authorized to use this bot.");
                return;
            }

            String messageText = update.getMessage().getText();
            if (messageText.equals("/gettrucks")) {
                sendMessage(update.getMessage().getChatId(), "Please give the state you are looking for trucks in.");
            } else if (isValidState(messageText)) {
                // Use the messageText as the state
                List<String[]> trucks = null;
                try {
                    trucks = processTruckData(messageText);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                sendTruckData(update.getMessage().getChatId(), trucks);
            }
        }
    }



    private boolean isValidState(String state) {
        // Validate that the input is a correct state abbreviation
        return state.matches("^[A-Z]{2}$");
    }

    private List<String[]> processTruckData(String state) throws InterruptedException {
        // Call your method to fetch truck data for the provided state
        return truckavailableBOT.processTruckData(state);
    }

    private void sendTruckData(Long chatId, List<String[]> trucks) {
        StringBuilder message = new StringBuilder("Available trucks:\n");

        for (String[] truck : trucks) {
            String truckInfo = "City: " + truck[0] + ", State: " + truck[1] + "\n";

            // Check if appending the next truck info would exceed the message length limit
            if (message.length() + truckInfo.length() > 4000) {
                sendMessage(chatId, message.toString());
                message = new StringBuilder(); // Start a new message part
            }

            message.append(truckInfo);
        }

        // Send the last part of the message if it's not empty
        if (message.length() > 0) {
            sendMessage(chatId, message.toString());
        }
    }

    @Override
    public String getBotUsername() {
        return "truckList_bot";
    }

    @Override
    public String getBotToken() {
        return "7693881433:AAGbtW0ioEz4w2ILSJX6tUcpLpZdvXXdxIs";
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message); // Call the method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

public class truckavailableBOT {

    // The processTruckData method as already defined
    public static List<String[]> processTruckData(String state) throws InterruptedException {
        List<String[]> todayTrucks = new ArrayList<>();
        List<String[]> nextDayTrucks = new ArrayList<>();
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        WebDriver driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        try {
            driver.get("https://teamup.com/login");

            WebElement email = driver.findElement(By.id("email"));
            email.sendKeys("diyorjon.rafikov@sultantrans.com");
            driver.findElement(By.id("_continue")).click();

            WebElement pass = driver.findElement(By.id("password"));
            pass.sendKeys("Friendofdiyor@19");
            driver.findElement(By.id("_submit")).click();

            WebElement diyorBttn = driver.findElement(By.xpath("//div[@class='calendar-box-content']"));
            diyorBttn.click();

            WebElement click= driver.findElement(By.xpath("(//a[@class='header-toggle'])[2]"));
            WebElement coveredTruckBttn = driver.findElement(By.xpath("//div[@class='calendar cal-bg-27']"));
            WebElement maintenanceBttn = driver.findElement(By.xpath("//div[@class='calendar cal-bg-7']"));
            WebElement onHold = driver.findElement(By.xpath("(//div[text()='ON HOLD'])[1]"));
            WebElement partial = driver.findElement(By.xpath("(//div[text()='PARTIAL'])[1]"));

            WebElement filter = driver.findElement(By.xpath("//input[@id='keywords_filter']"));

            // Click the necessary buttons
            maintenanceBttn.click();
            coveredTruckBttn.click();
            onHold.click();
            partial.click();

            // Collect data for today
            todayTrucks = extractTruckData(driver);

            // Click on 'nextDAY' button to move to the next day
            WebElement nextDayButton = driver.findElement(By.xpath("//div[@class='toolbar-button next']"));
            nextDayButton.click();

            // Collect data for the next day
            nextDayTrucks = extractTruckData(driver);

        } finally {
            driver.quit();
        }

        // Combine today's and next day's truck data
        List<String[]> allTrucks = new ArrayList<>();
        allTrucks.addAll(todayTrucks);
        allTrucks.addAll(nextDayTrucks);

        // Filter by state if provided
        if (!state.equalsIgnoreCase("ALL")) {
            allTrucks = filterByState(allTrucks, state);
        }

        return allTrucks;
    }

    public static List<String[]> extractTruckData(WebDriver driver) throws InterruptedException {
        List<String[]> locations = new ArrayList<>();
        Pattern statePattern = Pattern.compile("\\b[A-Z]{2}\\b"); // Regex for valid two-letter state codes

        List<WebElement> emptyColumn = driver.findElements(By.xpath("//div[@class='row-content']/div"));
        List<String> textValues = new ArrayList<>();
        for (WebElement empty : emptyColumn) {
            textValues.add(empty.getText());
        }

        // Process the extracted data
        for (String value : textValues) {
            String[] parts = value.split("\\(");
            if (parts.length > 0) {
                String location = parts[0].trim();
                String[] cityState = location.split(",");

                String city = cityState[0].trim();
                String state = "";

                // Handle case when state is missing (no comma in the text)
                if (cityState.length == 1) {
                    Matcher matcher = statePattern.matcher(city);
                    if (matcher.find()) {
                        state = matcher.group(0);
                        city = city.replace(state, "").trim(); // Remove the state from the city text
                    }
                } else if (cityState.length == 2) {
                    Matcher matcher = statePattern.matcher(cityState[1].trim());
                    if (matcher.find()) {
                        state = matcher.group(0);
                    }
                }

                locations.add(new String[]{city, state}); // Add cleaned city and state to the list
            }
        }

        return locations;
    }
    // filterByState method
    public static List<String[]> filterByState(List<String[]> trucks, String state) {
        List<String[]> filteredTrucks = new ArrayList<>();
        for (String[] truck : trucks) {
            if (truck[1].equalsIgnoreCase(state)) {
                filteredTrucks.add(truck);
            }
        }
        return filteredTrucks;
    }

    // Main method to start the bot
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramTruckBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
