package com.example.santy.syncmusic;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener {
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();

    public MusicService() {



    }
    public void setSong(int index){
        songPosn=index;
    }
    public void setSongs(ArrayList<Song> songs){
        this.songs=songs;
    }
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
    public void playSong(){
        //play a song
        player.reset();
        Song playSong = songs.get(songPosn);
        long currSong=playSong.getId();
        //get song
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        try {
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        songPosn=0;
        player=new MediaPlayer();
        initMediaPlayer();
    }
    public void initMediaPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0)
        {mp.reset();playNext();}
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songs.get(songPosn).getTitle())
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songs.get(songPosn).getTitle());
        Notification not = builder.build();
        startForeground(1, not);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        Log.i("Check","Durationchecking"+player.getDuration());
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }
    public void playNext(){
        songPosn++;
        if(songPosn>=songs.size())songPosn=0;
        playSong();
    }
    public void playPrev(){
        songPosn--;
        if(songPosn<0)songPosn=songs.size()-1;
        playSong();
    }
}
