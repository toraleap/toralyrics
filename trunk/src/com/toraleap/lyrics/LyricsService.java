//package com.toraleap.lyrics;
//
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.*;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.IBinder;
//import android.os.RemoteException;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.widget.Toast;
//
////import com.android.music.IMediaPlaybackService;
////import com.htc.music.IMediaPlaybackService;
//import java.io.*;
//import java.util.*;
//
//public class LyricsService extends Service {
//	ContentResolver resolver;
//    NotificationManager nm;
//    MediaPlayerServiceConnection conn;
//	String mediaTitle;
//	String mediaArtist;
//	String mediaAlbum;
//	String mediaPath;
//	String mediaLyrics;
//	boolean isHtc;
//	LyricsParser lyrics;
//	
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        resolver = getContentResolver();
//        bindToMusicService();
//        Timer mTimer = new Timer();
//		Log.i("toralyrics", "createtimer");
//        mTimer.schedule(new TimerTask() {
//             public void run() {
//         		 Log.i("toralyrics", String.valueOf(conn.getPosition()));
//         		 Log.i("toralyrics", lyrics.getLyrics(conn.getPosition(), true));
//                 Toast.makeText(getApplicationContext(), lyrics.getLyrics(conn.getPosition(), true), Toast.LENGTH_SHORT).show();
//             }
//         }, 2000, 2000);
//    }
//	
//	@Override
//	public void onDestroy() {
//		// TODO Auto-generated method stub
//		Log.i("toralyrics", "ondestroy");
//		unbindService(conn);
//		super.onDestroy();
//	}
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public void bindToMusicService() {
//        Intent i = new Intent();
//        isHtc = true;
//        i.setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"); 
//        conn = new MediaPlayerServiceConnection();
//        if (!bindService(i, conn, 0)) {
//        	isHtc = false;
//            i.setClassName("com.android.music", "com.android.music.MediaPlaybackService");
//            bindService(i, conn, 0);
//        }
//	}
//	
//    public class MediaPlayerServiceConnection implements ServiceConnection {
//    	public com.htc.music.IMediaPlaybackService mServiceHtc;
//    	public com.android.music.IMediaPlaybackService mServiceAndroid;
//    	
//    	public void onServiceConnected(ComponentName name, IBinder service) {
//    		if (isHtc) {
//    	    	mServiceHtc = com.htc.music.IMediaPlaybackService.Stub.asInterface(service);
//				try {
//					mediaTitle = mServiceHtc.getTrackName();
//					mediaArtist = mServiceHtc.getArtistName();
//					mediaAlbum = mServiceHtc.getAlbumName();
//		    		mediaPath = getMediaPath((int) mServiceHtc.getAudioId());
//					mediaLyrics = getLyrics(mediaPath);
//					lyrics = new LyricsParser(mediaLyrics, true);
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//    		}
//    		else {
//    			mServiceAndroid = com.android.music.IMediaPlaybackService.Stub.asInterface(service);
//				try {
//					mediaTitle = mServiceAndroid.getTrackName();
//					mediaArtist = mServiceAndroid.getArtistName();
//					mediaAlbum = mServiceAndroid.getAlbumName();
//		    		mediaPath = getMediaPath((int) mServiceAndroid.getAudioId());
//					mediaLyrics = getLyrics(mediaPath);
//					lyrics = new LyricsParser(mediaLyrics, true);
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//    		}
//    	}
//
//    	public void onServiceDisconnected(ComponentName name) {
//    		//Log.i("MediaPlayerServiceConnection", "Disconnected!");
//    	}
//    	
//    	public long getPosition() {
//			try {
//	    		if (isHtc)
//					return mServiceHtc.position();
//				else
//	    			return mServiceAndroid.position();
//			} catch (RemoteException e) {
//				return 0;
//			}
//    	}
//    }
//    
//    public String getMediaPath(int id) {
//		Uri mediaPath = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
//		Cursor cursor = resolver.query(mediaPath, null, null, null, null);
//		cursor.moveToFirst();
//		return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
//	}
//	
//	public String getLyrics(String mediaPath) throws IOException {
//		String lyricsPath = mediaPath.substring(0, mediaPath.lastIndexOf(".")) + ".lrc";
//		File file = new File(lyricsPath);
//		if (file.exists()) {
//	        BufferedInputStream in = new BufferedInputStream(new FileInputStream(lyricsPath));      
//	        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);         
//	        byte[] temp = new byte[1024];      
//	        int size = 0;      
//	        while ((size = in.read(temp)) != -1) {      
//	            out.write(temp, 0, size);      
//	        }      
//	        in.close();      
//	        byte[] content = out.toByteArray();      
//			return new String(content, 0, content.length, "gb2312");
//		}
//		return null;
//	}
//}
//
////nm = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
////Intent intent = new Intent();
////intent.setClass(this, MainActivity.class);
////intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////final PendingIntent appIntent = PendingIntent.getActivity(this, 0, intent, 0);
////n = new Notification();
////n.icon = android.R.drawable.ic_popup_disk_full;
////n.tickerText = text;
////n.setLatestEventInfo(this, "toralyrics", text, appIntent);
////nm.notify(0, n);