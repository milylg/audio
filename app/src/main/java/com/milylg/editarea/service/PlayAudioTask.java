package com.milylg.editarea.service;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

public class PlayAudioTask extends AsyncTask<Song, Integer, Integer> {

    private static final String TAG = "PlayAudioTask";
    private static final int PLAY_EXCEPTION = 1;
    private static final int PLAY_SUCCESS = 2;

    // MediaPlayer支持：AAC、AMR、FLAC、MP3、MIDI、OGG、PCM等格式
    private MediaPlayer mediaPlayer;

    public PlayAudioTask() {
        mediaPlayer = new MediaPlayer();
    }

    @Override
    protected Integer doInBackground(Song... song) {
        Log.i(TAG, "doInBackground: " + song[0].getUri());
        mediaPlayer.reset();
        try {
            String uri = song[0].getUri();
            mediaPlayer.setDataSource(uri);
        } catch (IOException e) {
            e.printStackTrace();
            return PLAY_EXCEPTION;
        }
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(mp -> {
            mediaPlayer.start();
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        });
        return PLAY_SUCCESS;
    }

    private int posPlayed = 0;

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            posPlayed = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        } else {
            mediaPlayer.seekTo(posPlayed);
            mediaPlayer.start();
        }
    }

    public void stopPlay() {
        if (mediaPlayer.isPlaying()) {
            posPlayed = 0;
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        Log.i(TAG, "onPostExecute: ");
        super.onPostExecute(integer);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
