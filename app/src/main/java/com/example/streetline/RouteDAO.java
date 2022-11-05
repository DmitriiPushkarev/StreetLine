package com.example.streetline;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RouteDAO {
    //добавить ID для юзера и типа дороги, сначала их сделать надо
    private static final String URL = "jdbc:postgresql://10.0.2.2:5432/postgres" ;

    private static final String USERNAME = "postgres" ;

    private  static final String PASSWORD = "1234" ;

    private static final String SQL_QUERY_INSERT_ROUTE = "INSERT INTO streetline.rating_table(area_id,user_id,score,area_type,feedback) VALUES(?,?,?,?,?)";

    private static final String SQL_QUERY_INSERT_LOCATIONS = "INSERT INTO streetline.area_table (area_json, user_id) VALUES (?, ?);";

    private static final String SQL_QUERY_SELECT_LOCATIONS = "SELECT area_id, area_json, user_id FROM streetline.area_table;";

    //CHANGE ID USER AND AREA!!!
    public void insertRoute(Route route) {

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY_INSERT_ROUTE);
            preparedStatement.setInt(1,1);
            preparedStatement.setInt(2,1);
            preparedStatement.setInt(3,route.getRating());
            preparedStatement.setString(4,route.getTypeOfRoad());
            preparedStatement.setString(5,route.getComment());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertLocations(List<Location> locations) {

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY_INSERT_LOCATIONS);
            preparedStatement.setString(1, locations.toString());
            preparedStatement.setInt(2,1);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Location> selectAllLocations() {

        List<Location> locations = new ArrayList<>();

        String str = "";

        DirectionsJSONParser directionsJSONParser = new DirectionsJSONParser();

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_QUERY_SELECT_LOCATIONS);
            while(resultSet.next()) {
                str = resultSet.getString("area_json");
                locations.addAll(directionsJSONParser.parseStringToLocations(str));
            }

            str = str.replaceAll(" ", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locations;
    }
}
