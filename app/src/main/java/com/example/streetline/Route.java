package com.example.streetline;

import java.util.ArrayList;
import java.util.List;

//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;

//@Entity
//@Table(name = "streetline.rating_table")
public class Route {

    //@Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    //@Column(name = "rating_id")
    private int id;

    private List<Location> locations = new ArrayList<>();

    private String rating;

    //@Column(name = "area_type")
    private String typeOfRoad;

    //@Column(name = "feedback")
    private String comment;

    public Route(List<Location> locations, String rating, String typeOfRoad, String comment) {
        this.locations = locations;
        this.rating = rating;
        this.typeOfRoad = typeOfRoad;
        this.comment = comment;
    }

    public Route() {
    }

    @Override
    public String toString() {
        return "Route{" +
                "locations=" + locations +
                ", rating='" + rating + '\'' +
                ", typeOfRoad='" + typeOfRoad + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    public Route(Route route) {
        this.locations = route.getLocations();
        this.rating = route.getRating();
        this.typeOfRoad = route.typeOfRoad;
        this.comment = route.comment;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getTypeOfRoad() {
        return typeOfRoad;
    }

    public void setTypeOfRoad(String typeOfRoad) {
        this.typeOfRoad = typeOfRoad;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
