package com.example.assignmentdistributed.user;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;
    private String token;
    private String rank;
    private String gameToken;

    public User(String name, String pass){
        this.username = name;
        this.password = pass;
        this.token = "";
        this.rank = "1";
        this.gameToken = "";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public int getRank() {
        return rank;
    }

    public String getGameToken() {
        return gameToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setGameToken(String gameToken) {
        this.gameToken = gameToken;
    }
}
