package com.milylg.audio;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.milylg.audio.databinding.ActivityMainBinding;
import com.milylg.audio.service.AudioPlayedCallback;
import com.milylg.audio.service.Lyric;
import com.milylg.audio.service.PlayAudioService;
import com.milylg.audio.service.Song;
import com.milylg.audio.ui.AudioItemClickAction;
import com.milylg.audio.ui.SongAdapter;
import com.milylg.audio.ui.SongAdjustAction;
import com.milylg.audio.ui.SongItem;
import com.milylg.audio.viewmodel.AudioViewModel;
import com.milylg.audio.viewmodel.SeekMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT;

public class AudioActivity extends AppCompatActivity {

    private static final String TAG = "AudioActivity";

    private ActivityMainBinding homeBinding;
    private AudioViewModel audioViewModel;
    private PlayAudioService.PlayAudioBinder playAudioBinder;

    private Drawable playDrawable;
    private Drawable pauseDrawable;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playAudioBinder = (PlayAudioService.PlayAudioBinder) service;
            playAudioBinder.setPlayAudioCallback(audioPlayedCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // do nothing...
        }
    };

    private final AudioPlayedCallback audioPlayedCallback = new AudioPlayedCallback() {
        @Override
        public void onCompleted() {
            homeBinding.btnPlayMusic.setImageDrawable(pauseDrawable);
        }

        @Override
        public void onError() {
            audioViewModel.handleMediaError();
        }

        @Override
        public void lyricText(String lrc, String translation) {
            audioViewModel.updateAudioLyric(lrc, translation);
        }
    };

    private final SongAdjustAction songAdjustAction = new SongAdjustAction() {
        @Override
        public void play() {
            audioViewModel.togglePlayStatus();
        }

        @Override
        public void previousSentence() {
            audioViewModel.seekAudio(SeekMode.PREV_SENTENCE);
        }

        @Override
        public void nextSentence() {
            audioViewModel.seekAudio(SeekMode.NEXT_SENTENCE);
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            audioViewModel.seekAudio(SeekMode.PREV_SENTENCE);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            audioViewModel.seekAudio(SeekMode.NEXT_SENTENCE);
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private final AudioItemClickAction audioItemClickAction = new AudioItemClickAction() {
        @Override
        public void onClick(View v) {
            TextView tvSongUri = v.findViewById(R.id.tv_uri_song);
            String mp3 = tvSongUri.getText().toString();
            Log.i(TAG, "onClick: mp3 path = " + mp3);
            if (!hasAvailableAudioLrc(mp3)) {
                homeBinding.tvCnStatement.setText("No lyric.");
                return;
            }

            List<Lyric> lyrics = lrcInfo(lrcPath(mp3));
            audioViewModel.playAudio(new Song(mp3, lyrics));

            // TODO: 显示当前播放音频名称
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_main);
        // Initialized View
        setSupportActionBar(homeBinding.toolbar);
        playDrawable = ContextCompat.getDrawable(
                this, R.drawable.ic_baseline_play);
        pauseDrawable = ContextCompat.getDrawable(
                this, R.drawable.ic_baseline_pause);
        // Initialized View Model
        audioViewModel = new AudioViewModel(getApplication());

        homeBinding.setVm(audioViewModel);
        homeBinding.setAction(songAdjustAction);

        // Initialized Song Items.
        SongAdapter songAdapter = new SongAdapter(audioViewModel, audioItemClickAction);
        RecyclerView recyclerView = homeBinding.rvNoteListView;
        recyclerView.setAdapter(songAdapter);

        // Initialized service for play audio.
        Intent intent = new Intent(this, PlayAudioService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        initializedViewEvent();
        if(hasAvailablePermission()) {
            refreshSongItems();
        }
    }

    private boolean hasAvailablePermission() {
        int permissionCode = ContextCompat.checkSelfPermission(
                AudioActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean isArrowAccessReadExtStorage = permissionCode == PackageManager.PERMISSION_GRANTED;
        if (!isArrowAccessReadExtStorage) {
            Snackbar.make(homeBinding.rvNoteListView,
                    "No open permission for application!",
                    LENGTH_LONG).setAction("Request", v ->
                    ActivityCompat.requestPermissions(
                            AudioActivity.this,
                            new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            },
                            1))
                    .show();
        }
        return isArrowAccessReadExtStorage;
    }

    private void initializedViewEvent() {

        audioViewModel.togglePlayEvent().observe(this, isPauseAudio -> {
            Song currentSong = audioViewModel.audioPlayedInfo();
            Log.i(TAG, "isPauseAudio = " + isPauseAudio);
            if (playAudioBinder == null || currentSong == null) {
                return;
            }

            playAudioBinder.pause();

            if (playAudioBinder.isPlaying()) {
                homeBinding.btnPlayMusic.setImageDrawable(playDrawable);
            } else {
                homeBinding.btnPlayMusic.setImageDrawable(pauseDrawable);
            }
        });

        audioViewModel.seekAudioEvent().observe(this, seekMode -> {

            if (seekMode.isNextSentence()) {
                playAudioBinder.nextSentence();
            } else {
                playAudioBinder.previousSentence();
            }
            homeBinding.btnPlayMusic.setImageDrawable(playDrawable);
        });

        audioViewModel.playItemAudioEvent().observe(this, song -> {

            if (playAudioBinder == null) {
                return;
            }

            if (song != null) {
                try {
                    playAudioBinder.play(song);
                    homeBinding.btnPlayMusic.setImageDrawable(playDrawable);
                } catch (IOException e) {
                    homeBinding.btnPlayMusic.setImageDrawable(pauseDrawable);
                    Snackbar.make(homeBinding.rvNoteListView,
                            "Play audio exception!",
                            LENGTH_SHORT).show();
                }
            }
        });

        audioViewModel.mediaErrorEvent().observe(this,
                unused -> Snackbar.make(homeBinding.rvNoteListView,
                "Error!",
                LENGTH_SHORT).show());
    }

    private boolean hasAvailableAudioLrc(String audioPath) {
        if (audioPath == null || "".equals(audioPath)) {
            return false;
        }
        String lrcPath = lrcPath(audioPath);
        return new File(lrcPath).exists();
    }

    private String lrcPath(String audioPath) {
        int postfix = audioPath.lastIndexOf('.');
        String lrcPath = audioPath.substring(0, postfix) + ".lrc";
        Log.i(TAG, "hasAvailableAudioLrc: lrcPath = " + lrcPath);
        return lrcPath;
    }

    private List<Lyric> lrcInfo(String lrcPath) {
        List<Lyric> lyrics = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(lrcPath);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "GBK");
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String regex = "\\[\\d\\d:\\d\\d.\\d\\d]";
            Pattern pattern = Pattern.compile(regex);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    int timeInt = timeToInt(matcher.group().substring(1, 9));
                    String textStr = line.substring(matcher.end());
                    lyrics.add(new Lyric(textStr, timeInt));
                } else {
                    if (!lyrics.isEmpty()) {
                        lyrics.get(lyrics.size() - 1).setTranslation(line);
                    }
                }
            }
        } catch (IOException e) {
            Log.w(TAG, Objects.requireNonNull(e.getMessage()));
            Snackbar.make(homeBinding.rvNoteListView,
                    "Read lrc file failed!",
                    LENGTH_SHORT).show();
        }
        return lyrics;
    }

    /**
     * 将字符串时间转换为数字时间
     * @param string 字符串时间格式：00:00.00
     * @return int 毫秒单位的时间
     */
    private int timeToInt(String string) {
        String[] timeData = string.replace(".", ":").split(":");
        int minute = Integer.parseInt(timeData[0]);
        int second = Integer.parseInt(timeData[1]);
        int millisecond = Integer.parseInt(timeData[2]) * 10;
        return (minute * 60 + second) * 1000 + millisecond;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull String name,
                             @NonNull Context context,
                             @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.search_music) {
            Log.i(TAG, "OptionsItemSelected: Refresh Song Items");
            if (hasAvailablePermission()) {
                refreshSongItems();
            }
        }
        return true;
    }

    private void refreshSongItems() {
        List<SongItem> songItemList = new ArrayList<>();
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DATA
                },
                null,
                null,
                MediaStore.Audio.AudioColumns.IS_MUSIC
        );

        if (cursor.moveToFirst()) {
            SongItem song;
            do {
                song = new SongItem(
                        cursor.getString(0),
                        cursor.getString(1)
                );
                songItemList.add(song);
            } while (cursor.moveToNext());
        }
        cursor.close();
        audioViewModel.refreshSongs(songItemList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}