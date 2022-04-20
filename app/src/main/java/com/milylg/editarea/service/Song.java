package com.milylg.editarea.service;

import java.util.List;

public class Song {
    private final String uri;
    private List<Lyric> lyrics;

    public Song(String uri) {
        this.uri = uri;
    }

    public Song(String uri, List<Lyric> lyrics) {
        this.uri = uri;
        this.lyrics = lyrics;
    }

    public String getUri() {
        return uri;
    }

    public List<Lyric> getLyric() {
        return lyrics;
    }

    public void lastTime(int lastMs) {
        if (lyrics != null && !lyrics.isEmpty()) {
            lyrics.get(lyrics.size() - 1).durationTime(lastMs);
        }
    }
}
