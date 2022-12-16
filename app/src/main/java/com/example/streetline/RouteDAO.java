package com.example.streetline;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RouteDAO {

    private String userid;

//    private static final String URL = "jdbc:postgresql://10.0.2.2:5432/postgres" ;
//
//    private static final String USERNAME = "postgres" ;
//
//    private  static final String PASSWORD = "1234" ;

    private static final String SQL_QUERY_INSERT_ROUTE = "INSERT INTO rating_table(area_id,user_id,score,area_type,feedback) VALUES(?,?,?,?,?)";

    private static final String SQL_QUERY_INSERT_USER = "INSERT INTO user_table(user_id,user_login,user_password) VALUES(?,?,?)";

    private static final String SQL_QUERY_INSERT_LOCATIONS = "INSERT INTO area_table (area_id, area_json, user_id) VALUES (?, ?, ?);";

    private static final String SQL_QUERY_SELECT_LOCATIONS = "SELECT rating_id, rating_table.area_id, rating_table.user_id, score, area_type, feedback, area_json\n" +
            "\tFROM rating_table JOIN area_table \n" +
            "\tON rating_table.area_id = area_table.area_id;";

    private static final String SQL_QUERY_SELECT_LOCATIONS_TEST = "SELECT rating_id, rating_table.area_id, rating_table.user_id, score, area_type, feedback, area_json, user_login " +
            "FROM rating_table INNER JOIN area_table " +
            "ON rating_table.area_id = area_table.area_id " +
            "INNER JOIN user_table " +
            "ON rating_table.user_id = user_table.user_id";

    private static final String SQL_QUERY_SELECT_AVG_SCORE_OF_ROUTE = "SELECT AVG(score) FROM rating_table where area_id = (?)";

    private static final String SQL_QUERY_SELECT_INFO_OF_ROUTE = "SELECT area_id, score, feedback, area_type, user_login FROM rating_table join\n" +
            "user_table on rating_table.user_id = user_table.user_id\n" +
            "where area_id = (?)";

    private static final String SQL_QUERY_SELECT_INFO_OF_ROUTE_WITH_LOGIN  = "SELECT user_login\n" +
            "    \tFROM rating_table JOIN user_table \n" +
            "    \tON rating_table.user_id = user_table.user_id";

    private DBHelper dbHelper;

    private SQLiteDatabase database;

    public RouteDAO(String userid, DBHelper dbHelper) {
        this.userid = userid;
        this.dbHelper = dbHelper;
    }

    public RouteDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insertRoute(Route route) {

        database = dbHelper.getWritableDatabase();

        try {
            SQLiteStatement statement = database.compileStatement(SQL_QUERY_INSERT_ROUTE);
            database.beginTransaction();
            statement.bindString(1, route.getAreaId());
            statement.bindString(2, userid);
            statement.bindLong(3, route.getRating());
            statement.bindString(4, route.getTypeOfRoad());
            statement.bindString(5, route.getComment());
            statement.executeInsert();
            database.setTransactionSuccessful();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            database.endTransaction();
        }
    }

    public void insertRouteFromApi(Route route) {

        database = dbHelper.getWritableDatabase();
        System.out.println(route);

        try {
            SQLiteStatement statement = database.compileStatement(SQL_QUERY_INSERT_ROUTE);
            database.beginTransaction();
            statement.bindString(1, route.getAreaId());
            statement.bindString(2, route.getUserId());
            statement.bindLong(3, route.getRating());
            statement.bindString(4, route.getTypeOfRoad());
            statement.bindString(5, route.getComment());
            statement.executeInsert();

            database.setTransactionSuccessful();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            database.endTransaction();
        }

        try{
            database.beginTransaction();
            SQLiteStatement statementUser = database.compileStatement(SQL_QUERY_INSERT_USER);
            statementUser.bindString(1, route.getUserId());
            statementUser.bindString(2, route.getUserName());
            statementUser.bindString(3, "password");
            statementUser.executeInsert();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            database.endTransaction();
        }
    }

    public void insertLocations(List<Location> locations, String id) {

        database = dbHelper.getWritableDatabase();

        try {
            SQLiteStatement statement = database.compileStatement(SQL_QUERY_INSERT_LOCATIONS);
            database.beginTransaction();
            statement.bindString(1, id);
            statement.bindString(2, locations.toString());
            statement.bindString(3, userid);
            statement.executeInsert();
            database.setTransactionSuccessful();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            database.endTransaction();
        }
    }

    public List<Route> selectAllLocations() {

        database = dbHelper.getReadableDatabase();

        List<Location> locationsOfRoute = new ArrayList<>();

        List<Route> routes = new ArrayList<>();

        String str = "";

        DirectionsJSONParser directionsJSONParser = new DirectionsJSONParser();

        Cursor dbCursor = database.rawQuery(SQL_QUERY_SELECT_LOCATIONS_TEST, null);

        int strIndex = dbCursor.getColumnIndex(dbHelper.AREA_JSON);
        int area_idIndex = dbCursor.getColumnIndex(dbHelper.AREA_ID);
        int scoreIndex = dbCursor.getColumnIndex(dbHelper.SCORE);
        int area_typeIndex = dbCursor.getColumnIndex(dbHelper.AREA_TYPE);
        int feedbackIndex = dbCursor.getColumnIndex(dbHelper.FEEDBACK);
        int loginIndex = dbCursor.getColumnIndex(dbHelper.USER_LOGIN);

        while(dbCursor.moveToNext()){
            str = dbCursor.getString(strIndex);
            locationsOfRoute = directionsJSONParser.parseStringToLocations(str);

            String login = dbCursor.getString(loginIndex);
            String area_id = dbCursor.getString(area_idIndex);
            int score = dbCursor.getInt(scoreIndex);
            String area_type = dbCursor.getString(area_typeIndex);
            String feedback = dbCursor.getString(feedbackIndex);
            Route route = new Route(login, locationsOfRoute, score, area_type, feedback, area_id);

            routes.add(route);

        }

        database.close();

        return routes;
    }

    public void selectAvgScoreOfRoute(Route route) {

        double avgScore = 0;

        database = dbHelper.getReadableDatabase();

        String[] args = {route.getAreaId()};

        Cursor dbCursor = database.rawQuery(SQL_QUERY_SELECT_AVG_SCORE_OF_ROUTE, args);

        int strIndex = dbCursor.getColumnIndex("AVG(score)");

        while(dbCursor.moveToNext()){
            avgScore = dbCursor.getDouble(strIndex);
            route.setAvgScore(avgScore);
        }

        database.close();

    }

    public List<RouteInfoUtility> selectInfoOfRoute(Route route) {

        List<RouteInfoUtility> info = new ArrayList<>();

        String login = "";

        String comment = "";

        String typeOfRoad = "";

        int score = 0;

        database = dbHelper.getReadableDatabase();

        String[] args = {route.getAreaId()};

        Cursor dbCursor = database.rawQuery(SQL_QUERY_SELECT_INFO_OF_ROUTE, args);

        int scoreIndex = dbCursor.getColumnIndex(dbHelper.SCORE);
        int loginIndex = dbCursor.getColumnIndex(dbHelper.USER_LOGIN);
        int commentIndex = dbCursor.getColumnIndex(dbHelper.FEEDBACK);
        int typeOfRoadIndex = dbCursor.getColumnIndex(dbHelper.AREA_TYPE);

        while(dbCursor.moveToNext()){
            login = dbCursor.getString(loginIndex);
            comment = dbCursor.getString(commentIndex);
            typeOfRoad = dbCursor.getString(typeOfRoadIndex);
            score = dbCursor.getInt(scoreIndex);
            info.add(new RouteInfoUtility(score,login,comment,typeOfRoad));
        }

        System.out.println(info);

        database.close();

        return info;
    }
}
