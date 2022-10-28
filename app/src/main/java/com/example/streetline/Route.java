package com.example.streetline;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private List<Location> locations = new ArrayList<>();

    private int rating;

    private String something;

    private String comment;

    public Route(List<Location> locations, int rating, String something, String comment) {
        this.locations = locations;
        this.rating = rating;
        this.something = something;
        this.comment = comment;
    }
}
