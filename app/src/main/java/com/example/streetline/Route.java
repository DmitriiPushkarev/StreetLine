package com.example.streetline;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Route {

    private List<Location> locations = new ArrayList<>();

    private String areaId;

    private int rating;

    private String typeOfRoad;

    private String comment;

    private double avgScore;

    public Route(List<Location> locations, int rating, String typeOfRoad, String comment) {
        this.locations = locations;
        this.rating = rating;
        this.typeOfRoad = typeOfRoad;
        this.comment = comment;
        this.areaId = UUID.randomUUID().toString();
    }

    public Route(List<Location> locations, int rating, String typeOfRoad, String comment, String id) {
        this.locations = locations;
        this.rating = rating;
        this.typeOfRoad = typeOfRoad;
        this.comment = comment;
        this.areaId = id;
    }

    public Route(int rating, String typeOfRoad, String comment, String id) {
        this.rating = rating;
        this.typeOfRoad = typeOfRoad;
        this.comment = comment;
        this.areaId = id;
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

    public double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(double avgScore) {
        this.avgScore = avgScore;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
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
