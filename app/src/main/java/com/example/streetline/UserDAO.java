package com.example.streetline;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.example.streetline.Route;
import com.example.streetline.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    private static final String URL = "jdbc:postgresql://10.0.2.2:5432/postgres";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "1234";

    private static final String SQL_QUERY_SELECT_USER = "SELECT user_login, user_password FROM streetline.user_table WHERE user_login = ? AND user_password = ?";
    private static final String SQL_QUERY_SELECT_LOGIN = "SELECT user_login, user_password FROM user_table WHERE user_login = ?";
    private static final String SQL_QUERY_INSERT_USER = "INSERT INTO user_table(user_id, user_login,user_password) VALUES(?,?,?)";

    private DBHelper dbHelper;

    private SQLiteDatabase database;

    public UserDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }


    public void singIn() {

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_QUERY_SELECT_USER);

            if (!resultSet.isBeforeFirst())
                throw new Exception("Пользователь не найден");
            else {
                while (resultSet.next()) {
                    String login = resultSet.getString("user_login");

                    String password = resultSet.getString("user_password");
                    User user = new User(login, password);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void signUp(User user) throws AlreadyExistLoginException {

        database = dbHelper.getReadableDatabase();

        String[] args = {user.getLogin()};

        Cursor dbCursor = database.rawQuery(SQL_QUERY_SELECT_LOGIN, args);

        if (dbCursor.getCount() == 1) {
            throw new AlreadyExistLoginException("Пользователь уже существует");
        }

        database.close();

        database = dbHelper.getWritableDatabase();

        SQLiteStatement statement = database.compileStatement(SQL_QUERY_INSERT_USER);

        statement.bindString(1, user.getUserid());
        statement.bindString(2, user.getLogin());
        statement.bindString(3, user.getPassword());
        statement.executeInsert();

//        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
//            PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY_SELECT_LOGIN);
//            preparedStatement.setString(1,user.getLogin());
//            ResultSet resultSet = preparedStatement.executeQuery();
//            if (resultSet.isBeforeFirst()) {
//                throw new AlreadyExistLoginException("Пользователь уже существует");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
//            PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY_INSERT_USER);
//            preparedStatement.setString(1,user.getUserid());
//            preparedStatement.setString(2,user.getLogin());
//            preparedStatement.setString(3,user.getPassword());
//            preparedStatement.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }
}
