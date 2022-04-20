package com.milylg.editarea.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlayAudioService extends Service {

    private static final String TAG = "PlayAudioService";

    private MediaPlayer mediaPlayer;
    private AudioPlayedCallback audioPlayedCallback;
    private final Timer timer = new Timer();

    private class LyricWork extends TimerTask {

        private int currentLyricIndex;
        int lyricNum;
        String lyricStr = "";
        List<Lyric> lyrics;

        public LyricWork(List<Lyric> lyrics) {
            this.lyrics = lyrics;
            this.lyricNum = lyrics.size();
            this.currentLyricIndex = -1;
        }

        @Override
        public void run() {
            int timePlayed = mediaPlayer.getCurrentPosition();
            for (int i=0; i < lyricNum; i ++) {
                Lyric lyric = lyrics.get(i);
                if (timePlayed >= lyric.getStartTime()
                        && timePlayed < lyric.getEndTime()) {
                    if (currentLyricIndex != i) {
                        audioPlayedCallback.lyricText(lyric.getLyric());
                        currentLyricIndex = i;
                    }
                    // currentLyricIndex = i;
                    break;
                }
            }

//            if(!lyrics.isEmpty()) {
//                if (!lyricStr.equals(lyrics.get(currentLyricIndex).getLyric())) {
//                    audioPlayedCallback.lyricText(lyrics.get(currentLyricIndex).getLyric());
//                }
//            }
        }

        public int previousMills() {
            playAudioBinder.posPlayedTime = 0;
            int prev = currentLyricIndex == 0 ? 0 : currentLyricIndex - 1;
            return lyrics.get(prev).getStartTime();
        }

        public int nextMills() {
            playAudioBinder.posPlayedTime = 0;
            int next = currentLyricIndex == lyricNum - 1 ? lyricNum - 1 : currentLyricIndex + 1;
            return lyrics.get(next).getStartTime();
        }
    }
    private LyricWork lyricWork;

    public class PlayAudioBinder extends Binder {

        int posPlayedTime = 0;
        boolean isPlayFinished = false;
        Song song;

        public void pause() throws IOException {

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                posPlayedTime = mediaPlayer.getCurrentPosition();
                return;
            }

            mediaPlayer.seekTo(posPlayedTime);
            mediaPlayer.start();

            if (isPlayFinished) {
                lyricWork = new LyricWork(song.getLyric());
                timer.schedule(lyricWork, 0, 500);
                isPlayFinished = false;
            }
        }



        public void play(Song song) throws IOException {

            // 结束上一首音频的字幕显示任务
            if (lyricWork != null) {
                lyricWork.cancel();
            }

            this.song = song;
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getUri());
            mediaPlayer.prepare();
            mediaPlayer.start();

            song.lastTime(mediaPlayer.getDuration());
            lyricWork = new LyricWork(song.getLyric());
            timer.schedule(lyricWork, 0, 500);
        }

        public boolean isPlaying() {
            return mediaPlayer.isPlaying();
        }

        public void previousSentence() {
            if (lyricWork!=null) {
                mediaPlayer.seekTo(lyricWork.previousMills());
                mediaPlayer.start();

                if (isPlayFinished) {
                    lyricWork = new LyricWork(song.getLyric());
                    timer.schedule(lyricWork, 0, 500);
                    isPlayFinished = false;
                }
            }
        }

        public void nextSentence() {
            if (lyricWork!=null) {
                mediaPlayer.seekTo(lyricWork.nextMills());
                mediaPlayer.start();

                if (isPlayFinished) {
                    lyricWork = new LyricWork(song.getLyric());
                    timer.schedule(lyricWork, 0, 500);
                    isPlayFinished = false;
                }
            }
        }

        public void setPlayAudioCallback(AudioPlayedCallback callback) {
            audioPlayedCallback = callback;
        }
    }



    private final PlayAudioBinder playAudioBinder = new PlayAudioBinder();

    /**
     * 只有服务第一次创建时被调用
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(
                mp -> {
                    audioPlayedCallback.onCompleted();
                    lyricWork.cancel();
                    playAudioBinder.posPlayedTime = 0;
                    playAudioBinder.isPlayFinished = true;
                });
        // OnError
    }

    /**
     * 当需要服务一启动就执行一些任务，可以在这里进行。
     * 当服务每次被启动时，它都会被调用，而onCreate方法不会每次都被调用。
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return playAudioBinder;
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
