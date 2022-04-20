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

    private final PlayAudioBinder playAudioBinder = new PlayAudioBinder();
    private MediaPlayer mediaPlayer;
    private AudioPlayedCallback audioPlayedCallback;
    private final Timer timer = new Timer();
    private LyricWork lyricWork;


    private class LyricWork extends TimerTask {

        private int currentLyricIndex;
        private final int lyricNum;
        private final List<Lyric> lyrics;

        public LyricWork(List<Lyric> lyrics) {
            this.lyrics = lyrics;
            this.lyricNum = lyrics.size();
            this.currentLyricIndex = -1;
        }

        @Override
        public void run() {
            int timePlayed = mediaPlayer.getCurrentPosition();
            for (int i = 0; i < lyricNum; i++) {
                Lyric lyric = lyrics.get(i);
                if (timePlayed >= lyric.getStartTime()
                        && timePlayed < lyric.getEndTime()) {
                    if (currentLyricIndex != i) {
                        audioPlayedCallback.lyricText(
                                lyric.getLyric(),
                                lyric.getTranslation()
                        );
                        currentLyricIndex = i;
                    }
                    break;
                }
            }
        }

        public int prevLyricMills() {
            playAudioBinder.seekToOrigin();
            int prev = currentLyricIndex == 0
                    ? currentLyricIndex : currentLyricIndex - 1;
            return lyrics.get(prev).getStartTime();
        }

        public int nextLyricMills() {
            playAudioBinder.seekToOrigin();
            int next = currentLyricIndex == lyricNum - 1
                    ? currentLyricIndex : currentLyricIndex + 1;
            return lyrics.get(next).getStartTime();
        }
    }

    public class PlayAudioBinder extends Binder {

        private int playedTimePos = 0;
        private boolean isPlayFinished = false;
        private Song song;

        public void pause() {

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playedTimePos = mediaPlayer.getCurrentPosition();
                return;
            }

            mediaPlayer.seekTo(playedTimePos);
            mediaPlayer.start();
            ifRebootLyricWork();
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
            if (lyricWork != null) {
                mediaPlayer.seekTo(lyricWork.prevLyricMills());
                mediaPlayer.start();
                ifRebootLyricWork();
            }
        }

        public void nextSentence() {
            if (lyricWork != null) {
                mediaPlayer.seekTo(lyricWork.nextLyricMills());
                mediaPlayer.start();
                ifRebootLyricWork();
            }
        }

        // 当音频播放完成时，记录重新播放音频的位置
        void seekToOrigin() {
            playedTimePos = 0;
        }

        void audioPlayFinished() {
            isPlayFinished = true;
        }

        private void ifRebootLyricWork() {
            if (isPlayFinished) {
                lyricWork = new LyricWork(song.getLyric());
                timer.schedule(lyricWork, 0, 500);
                isPlayFinished = false;
            }
        }

        public void setPlayAudioCallback(AudioPlayedCallback callback) {
            audioPlayedCallback = callback;
        }
    }

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
                    playAudioBinder.seekToOrigin();
                    playAudioBinder.audioPlayFinished();
                });
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            audioPlayedCallback.onError();
            return false;
        });
    }

    /**
     * 当需要服务一启动就执行一些任务，可以在这里进行。
     * 当服务每次被启动时，它都会被调用，而onCreate方法不会每次都被调用。
     *
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
