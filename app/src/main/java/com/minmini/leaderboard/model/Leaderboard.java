package com.minmini.leaderboard.model;

public class Leaderboard {
    private String player_name;
    private String course;
    private String score;
    private String activity_date;

    public Leaderboard(String player_name, String course, String score, String activity_date) {
        this.player_name = player_name;
        this.course = course;
        this.score = score;
        this.activity_date = activity_date;
    }

    public String getPlayer_name() {
        return player_name;
    }

    public void setPlayer_name(String player_name) {
        this.player_name = player_name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getActivity_date() {
        return activity_date;
    }

    public void setActivity_date(String activity_date) {
        this.activity_date = activity_date;
    }
}
