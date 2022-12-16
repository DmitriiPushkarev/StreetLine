package com.example.streetline;

public class RouteInfoUtility {
    private int score;
    private String login;
    private String comment;
    private String typeOfRoad;

    public RouteInfoUtility(int score, String login, String comment, String typeOfRoad) {
        this.score = score;
        this.login = login;
        this.comment = comment;
        this.typeOfRoad = typeOfRoad;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTypeOfRoad() {
        return typeOfRoad;
    }

    public void setTypeOfRoad(String typeOfRoad) {
        this.typeOfRoad = typeOfRoad;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "RouteInfoUtility{" +
                "score=" + score +
                ", login='" + login + '\'' +
                ", comment='" + comment + '\'' +
                ", typeOfRoad='" + typeOfRoad + '\'' +
                '}';
    }
}
