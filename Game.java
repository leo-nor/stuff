package com.example.assignmentdistributed;

import com.example.assignmentdistributed.server.Champion;
import com.example.assignmentdistributed.server.UserHandler;
import com.example.assignmentdistributed.user.User;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;

public class Game implements Serializable {
    public String P1;
    public int P1_level;
    public Champion P1_champion;
    public int P1_currentChampHealth;

    public String P2;
    public int P2_level;
    public Champion P2_champion;
    public int P2_currentChampHealth;

    public String turn;
    public String tooltip;

    public boolean gameOver;

    private String winner;

    public Game(User user1, User user2){
        P1 = user1.getUsername();
        P2 = user2.getUsername();
        P1_level = Integer.parseInt(user1.getLevel());
        P2_level = Integer.parseInt(user2.getLevel());
        P1_champion = Champion.CHAMPION_1;
        P2_champion = Champion.CHAMPION_1;
        P1_currentChampHealth = P1_champion.getHealth(P1_level);
        P2_currentChampHealth = P2_champion.getHealth(P2_level);
        turn = P1;
        tooltip = turn + "was given the first attack opportunity!";
    }

    public void nextTurn() {
        if (turn.equals(P1)){
            attack(P1, P2);
            turn = P2;
        }
        else if (turn.equals(P2)) {
            attack(P1, P2);
            turn = P1;
        }
        else System.out.println("invalid turn: " + turn);
    }

    public void attack(String attacker, String defender){
        Champion attackingChampion, defendingChampion;
        int attackingChampionCurrentHealth, defendingChampionCurrentHealth;

        if (attacker.equals(P1)) {
            attackingChampion = P1_champion;
            attackingChampionCurrentHealth = P1_currentChampHealth;
            defendingChampion = P2_champion;
            defendingChampionCurrentHealth = P2_currentChampHealth;
        } else {
            attackingChampion = P2_champion;
            attackingChampionCurrentHealth = P2_currentChampHealth;
            defendingChampion = P1_champion;
            defendingChampionCurrentHealth = P1_currentChampHealth;
        }

        Random rand = new Random();
        double randomMultiplier = 0.5 + rand.nextDouble();   //rand between 0.5 and 1.5
        int damage = (int) (randomMultiplier * attackingChampion.getDamage(P1_level));
        defendingChampionCurrentHealth -= damage;

        System.out.println(attacker + " attacks " + defender + " and deals " + damage + " damage!");
        tooltip = attacker + " attacks " + defender + " and deals " + damage + " damage!";

        if (defendingChampionCurrentHealth <= 0) {
            System.out.println(attacker + " won!");
            winner = attacker;
            gameOver = true;
        } else {
            if (attacker.equals(P1)) {
                P2_currentChampHealth = defendingChampionCurrentHealth;
            } else {
                P1_currentChampHealth = defendingChampionCurrentHealth;
            }
        }
    }

    public String getWinner() {
        return winner;
    }


}
