package com.milylg.editarea.viewmodel;

public enum SeekMode {
    NEXT_SENTENCE, PREV_SENTENCE;

    public boolean isNextSentence() {
        return this == NEXT_SENTENCE;
    }
}
