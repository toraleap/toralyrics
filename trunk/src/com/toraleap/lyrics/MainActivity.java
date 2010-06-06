package com.toraleap.lyrics;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.*;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.*;
import android.widget.*;

public class MainActivity extends Activity {
	private static final int MENU_CONTROL = Menu.FIRST;
	private static final int MENU_PREFERENCE = Menu.FIRST + 1;
	private static final int MENU_SLEEPMODE = Menu.FIRST + 2;
	private static final int MENU_EXPANSION = Menu.FIRST + 3;
	private static final int MENU_ABOUT = Menu.FIRST + 4;
	private static final int MENU_EXIT = Menu.FIRST + 5;
	private static final int MENU_PREV = Menu.FIRST + 6;
	private static final int MENU_PLAYPAUSE = Menu.FIRST + 7;
	private static final int MENU_REPLAY = Menu.FIRST + 8;
	private static final int MENU_NEXT = Menu.FIRST + 9;
	private static final int MENU_PLAYLIST = Menu.FIRST + 10;
	private static final int MENU_DELETE = Menu.FIRST + 11;
	private static final int MENU_DELETELYRICS = Menu.FIRST + 12;
	private static final int MENU_DEBUGINFO = Menu.FIRST + 13;
	private static final int MENU_TEST = Menu.FIRST + 14;
	private static final int DIALOG_SLEEPMODE = 0;
	private static final int DIALOG_DEBUGINFO = 1;
	private static final int DIALOG_BACKKEY = 2;
	private static final int DIALOG_ABOUT = 3;
	private static final int DIALOG_DELETE = 4;
	private static final int DIALOG_DELETELYRICS = 5;
	private static final int RESULT_PREFERENCE = 1;
	MediaConnection mc;
	NotificationManager nm;
	PowerManager.WakeLock wakelock;
	GestureDetector gestureDetector = new GestureDetector(new ControlGestureDetector());
    Notification n;
    String notiTitle = "";
    String notiLyrics = "";
	Timer sleepModeTimer = new Timer();
	Date sleepModeTime;
	long sleepModeDelay = 0;
	String pendingDelete = null;
	ImageView imgAlbumArt;
	TextView txtLyricsPrev;
	TextView txtLyricsCurr;
	TextView txtLyricsNext;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    	wakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "toralyrics");
        updatePreference();
        setContentView(R.layout.main);
        imgAlbumArt = (ImageView)findViewById(R.id.mainalbumart);
        txtLyricsPrev = (TextView)findViewById(R.id.maintextprev);
        txtLyricsCurr = (TextView)findViewById(R.id.maintextcurr);
        txtLyricsNext = (TextView)findViewById(R.id.maintextnext);
        
       	nm = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
       	Intent intent = new Intent();
       	intent.setClass(MainActivity.this, MainActivity.class);
       	intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
       	final PendingIntent appIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
       	n = new Notification();
       	n.icon = R.drawable.notification;
       	n.flags = Notification.FLAG_ONGOING_EVENT;
       	n.contentIntent = appIntent;
       	n.when = 0;
       	
		mc = new MediaConnection(this, new Handler(){
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MediaConnection.MESSAGE_MEDIA_CHANGED:
					onMediaChanged();
					break;
				case MediaConnection.MESSAGE_POSITION_CHANGED:
					onPositionChanged();
					break;
				case MediaConnection.MESSAGE_LYRICS_CHANGED:
					onLyricsChanged();
					break;
				}
			}
		});
    }
    
    @Override
	protected void onDestroy() {
    	if (wakelock.isHeld()) wakelock.release();
		if (mc != null) mc.release();
		if (nm != null) nm.cancelAll();
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu menuControl = menu.addSubMenu(0, MENU_CONTROL, 0, R.string.menu_control).setIcon(android.R.drawable.ic_menu_manage);
		menuControl.add(0, MENU_PREV, 0, R.string.menu_prev).setIcon(android.R.drawable.ic_media_previous);
		menuControl.add(0, MENU_PLAYPAUSE, 0, R.string.menu_playpause).setIcon(android.R.drawable.ic_media_play);
		menuControl.add(0, MENU_REPLAY, 0, R.string.menu_replay).setIcon(android.R.drawable.ic_media_rew);
		menuControl.add(0, MENU_NEXT, 0, R.string.menu_next).setIcon(android.R.drawable.ic_media_next);
    	menu.add(0, MENU_PREFERENCE, 0, R.string.menu_preference).setIcon(android.R.drawable.ic_menu_preferences);
    	menu.add(0, MENU_SLEEPMODE, 0, R.string.menu_sleepmode).setIcon(android.R.drawable.ic_menu_today);
    	SubMenu menuExpansion = menu.addSubMenu(0, MENU_EXPANSION, 0, R.string.menu_expansion).setIcon(android.R.drawable.ic_menu_view);
    	menuExpansion.add(0, MENU_PLAYLIST, 0, R.string.menu_playlist).setIcon(android.R.drawable.ic_menu_manage);
    	menuExpansion.add(0, MENU_DELETE, 0, R.string.menu_delete).setIcon(android.R.drawable.ic_menu_delete);
    	menuExpansion.add(0, MENU_DELETELYRICS, 0, R.string.menu_deletelyrics).setIcon(android.R.drawable.ic_menu_delete);
    	menuExpansion.add(0, MENU_DEBUGINFO, 0, R.string.menu_debuginfo).setIcon(android.R.drawable.ic_menu_info_details);
    	menuExpansion.add(0, MENU_TEST, 0, R.string.menu_test).setIcon(android.R.drawable.ic_menu_info_details);
    	menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_help);
    	menu.add(0, MENU_EXIT, 0, R.string.menu_exit).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case MENU_PREV:
    		mc.prev();
    		break;
    	case MENU_PLAYPAUSE:
    		mc.playpause();
    		break;
    	case MENU_REPLAY:
    		mc.replay();
    		break;
    	case MENU_NEXT:
    		mc.next();
    		break;
    	case MENU_PREFERENCE:
    		Intent intent = new Intent(this, LyricsPreferenceActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		startActivityForResult(intent, RESULT_PREFERENCE);
    		break;
    	case MENU_SLEEPMODE:
    		showDialog(DIALOG_SLEEPMODE);
    		break;
    	case MENU_ABOUT:
    		showDialog(DIALOG_ABOUT);
    		break;
    	case MENU_EXIT:
        	exitActivity();
    		break;
    	case MENU_DELETE:
    		if (mc != null && mc.isPlaying())
    			showDialog(DIALOG_DELETE);
    		else
    			Toast.makeText(this, R.string.delete_require, Toast.LENGTH_SHORT).show();
    		break;
    	case MENU_DELETELYRICS:
    		if (mc != null && mc.isPlaying())
    			if (mc.lyrics != null)
    				showDialog(DIALOG_DELETELYRICS);
    			else
    				Toast.makeText(this, R.string.deletelyrics_notexist, Toast.LENGTH_SHORT).show();
    		else
    			Toast.makeText(this, R.string.deletelyrics_require, Toast.LENGTH_SHORT).show();
    		break;
    	case MENU_DEBUGINFO:
    		if (mc != null)
    			showDialog(DIALOG_DEBUGINFO);
    		break;
    	case MENU_TEST:
    		Intent i = new Intent(Intent.ACTION_PICK);
    		i.setType(MediaStore.Audio.Playlists.CONTENT_TYPE);
    		startActivityForResult(i, 2);
    		break;
    	}
    	return false;
    }
    
    @Override
	public void onBackPressed() {
		if ("hide".equalsIgnoreCase(Preference.backKey))
			this.moveTaskToBack(true);
		else if ("exit".equalsIgnoreCase(Preference.backKey))
			exitActivity();
		else
			showDialog(DIALOG_BACKKEY);
	}
    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (Preference.enableGesture)
			return gestureDetector.onTouchEvent(event);
		else
			return super.onTouchEvent(event);
	} 

	@Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case DIALOG_SLEEPMODE:
			return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_menu_today)
				.setTitle(R.string.menu_sleepmode)
				.setSingleChoiceItems(R.array.entries_sleepmode, 0, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						sleepModeDelay = getResources().getIntArray(R.array.entriesvalue_sleepmode)[item];
					}
				})
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (sleepModeDelay > 0) {
							sleepModeTimer.cancel();
							sleepModeTimer = new Timer();
							sleepModeTime = new Date(new Date().getTime() + sleepModeDelay);
							sleepModeTimer.schedule(new TimerTask() {
					            public void run() {
					            	sleepModeTime = null;
									mc.stop();
									exitActivity();
					            }
					    	}, sleepModeDelay);
						}
						else {
				        	sleepModeTimer.cancel();
							sleepModeTimer = new Timer();
				        	sleepModeTime = null;
						}
					}
				})
				.setNegativeButton(R.string.dialog_cancel, null)
				.create();
   		case DIALOG_DEBUGINFO:
   			return new AlertDialog.Builder(this)
   				.setIcon(android.R.drawable.ic_dialog_info)
   				.setTitle(R.string.menu_expansion)
   				.setMessage("DIALOG_DEBUGINFO")
   				.setPositiveButton(R.string.dialog_ok, null)
   				.create();
  		case DIALOG_DELETE:
   			return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.menu_delete)
				.setMessage("DIALOG_DELETE")
				.setPositiveButton(R.string.dialog_ok,  new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (mc.delete(pendingDelete))
							Toast.makeText(MainActivity.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(MainActivity.this, R.string.delete_failure, Toast.LENGTH_SHORT).show();
					}
				})
				.setNegativeButton(R.string.dialog_cancel, null)
				.create();
  		case DIALOG_DELETELYRICS:
   			return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.menu_deletelyrics)
				.setMessage("DIALOG_DELETELYRICS")
				.setPositiveButton(R.string.dialog_ok,  new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (mc.deleteLyrics(pendingDelete))
							Toast.makeText(MainActivity.this, R.string.deletelyrics_success, Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(MainActivity.this, R.string.deletelyrics_failure, Toast.LENGTH_SHORT).show();
					}
				})
				.setNegativeButton(R.string.dialog_cancel, null)
				.create();
   		case DIALOG_BACKKEY:
   			return new AlertDialog.Builder(this)
	   			.setIcon(android.R.drawable.ic_dialog_info)
	   			.setTitle(R.string.pref_global_backkey)
	   			.setMessage(R.string.backkey_message)
	   			.setPositiveButton(R.string.backkey_hide, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						MainActivity.this.moveTaskToBack(true);
					}
				})
	   			.setNegativeButton(R.string.backkey_exit,  new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						exitActivity();
					}
				})
	   			.create();
   		case DIALOG_ABOUT:
   			return new AlertDialog.Builder(this)
   				.setTitle(R.string.menu_about)
   				.setMessage(R.string.app_message)
   				.setPositiveButton(R.string.dialog_ok, null)
   				.create();
		default:
    		return null;
    	}
    }
    
    @Override
	protected void onPrepareDialog(int id, Dialog dialog) {
    	switch (id) {
   		case DIALOG_DELETE:
   			pendingDelete = mc.mediaPath;
   			((AlertDialog)dialog).setMessage(String.format(getString(R.string.delete_message), pendingDelete));
   			break;
   		case DIALOG_DELETELYRICS:
   			pendingDelete = mc.lyrics.lyricsPath;
   			((AlertDialog)dialog).setMessage(String.format(getString(R.string.deletelyrics_message), pendingDelete));
   			break;
   		case DIALOG_DEBUGINFO:
   			((AlertDialog)dialog).setMessage(
				String.format(getString(R.string.debug_string), 
				mc.mediaId, mc.mediaTitle, mc.mediaArtist, mc.mediaAlbum,
				mc.getCurrentPosition(), mc.mediaDuration, mc.mediaPath,
				notiLyrics, (mc.lyrics==null?getString(R.string.lyrics_not_exist):mc.lyrics.lyricsPath),
				(mc.lyrics==null?getString(R.string.lyrics_not_exist):mc.lyrics.encoding),
				sleepModeTime));
   			break;
    	}
    	super.onPrepareDialog(id, dialog);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_PREFERENCE) {
			updatePreference();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void onMediaChanged() {
		notiTitle = (Preference.showPosition ? String.format("[%s/%s] ", millsToString(mc.getCurrentPosition()) ,millsToString(mc.mediaDuration)) : "")
		+ (sleepModeTime != null ? String.format(getString(R.string.sleepmode_remaining), (sleepModeTime.getTime() - new Date().getTime()) / 60000) : String.format("%s", mc.mediaTitle));
		notiLyrics = mc.mediaTitle + " " + mc.mediaArtist;
		// 更新Activity界面
		if (mc.mediaAlbumArtPath != null) {
			imgAlbumArt.setImageURI(Uri.parse(mc.mediaAlbumArtPath));
			if (Preference.showAnimation) {
				int[] anims = {R.anim.albumart1, R.anim.albumart2, R.anim.albumart3, R.anim.albumart4};
				imgAlbumArt.startAnimation(AnimationUtils.loadAnimation(this, anims[new Random().nextInt(anims.length)]));
			}
		} else imgAlbumArt.setImageURI(null);
		setTitle(String.format(getString(R.string.main_titlebar), mc.mediaTitle, mc.mediaArtist));
		txtLyricsPrev.setText(LyricsSplitter.split((mc.lyrics==null?getString(R.string.lyrics_not_exist):""), 20));
		txtLyricsCurr.setText(LyricsSplitter.split(mc.mediaTitle, 16));
		txtLyricsNext.setText(LyricsSplitter.split(mc.mediaArtist, 16));

		// 更新桌面小工具
       	RemoteViews widgetViews = new RemoteViews(getPackageName(), R.layout.appwidget);
	   	widgetViews.setTextViewText(R.id.widgettextprev, LyricsSplitter.split((mc.lyrics==null?getString(R.string.lyrics_not_exist):""), 20));
		widgetViews.setTextViewText(R.id.widgettextcurr, LyricsSplitter.split(mc.mediaTitle, 16));
	   	widgetViews.setTextViewText(R.id.widgettextnext, LyricsSplitter.split(mc.mediaArtist, 16));
	   	widgetViews.setOnClickPendingIntent(R.id.widgettextprev, n.contentIntent);
	   	widgetViews.setOnClickPendingIntent(R.id.widgettextcurr, n.contentIntent);
	   	widgetViews.setOnClickPendingIntent(R.id.widgettextnext, n.contentIntent);
       	ComponentName thisWidget = new ComponentName(this, LyricsWidget.class);
       	AppWidgetManager manager = AppWidgetManager.getInstance(this);
       	manager.updateAppWidget(thisWidget, widgetViews);
       	
       	n.setLatestEventInfo(MainActivity.this, notiTitle, notiLyrics, n.contentIntent);
       	nm.notify(0, n);
	}
	
	private void onPositionChanged() {
		// 更新通知栏
		notiTitle = (Preference.showPosition ? String.format("[%s/%s] ", millsToString(mc.getCurrentPosition()) ,millsToString(mc.mediaDuration)) : "")
			+ (sleepModeTime != null ? String.format(getString(R.string.sleepmode_remaining), (sleepModeTime.getTime() - new Date().getTime()) / 60000) : String.format("%s", mc.mediaTitle));
		if (Preference.tickerText) n.tickerText = notiLyrics + spacer();
       	n.setLatestEventInfo(MainActivity.this, notiTitle, notiLyrics, n.contentIntent);
       	nm.notify(0, n);	
	}
	
	private void onLyricsChanged() {
		// 更新通知栏
		notiLyrics = mc.mediaLyricsContext.curr;
		n.tickerText = Preference.tickerText ? notiLyrics + spacer() : "";
       	n.setLatestEventInfo(MainActivity.this, notiTitle, notiLyrics, n.contentIntent);
       	nm.notify(0, n);

   		// 更新Activity界面
   		txtLyricsPrev.setText(LyricsSplitter.split(mc.mediaLyricsContext.prev, 16));
   		txtLyricsCurr.setText(LyricsSplitter.split(notiLyrics, 20));
   		txtLyricsNext.setText(LyricsSplitter.split(mc.mediaLyricsContext.next, 16));
   		if (Preference.showAnimation) {
			int[] anims = {R.anim.lyrics1, R.anim.lyrics2, R.anim.lyrics3};
	   		txtLyricsPrev.startAnimation(AnimationUtils.loadAnimation(this, R.anim.flyout));
	   		txtLyricsCurr.startAnimation(AnimationUtils.loadAnimation(this, anims[new Random().nextInt(anims.length)]));
	   		txtLyricsNext.startAnimation(AnimationUtils.loadAnimation(this, R.anim.flyin));
   		}
   		
   		// 更新桌面小工具
       	RemoteViews widgetViews = new RemoteViews(getPackageName(), R.layout.appwidget);
       	widgetViews.setTextViewText(R.id.widgettextprev, LyricsSplitter.split(mc.mediaLyricsContext.prev, 16));
       	widgetViews.setTextViewText(R.id.widgettextcurr, LyricsSplitter.split(notiLyrics, 20));
       	widgetViews.setTextViewText(R.id.widgettextnext, LyricsSplitter.split(mc.mediaLyricsContext.next, 16));
	   	widgetViews.setOnClickPendingIntent(R.id.widgettextprev, n.contentIntent);
	   	widgetViews.setOnClickPendingIntent(R.id.widgettextcurr, n.contentIntent);
	   	widgetViews.setOnClickPendingIntent(R.id.widgettextnext, n.contentIntent);
       	ComponentName thisWidget = new ComponentName(this, LyricsWidget.class);
       	AppWidgetManager manager = AppWidgetManager.getInstance(this);
       	manager.updateAppWidget(thisWidget, widgetViews);
    }
    
    private String millsToString(long mills) {
		return String.format("%d:%02d", mills / 60000, mills / 1000 % 60);
    }
    
	boolean space = false;
    private String spacer() {
    	space = !space;
    	return space? " " : "";
    }
    
    private void updatePreference() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	Preference.showAnimation = prefs.getBoolean("showanimation", true);
    	Preference.enableGesture = prefs.getBoolean("enablegesture", true);
    	Preference.fullScreen = prefs.getBoolean("fullscreen", true);
    	Preference.showPosition = prefs.getBoolean("showposition", true);
    	Preference.tickerText = prefs.getBoolean("tickertext", false);
    	Preference.keepScreen = prefs.getBoolean("keepscreen", true);
    	Preference.backKey = prefs.getString("backkey", "alert");
    	if (Preference.keepScreen) {
    		if (!wakelock.isHeld()) wakelock.acquire();
    	} else {
    		if (wakelock.isHeld()) wakelock.release();
    	}
    	if (Preference.fullScreen) {
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    } else {
	        getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}

    	MediaConnection.Preference.refreshRate = Long.valueOf(prefs.getString("refreshrate", "200"));

    	LyricsSplitter.Preference.splitLyrics = prefs.getBoolean("splitlyrics", true);
    	LyricsSplitter.Preference.density = getResources().getDisplayMetrics().density;
    	
    	LyricsParser.Preference.doubleLine = prefs.getBoolean("doubleline", true);
    	LyricsParser.Preference.skipBlank = prefs.getBoolean("skipblank", true);
    	LyricsParser.Preference.offset = Long.valueOf(prefs.getString("offset", "0"));
    	LyricsParser.Preference.charset = prefs.getString("chatset", "auto");
    }

    private void exitActivity() {
		finish();    	
    }

    private class ControlGestureDetector extends SimpleOnGestureListener {
    	private static final int SWIPE_MIN_DISTANCE = 120;
    	private static final int SWIPE_MAX_DISTANCE = 80;
    	private static final int SWIPE_THRESHOLD_VELOCITY = 160;

//		@Override
//		public boolean onDown(MotionEvent e) {
//			imgAlbumArt.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.trembling));
//			return super.onDown(e);
//		}
//
//		@Override
//		public boolean onSingleTapUp(MotionEvent e) {
//			imgAlbumArt.setAnimation(null);
//			return super.onSingleTapUp(e);
//		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,	float velocityY) {
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && 
					Math.abs(e1.getY() - e2.getY()) < SWIPE_MAX_DISTANCE && 
					Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(MainActivity.this, R.string.menu_next, Toast.LENGTH_SHORT).show();
				mc.next();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && 
					Math.abs(e1.getY() - e2.getY()) < SWIPE_MAX_DISTANCE && 
					Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(MainActivity.this, R.string.menu_prev, Toast.LENGTH_SHORT).show();
				mc.prev();
			} else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && 
					Math.abs(e1.getX() - e2.getX()) < SWIPE_MAX_DISTANCE && 
					Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(MainActivity.this, R.string.menu_playpause, Toast.LENGTH_SHORT).show();
				mc.playpause();
			} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && 
					Math.abs(e1.getX() - e2.getX()) < SWIPE_MAX_DISTANCE && 
					Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(MainActivity.this, R.string.menu_replay, Toast.LENGTH_SHORT).show();
				mc.replay();
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}
    }

    public static class Preference {
    	static boolean showAnimation = true;
    	static boolean enableGesture = true;
    	static boolean fullScreen = true;
    	static boolean showPosition = true;
    	static boolean tickerText = false;
    	static boolean keepScreen = true;
    	static String backKey = "alert";
    }
}