package com.hella.christmasquiz.communication;

public class GameStates {
    private static String team;

    public static synchronized String getTeam(){
        return team;
    }

    public static synchronized void setTeam(String teamName){
        team = teamName;
    }

}
