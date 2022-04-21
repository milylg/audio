package com.milylg.audio.viewmodel;

public enum SeekMode {
    NEXT_SENTENCE, PREV_SENTENCE;

    public boolean isNextSentence() {
        return this == NEXT_SENTENCE;
    }
}
