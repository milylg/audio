package com.milylg.editarea.service;

import java.util.List;

public class Song {
    private final String uri;
    private final List<Lyric> lyrics;

    public Song(String uri, List<Lyric> lyrics) {
        this.uri = uri;
        this.lyrics = lyrics;

        for (int i = 0, s = lyrics.size() - 2; i <= s; i ++) {
            lyrics.get(i).setEndTime(
                    lyrics.get(i + 1).getStartTime()
            );
        }
    }

    public String getUri() {
        return uri;
    }

    public List<Lyric> getLyric() {
        return lyrics;
    }

    public void lastTime(int lastMills) {
        if (lyrics != null && !lyrics.isEmpty()) {
            lyrics.get(lyrics.size() - 1).setEndTime(lastMills);
        }
    }
}
