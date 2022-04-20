package com.milylg.editarea.service;

public class Lyric {
    private final String lyric;
    private final int startTime;
    private int endTime;
    private int duration;

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

    public void durationTime(int endTime) {
        this.endTime = endTime;
        this.duration = endTime - startTime;
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
