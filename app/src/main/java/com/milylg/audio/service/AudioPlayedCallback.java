package com.milylg.audio.service;

public interface AudioPlayedCallback {

    void onCompleted();

    void onError();

    void lyricText(String lrc, String translation);

}
