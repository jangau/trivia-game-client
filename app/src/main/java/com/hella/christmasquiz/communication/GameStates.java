package com.hella.christmasquiz.communication;

public class GameStates {
    private static String team;

    public static synchronized String getTeam(){
        return "#Rezist";
    }

    public static synchronized void setTeam(String teamName){
        GameStates.team = teamName;
    }

}
