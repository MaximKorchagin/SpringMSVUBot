package msvu.project.SpringMSVUBot.service;


import msvu.project.SpringMSVUBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    public static final long ADMINCHATID = 426707306;
    final BotConfig config;
    final ClassForDB classForDB;


    public TelegramBot(BotConfig config, ClassForDB classForDB) throws SQLException {
        this.config = config;
        this.classForDB = classForDB;
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    Map<Long, LastQuestionAsked> map = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            long userId = update.getMessage().getChat().getId();
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (classForDB.checkIfUserExist(userId) && !map.containsKey(userId)) {
                map.put(userId, LastQuestionAsked.NOTHING);
            }


            if (!map.containsKey(userId) && messageText.equals("/start")) {
                initUser(chatId, userId, update);
            } else if (!map.containsKey(userId) && update.getMessage().hasText()) {
                sendMessage(chatId, "Чтобы начать взаимодействие с ботом Вы должны ввести команду /start");
            } else {
                switch (map.get(userId)) {
                    case WHAT_IS_YOUR_NAME:
                        String fio = update.getMessage().getText();
                        classForDB.addUserToDB(chatId, userId, fio);
                        sendMessage(chatId, "Вы ввели ваше имя) - " + fio);
                        map.put(userId, LastQuestionAsked.NOTHING);
                        break;
                    case DB_PRINTED:
                        //sendMessage(chatId, "Вы получили базу данных. Какой молодец");
                        //map.put(userId, LastQuestionAsked.NOTHING);
                        //createTask(chatId, userId, update.getMessage().getText());
                        //TODO changeTaskStatus(chatId, userId, update.getMessage().getText());
                        //printBD(chatId, userId);
                        break;
                    case DB_ADD_REQUEST:
                        classForDB.createTask(chatId, userId, update.getMessage().getText());
                        sendMessage(chatId, "Вы успешно добавили задачу: " + update.getMessage().getText());
                        map.put(userId, LastQuestionAsked.NOTHING);
                        break;
                    case NOTHING:
                        switch (messageText) {
                            case "/dotask":
                                ArrayList<String> stringArrayList = classForDB.findAllTasks();
                                for (String s : stringArrayList) {
                                    sendMessage(chatId, s);
                                }
                                map.put(userId, LastQuestionAsked.NOTHING);
                                sendMessage(chatId, "Вы получили базу данных. Какой молодец!");
                                break;
                            case "/addtask":
                                sendMessage(chatId, "Введите задачу, которую вы хотите добавить в список задач");
                                map.put(userId, LastQuestionAsked.DB_ADD_REQUEST);
                                break;
                            case "/start":
                                sendMessage(chatId, "Вы уже зарегистрированы");
                                break;
                            case "/floppa":
                                sendFloppa(chatId);
                                break;
                            default:
                                sendMessage(chatId, "Sorry, command was not recognized");
                                break;
                        }
                        break;
                    default:
                        sendMessage(chatId, "Sorry, command was not recognized");
                }
            }
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you!";
        sendMessage(chatId, answer);
    }

    private void initUser(long chatId, long userId, Update update) {
        startCommandReceived(chatId, update
                .getMessage()
                .getChat()
                .getFirstName());
        //TODO vvv
        tryToGetChatId(chatId, update
                .getMessage()
                .getChatId()
                .toString());
        getChatIdFromAnyUser(update
                        .getMessage()
                        .getChat()
                        .getFirstName(),
                update.getMessage().getChatId().toString());
        sendMessage(chatId, "Введите, пожалуйста, ФИО полностью, например \n" +
                "Иванова Мария Ивановна");
        map.put(userId, LastQuestionAsked.WHAT_IS_YOUR_NAME);

    }


    private void tryToGetChatId(long chatId, String raz) {
        String answer = "Ваш чат айди: " + raz + " отправлен администратору.";
        sendMessage(chatId, answer);
    }

    private void getChatIdFromAnyUser(String name, String test) {
        String answer = "Айди пользователя " + name + " получен: " + test;
        sendMessage(TelegramBot.ADMINCHATID, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        try {
            execute(message);
            //execute(animation);
        } catch (TelegramApiException e) {
        }
    }

    private void sendFloppa(long chatId) {
        SendAnimation animation = new SendAnimation();
        animation.setChatId(chatId);
        animation.setAnimation(new InputFile("CgACAgIAAxkBAAIFqGMl6RNYxoC8skTLr4uxgVHpBr91AAJHGAACTHlgSg8bDqbiWzocKQQ"));
        try {
            execute(animation);
        } catch (TelegramApiException e) {
        }
    }
}
