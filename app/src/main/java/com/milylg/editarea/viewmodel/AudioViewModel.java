package com.milylg.editarea.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;
import androidx.lifecycle.AndroidViewModel;

import com.milylg.editarea.SingleLiveEvent;
import com.milylg.editarea.service.Song;
import com.milylg.editarea.ui.SongItem;

import java.util.List;

public class AudioViewModel extends AndroidViewModel {

    public ObservableList<SongItem> items = new ObservableArrayList<SongItem>();

    public AudioViewModel(@NonNull Application application) {
        super(application);
    }

    private final SingleLiveEvent<Song> playAudio = new SingleLiveEvent<>();

    public SingleLiveEvent<Song> playItemAudioEvent() {
        return playAudio;
    }

    public void playAudio(Song song) {
        playAudio.setValue(song);
    }

    public Song audioPlayedInfo() {
        return playAudio.getValue();
    }

    public ObservableField<String> enAudioStatement = new ObservableField<>();
    public ObservableField<String> cnAudioStatement = new ObservableField<>();

    public void updateAudioLyric(String enLyric, String cnLyric) {
        enAudioStatement.set(enLyric);
        cnAudioStatement.set(cnLyric);
    }

    private final SingleLiveEvent<Void> pauseStatus = new SingleLiveEvent<>();

    public void togglePlayStatus() {
        pauseStatus.call();
    }

    public SingleLiveEvent<Void> togglePlayEvent() {
        return pauseStatus;
    }

    private final SingleLiveEvent<SeekMode> seekSentence = new SingleLiveEvent<>();

    public SingleLiveEvent<SeekMode> seekAudioEvent() {
        return seekSentence;
    }

    public void seekAudio(SeekMode seekMode) {
        seekSentence.setValue(seekMode);
    }

    public void refreshSongs(List<SongItem> newSongs) {
        items.clear();
        items.addAll(newSongs);
    }
}
