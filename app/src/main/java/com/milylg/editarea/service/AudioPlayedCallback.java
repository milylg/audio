package com.milylg.editarea.service;

public interface AudioPlayedCallback {

    void onCompleted();

    void onError();

    void lyricText(String lrc, String translation);

}
