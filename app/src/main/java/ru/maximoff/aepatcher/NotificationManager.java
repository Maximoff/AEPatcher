package ru.maximoff.aepatcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

public class NotificationManager {
	private android.app.NotificationManager mManager;
	private Notification.Builder mBuilder;
	private Context context;
	private String notificationId;
	private int notifyID;

	public NotificationManager(Context ctx) {
		this.context = ctx;
		this.mManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		this.notificationId = "proccess_finished";
		this.notifyID = 1;
	}

	public void setId(int id) {
		this.notifyID = id;
	}

	public void setNotifiId(String id) {
		this.notificationId = id;
	}

	public void init(CharSequence name, CharSequence desc) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(notificationId, name, android.app.NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription(desc.toString());
			channel.enableLights(false);
			channel.enableVibration(false);
			channel.setSound(null, null);
			mManager.createNotificationChannel(channel);
		} 
	}

	public Notification notify(String title, String content) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mBuilder = new Notification.Builder(context, notificationId);
		} else {
			mBuilder = new Notification.Builder(context);
		}
		mBuilder.setContentTitle(title);
		mBuilder.setWhen(System.currentTimeMillis());
		mBuilder.setContentText(content);
		mBuilder.setAutoCancel(false);
		mBuilder.setSmallIcon(R.drawable.ic_notification);
		mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(contentIntent);
		Notification notification = mBuilder.build();
		mManager.notify(notifyID, notification);
		return notification;
	}

	public void cancel() {
		mManager.cancel(notifyID);
	}

	public Notification update(String title, String message) {
		if (mBuilder != null) {
			mBuilder.setContentTitle(title);
			mBuilder.setContentText(message);
			Notification n = mBuilder.build();
			mManager.notify(notifyID, n);
			return n;
		} else {
			return notify(title, message);
		}
	}
}


