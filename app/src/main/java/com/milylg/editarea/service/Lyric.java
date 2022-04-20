package com.milylg.editarea.service;

public class Lyric {
    private final String lyric;
    private final int startTime;
    private int endTime;

    public Lyric(String lyric, int startTime) {
        this.lyric = lyric;
        this.startTime = startTime;
    }

    public String getLyric() {
        return lyric;
    }

    public int getStartTime() {
        return startTime;
    }

    void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "lyric='" + lyric + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
