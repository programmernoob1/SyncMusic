package com.example.santy.syncmusic;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {
    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicService;
    private Intent playIntent;
    private boolean pause=true;
    private boolean musicBound=false;
    private MusicController mediaController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
// app-defined int constant
                return;
            }}


        setContentView(R.layout.activity_main);
        songView=(ListView)findViewById(R.id.song_list);
        songList=new ArrayList<>();
        setSongList();
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o1.getTitle());
            }
        });
        SongAdapter songAdapter=new SongAdapter(this,songList);
        songView.setAdapter(songAdapter);
//        if(songList.size()!=0)
//        Toast.makeText(MainActivity.this,songList.size(), Toast.LENGTH_LONG).show();
        setMediaController();
    }
    private ServiceConnection musicConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder=(MusicService.MusicBinder)service;
            musicService=binder.getService();
            musicBound=true;
            musicService.setSongs(songList);
            mediaController.show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound=false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent=new Intent(this,MusicService.class);
            bindService(playIntent,musicConnection,BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    public void songPicked(View view){
        musicService.setSong(Integer.parseInt( view.getTag().toString()));
        musicService.playSong();
        pause=false;
        mediaController.show(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(playIntent);
        musicService=null;
        musicBound=false;
    }
    public void setSongList(){
        ContentResolver contentResolver=getContentResolver();
        Uri musicResolver= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor=contentResolver.query(musicResolver,null,null,null,null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
//            Toast.makeText(MainActivity.this,"HELLO",Toast.LENGTH_LONG).show();
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }
    public class MusicController extends MediaController{
        public MusicController(Context context) {
            super(context);
        }

        @Override
        public void hide() {
//            super.hide();
        }
    }

    private void setMediaController(){
        mediaController=new MusicController(MainActivity.this);
        mediaController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                playNext();
                musicService.playPrev();
                mediaController.show(0);
            }
        },new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                playPrev();
                musicService.playNext();
                mediaController.show(0);
            }
        });
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(findViewById(R.id.song_list));
        mediaController.setEnabled(true);
//        Toast.makeText(this,"helloooooo",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void start() {
        pause=false;
        musicService.go();
    }

    @Override
    public void pause() {
        pause=true;
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        Log.i("Check",String.valueOf(musicService.isPng()));
        if(musicService!=null && musicBound && !pause)
            return musicService.getDur();
        return 0;
    }
    @Override
    public int getCurrentPosition() {
        if(musicService!=null && musicBound && !pause)
            return musicService.getPosn();
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicService!=null&& musicBound)
            return !pause;
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
