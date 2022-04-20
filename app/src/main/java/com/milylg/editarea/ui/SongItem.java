package com.milylg.editarea.ui;

public class SongItem {

    private final String songName;
    private final String uri;

    public SongItem(String songName, String uri) {
        this.uri = uri;
        this.songName = songName;
    }

    public String getUri() {
        return uri;
    }

    public String getSongName() {
        return songName;
    }
}
