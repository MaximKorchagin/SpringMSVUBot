package msvu.project.SpringMSVUBot.service;

import lombok.extern.slf4j.Slf4j;
import msvu.project.SpringMSVUBot.config.BotConfig;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
@Component
@Slf4j
public class ClassForDB {
    final BotConfig config;
    Connection connection;

    public ClassForDB(BotConfig config) {
        this.config = config;
        try {
            connection = DriverManager.getConnection(config.getDbUrl(), config.getDbUsername(), config.getDbPassword());
        } catch (SQLException e) {
            log.error("Error occurred :" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void addUserToDB(long chatId, long userId, String fio) {
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
            log.error("Error occurred :" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean checkIfUserExist(long userId) {
        try {
            String sql = "select userid from users where userid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, userId);
            ResultSet result = preparedStatement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            log.error("Error occurred :" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

//    private void changeTaskStatusToDone(long chatId, long userId, String messageText) {
//        try {
//            String sql = "update task set state = 'DONE' where id = ?";
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            int taskId = Integer.parseInt(messageText);
//            preparedStatement.setInt(1, taskId);
//            preparedStatement.executeUpdate();
//            map.put(userId, LastQuestionAsked.NOTHING);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public ArrayList<String> findAllTasks() {
        try {
            Statement statement = connection.createStatement();
            String SQL_SELECT_TASKS = "select * from task order by id";
            ResultSet result = statement.executeQuery(SQL_SELECT_TASKS);
            ArrayList<String> tasks = new ArrayList<>();
            while (result.next()) {
                tasks.add(result.getInt("id")
                        + " " + result.getString("name")
                        + " " + result.getString("state"));
            }
            return tasks;
        } catch (SQLException e) {
            log.error("Error occurred :" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void createTask(long chatId, long userId, String messageText) {
        try {
            String sql = "insert into task (name, state) values (?, 'IN_PROGRESS');";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, messageText);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error occurred :" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
