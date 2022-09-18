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
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "g7Jxm4Pwd";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/testDB";

    public static final long ADMINCHATID = 426707306;
    final BotConfig config;

    public TelegramBot(BotConfig config) throws SQLException {
        this.config = config;
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    Map<Long, LastQuestionAsked> map = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            long userId = update.getMessage().getChat().getId();
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (checkIfUserExist(userId) && !map.containsKey(userId)) {
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
                        addUserToDB(chatId, userId, fio);
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
                        createTask(chatId, userId, update.getMessage().getText());
                        break;
                    case NOTHING:
                        switch (messageText) {
                            case "/dotask":
                                printBD(chatId, userId);
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

//            if (!map.containsKey(userId)) {
//                switch (messageText) {
//                    case "/start":
//                        initUser(chatId, userId, update);
//                        break;
//                case "/dotask":
//                    printBD(chatId, userId);
//                    default:
//                        sendMessage(chatId, "Sorry, command was not recognized");
//                }
//            } else {
//                switch (map.get(userId)) {
//                    case WHAT_IS_YOUR_NAME:
//                        String fio = update.getMessage().getText();
//                        sendMessage(chatId, fio);
//                        map.put(userId, LastQuestionAsked.NOTHING);
//                        break;
//                    case DB_PRINTED:
//                        sendMessage(chatId, "Вы получили базу данных. Какой молодец");
//                        break;
//                }
//            }

//        }

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

    private void addUserToDB(long chatId, long userId, String fio) {
        try {

            String sql = "insert into users (name, userid) values (?, ?);";
            String sql2 = "select userid from users where userid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setLong(1, userId);
            //preparedStatement2.execute();
            ResultSet zalupa = preparedStatement2.executeQuery();
            if (!zalupa.next()) {
                preparedStatement.setString(1, fio);
                preparedStatement.setLong(2, userId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkIfUserExist(long userId) {
        try {
            String sql = "select userid from users where userid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, userId);
            ResultSet result = preparedStatement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    //TODO vvv

    private void changeTaskStatusToDone(long chatId, long userId, String messageText) {
        try {
            String sql = "update task set state = 'DONE' where id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            int taskId = Integer.parseInt(messageText);
            preparedStatement.setInt(1, taskId);
            preparedStatement.executeUpdate();
            map.put(userId, LastQuestionAsked.NOTHING);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void printBD(long chatId, long userId) {
        try {
            Statement statement = connection.createStatement();
            String SQL_SELECT_TASKS = "select * from task order by id";
            ResultSet result = statement.executeQuery(SQL_SELECT_TASKS);
            while (result.next()) {
                sendMessage(chatId, result.getInt("id")
                        + " " + result.getString("name")
                        + " " + result.getString("state"));
                map.put(userId, LastQuestionAsked.NOTHING);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTask(long chatId, long userId, String messageText) {
        try {
            String sql = "insert into task (name, state) values (?, 'IN_PROGRESS');";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            String taskName = messageText;
            preparedStatement.setString(1, taskName);
            preparedStatement.executeUpdate();
            sendMessage(chatId, "Вы успешно добавили задачу: " + taskName);
            map.put(userId, LastQuestionAsked.NOTHING);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        //SendAnimation animation = new SendAnimation();
        //animation.setChatId(chatId);
        //animation.setAnimation(new InputFile("CgACAgIAAxkBAAIFqGMl6RNYxoC8skTLr4uxgVHpBr91AAJHGAACTHlgSg8bDqbiWzocKQQ"));
        try {
            execute(message);
            //execute(animation);
        } catch (TelegramApiException e) {
            //boolean sex = true;
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
