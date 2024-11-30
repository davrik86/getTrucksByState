import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TelegramTruckBot extends TelegramLongPollingBot {
    private final Set<Long> allowedUsers = new HashSet<>();

    public TelegramTruckBot() {
        allowedUsers.add(1039376742L);  // Master
        allowedUsers.add(120146603L);  // Bakhtiyorivich
        allowedUsers.add(6148859532L); // Ismoil
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long userId = update.getMessage().getFrom().getId();

            if (!allowedUsers.contains(userId)) {
                sendMessage(update.getMessage().getChatId(), "You are not authorized to use this bot.");
                return;
            }

            String messageText = update.getMessage().getText();
            if (messageText.equals("/gettrucks")) {
                sendMessage(update.getMessage().getChatId(), "Please give the state you are looking for trucks in.");
            } else if (isValidState(messageText)) {
                List<String[]> trucks = null;
                try {
                    trucks = Truckavailable.processTruckData(messageText);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                sendTruckData(update.getMessage().getChatId(), trucks);
            }
        }
    }

    private boolean isValidState(String state) {
        return state.matches("^[A-Z]{2}$");
    }

    private void sendTruckData(Long chatId, List<String[]> trucks) {
        StringBuilder message = new StringBuilder("Available trucks:\n");

        for (String[] truck : trucks) {
            String truckInfo = "City: " + truck[0] + ", State: " + truck[1] + "\n";

            if (message.length() + truckInfo.length() > 4000) {
                sendMessage(chatId, message.toString());
                message = new StringBuilder();
            }

            message.append(truckInfo);
        }

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
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}