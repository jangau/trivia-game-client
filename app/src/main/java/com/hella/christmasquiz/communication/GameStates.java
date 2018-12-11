package com.hella.christmasquiz.communication;

public class GameStates {
    private static String team;
    private static Integer gameID;
    private static String viewName;

    public static synchronized Integer getGameID() {
        return GameStates.gameID;
    }

    public static synchronized void setGameID(Integer gameID){
        GameStates.gameID = gameID;
    }

    public static synchronized String getTeam(){
        return GameStates.team;
    }

    public static synchronized void setTeam(String teamName){
        GameStates.team = teamName;
    }

    public static synchronized String getViewName(){
        return GameStates.viewName;
    }

    public static synchronized void setViewName(String viewName){
        GameStates.viewName = viewName;
    }

}
