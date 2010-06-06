package com.toraleap.lyrics;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class LyricsWidget extends AppWidgetProvider {

	@Override
	public void onEnabled(Context context) {
       	Intent intent = new Intent();
       	intent.setClass(context, MainActivity.class);
       	intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
       	final PendingIntent appIntent = PendingIntent.getActivity(context, 0, intent, 0);
       	RemoteViews widgetViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);
	   	widgetViews.setOnClickPendingIntent(R.id.widgettextprev, appIntent);
	   	widgetViews.setOnClickPendingIntent(R.id.widgettextcurr, appIntent);
	   	widgetViews.setOnClickPendingIntent(R.id.widgettextnext, appIntent);
       	ComponentName thisWidget = new ComponentName(context, LyricsWidget.class);
       	AppWidgetManager manager = AppWidgetManager.getInstance(context);
       	manager.updateAppWidget(thisWidget, widgetViews);
       	super.onEnabled(context);
	}

}
