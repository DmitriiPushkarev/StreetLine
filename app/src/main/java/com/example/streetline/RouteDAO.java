package com.example.streetline;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RouteDAO {
    //добавить ID для юзера и типа дороги, сначала их сделать надо
    private static final String URL = "jdbc:postgresql://10.0.2.2:5432/postgres" ;

    private static final String USERNAME = "postgres" ;

    private  static final String PASSWORD = "1234" ;

    private static final String SQL_QUERY_INSERT_ROUTE = "INSERT INTO streetline.rating_table(area_id,user_id,score,area_type,feedback) VALUES(?,?,?,?,?)";

    private static final String SQL_QUERY_INSERT_LOCATIONS = "INSERT INTO streetline.area_table (area_id, area_json, user_id) VALUES (?, ?, ?);";

    //private static final String SQL_QUERY_SELECT_LOCATIONS = "SELECT area_id, area_json, user_id FROM streetline.area_table;";

    private static final String SQL_QUERY_SELECT_LOCATIONS = "SELECT rating_id, rating_table.area_id, rating_table.user_id, score, area_type, feedback, area_json\n" +
            "\tFROM streetline.rating_table JOIN streetline.area_table \n" +
            "\tON rating_table.area_id = area_table.area_id;";

    private static final String SQL_QUERY_SELECT_AVG_SCORE_OF_ROUTE = "SELECT AVG(score) FROM streetline.rating_table where area_id = (?)";

    //CHANGE ID USER AND AREA!!!
    public void insertRoute(Route route) {

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY_INSERT_ROUTE);
            preparedStatement.setString(1,route.getAreaId());
            preparedStatement.setInt(2,1);
            preparedStatement.setInt(3,route.getRating());
            preparedStatement.setString(4,route.getTypeOfRoad());
            preparedStatement.setString(5,route.getComment());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertLocations(List<Location> locations, String id) {

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY_INSERT_LOCATIONS);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, locations.toString());
            preparedStatement.setInt(3,1);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Route> selectAllLocations() {

        List<Location> locationsOfRoute = new ArrayList<>();

        List<Route> routes = new ArrayList<>();

        String str = "";

        DirectionsJSONParser directionsJSONParser = new DirectionsJSONParser();

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_QUERY_SELECT_LOCATIONS);
            while(resultSet.next()) {
                str = resultSet.getString("area_json");
                locationsOfRoute = directionsJSONParser.parseStringToLocations(str);

                String area_id = resultSet.getString("area_id");
                int score = resultSet.getInt("score");
                String area_type = resultSet.getString("area_type");
                String feedback = resultSet.getString("feedback");
                Route route = new Route(locationsOfRoute, score, area_type, feedback, area_id);

                routes.add(route);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return routes;
    }

    public void selectAvgScoreOfRoute(Route route) {

        double avgScore = 0;
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY_SELECT_AVG_SCORE_OF_ROUTE);
            preparedStatement.setString(1, route.getAreaId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                avgScore = resultSet.getDouble("avg");
                route.setAvgScore(avgScore);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
