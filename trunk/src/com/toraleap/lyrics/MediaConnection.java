package com.toraleap.lyrics;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

public class MediaConnection {
	public static final int MESSAGE_PLAYSTATE_CHANGED = 1;
	public static final int MESSAGE_MEDIA_CHANGED = 2;
	public static final int MESSAGE_LYRICS_CHANGED = 3;
	public static final int MESSAGE_POSITION_CHANGED = 4;
	
	Context context;
	Handler handler;
	Timer timer;
	ContentResolver resolver;
    MediaPlayerServiceConnection conn;
	boolean isHtc;
	Handler bindHandler;
    
	public long mediaId = -1;
    public String mediaTitle = null;
	public String mediaArtist = null;
	public String mediaAlbum = null;
	public long mediaPosition = 0;
	public long mediaDuration = 0;
	public String mediaPath = null;
	public String mediaAlbumArtPath = null;
	public LyricsParser.LyricsContext mediaLyricsContext = null;
	public LyricsParser lyrics = null;
	private long lastLyricsId = -100;
	
	public MediaConnection(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		resolver = context.getContentResolver();
		bindToMusicService();
		bindHandler = new Handler() {
			public void handleMessage(Message msg) {
		        timer = new Timer();
		        timer.schedule(new TimerTask() {
		            public void run() {
		            	if (mediaId != conn.getAudioId()) {
		            		OnMediaChanged();
		            		MediaConnection.this.handler.sendEmptyMessage(MESSAGE_MEDIA_CHANGED);
		            	}
						if (mediaPosition != getCurrentPosition() && mediaDuration > 0) {
							mediaPosition = getCurrentPosition();
		            		MediaConnection.this.handler.sendEmptyMessage(MESSAGE_POSITION_CHANGED);
						}
						if (lyrics != null) {
							mediaLyricsContext = lyrics.getLyricsContext(mediaPosition);
							if (lastLyricsId != mediaLyricsContext.id) {
								lastLyricsId = mediaLyricsContext.id;
				            	MediaConnection.this.handler.sendEmptyMessage(MESSAGE_LYRICS_CHANGED);
							}
						}
		            }
		    	}, 0, Preference.refreshRate);
			}
		};
    }
	
	public void release() {
		timer.cancel();
		context.unbindService(conn);
	}
	
	void bindToMusicService() {
        Intent i = new Intent();
        isHtc = true;
        i.setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"); 
        conn = new MediaPlayerServiceConnection();
        if (!context.bindService(i, conn, 0)) {
        	isHtc = false;
            i.setClassName("com.android.music", "com.android.music.MediaPlaybackService");
            context.bindService(i, conn, 0);
        }
	}

    class MediaPlayerServiceConnection implements ServiceConnection {
    	public com.htc.music.IMediaPlaybackService mServiceHtc;
    	public com.android.music.IMediaPlaybackService mServiceAndroid;
    	
    	public void onServiceConnected(ComponentName name, IBinder service) {
    		if (isHtc)
    			mServiceHtc = com.htc.music.IMediaPlaybackService.Stub.asInterface(service);
			else
				mServiceAndroid = com.android.music.IMediaPlaybackService.Stub.asInterface(service);
    		bindHandler.sendEmptyMessage(0);
    	}

    	public void onServiceDisconnected(ComponentName name) {
    		//Log.i("MediaPlayerServiceConnection", "Disconnected!");
    	}
    	
    	public long getAudioId() {
    		try {
				return (isHtc ? (long)mServiceHtc.getAudioId() : mServiceAndroid.getAudioId());
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to get AudioId");
				return -1;
			}
    	}
    	
    	public String getTrackName() {
    		try {
				return (isHtc ? mServiceHtc.getTrackName() : mServiceAndroid.getTrackName());
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to get TrackName");
				return "Failed to get TrackName";
			}    		
    	}
    	
    	public String getArtistName() {
    		try {
				return (isHtc ? mServiceHtc.getArtistName() : mServiceAndroid.getArtistName());
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to get ArtistName");
				return "Failed to get ArtistName";
			}    		
    	}
    	
    	public String getAlbumName() {
    		try {
				return (isHtc ? mServiceHtc.getAlbumName() : mServiceAndroid.getAlbumName());
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to get AlbumName");
				return "Failed to get AlbumName";
			}    		
    	}
    	
    	public long getPosition() {
    		try {
				return (isHtc ? mServiceHtc.position() : mServiceAndroid.position());
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to get Position");
				return -1;
			}    		
    	}
    	
    	public long getDuration() {
    		try {
				return (isHtc ? mServiceHtc.duration() : mServiceAndroid.duration());
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to get Duration");
				return -1;
			}    		
    	}
       	
    	public void prev() {
    		try {
				if (isHtc) mServiceHtc.prev(); else mServiceAndroid.prev();
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to play previous media");
			}    		
    	}
       	
    	public void replay() {
    		try {
				if (isHtc) mServiceHtc.seek(0); else mServiceAndroid.seek(0);
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to replay this media");
			}    		
    	}
    	
    	public void next() {
    		try {
				if (isHtc) mServiceHtc.next(); else mServiceAndroid.next();
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to play next media");
			}    		
    	}
       	
       	public boolean isPlaying() {
    		try {
				if (isHtc) return mServiceHtc.isPlaying(); else return mServiceAndroid.isPlaying();
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to get playstate");
			}
			return false;
    	}
       	
       	public void play() {
    		try {
				if (isHtc) mServiceHtc.play(); else mServiceAndroid.play();
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to play media");
			}    		
    	}
     	
       	public void pause() {
    		try {
				if (isHtc) mServiceHtc.pause(); else mServiceAndroid.pause();
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to pause media");
			}    		
    	}
     	
       	public void playpause() {
			if (isPlaying()) pause(); else play();	
    	}
    	
    	public void stop() {
    		try {
				if (isHtc) mServiceHtc.stop(); else mServiceAndroid.stop();
			} catch (RemoteException e) {
				Log.e("MediaConnection", "Failed to stop Playback");
			}    		
    	}
    }
    
    private String getMediaPath(long id) {
		Uri mediaPath = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
		Cursor cursor = resolver.query(mediaPath, null, null, null, null);
		cursor.moveToFirst();
		return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
	}
    
    private String getMediaAlbumArtPath(String mediaPath) {
    	String albumArtPath = mediaPath.substring(0, mediaPath.lastIndexOf("/")) + "/AlbumArt.jpg";
    	if (new File(albumArtPath).exists()) return albumArtPath; else return null; 
	}
    
    public long getCurrentPosition() {
    	return conn.getPosition();
    }
   	
	public void prev() {
		conn.prev(); 		
	}
   	
	public void replay() {
		conn.replay();  		
	}
	
	public void next() {
		conn.next();
	}
   	
   	public boolean isPlaying() {
		return conn.isPlaying();
	}
   	
   	public void play() {
		conn.play();		
	}
 	
   	public void pause() {
		conn.pause();   		
	}
 	
   	public void playpause() {
		conn.playpause();	
	}
   	
    public void stop() {
    	conn.stop();
    }
	
    public boolean delete(String mediaPath) {
    	boolean result = false;
    	File file;
    	file = new File(mediaPath);
    	if (file.exists()) result = file.delete();
    	if (!result) return false;
    	file = new File(mediaPath.substring(0, mediaPath.lastIndexOf(".")) + ".lrc");
    	if (file.exists()) result = file.delete();
    	if (!result) return false; else return true;
    }
	
    public boolean deleteLyrics(String lyricsPath) {
    	boolean result = false;
    	File file;
    	file = new File(lyricsPath);
    	if (file.exists()) result = file.delete();
    	if (!result) return false; else return true;
    }
    
	public void OnMediaChanged() {
		mediaId = conn.getAudioId();
		mediaTitle = conn.getTrackName();
		mediaArtist = conn.getArtistName();
		mediaAlbum = conn.getAlbumName();
		mediaDuration = conn.getDuration();
		mediaPath = getMediaPath(mediaId);
		mediaAlbumArtPath = getMediaAlbumArtPath(mediaPath);
		lastLyricsId = -100;
		lyrics = LyricsParser.FromMediaFile(mediaPath);
	}
	
	public static class Preference {
		public static long refreshRate = 200;
	}
}
