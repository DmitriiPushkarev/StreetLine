package com.example.streetline;

import java.util.UUID;

public class User {
    private String userid;
    private String login;
    private String email;
    private String password;
    private String password2;

    public User(String login, String password) {
        this.userid = UUID.randomUUID().toString();
        this.login = login;
        this.password = password;
    }

    public User(String login, String password, String email) {
        this.userid = UUID.randomUUID().toString();
        this.login = login;
        this.password = password;
        this.email = email;
    }

    public User(String login, String email, String password, String password2) {
        this.login = login;
        this.email = email;
        this.password = password;
        this.password2 = password2;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
