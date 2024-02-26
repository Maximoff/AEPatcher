package ru.maximoff.aepatcher;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import java.util.List;

public class CreateHelpShortcut extends Activity {
	private final int CREATE_SHORTCUT_CODE = 1117;
	private String receiverId;

    private void finishActivity(String result) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
		if (Build.VERSION.SDK_INT >= 21) {
			finishAndRemoveTask();
		} else {
			finish();
		}
    }

	private class WaitFor extends AsyncTask<Void,Void,Void> {
        final int waitPeriod;

        private WaitFor(int n) {
            waitPeriod = n;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(waitPeriod);
                Intent bi = new Intent(receiverId);
                sendBroadcast(bi);
            } catch (Exception e) {}
            return null;
        }
    }

    private void createShortcut26() {
		ShortcutManager sm = getSystemService(ShortcutManager.class);
		if (sm != null && sm.isRequestPinShortcutSupported()) {
			boolean shortcutExists = false;
			List<ShortcutInfo> shortcuts = sm.getPinnedShortcuts();
			for (int i = 0; i < shortcuts.size() && !shortcutExists; i++) {
				shortcutExists = shortcuts.get(i).getId().equals(receiverId);
			}
			if (shortcutExists) {
				finishActivity(getString(R.string.shortcut_exists));
			} else {
				Intent broadcastIntent = new Intent(receiverId);
				broadcastIntent.putExtra("message", getString(R.string.success));
				final WaitFor waitFor = new WaitFor(1000);
				waitFor.execute();
				registerReceiver(new BroadcastReceiver() {
						@Override
						public void onReceive(Context c, Intent intent) {
							if (intent.hasExtra("message")) {
								String msg = intent.getStringExtra("message");
								unregisterReceiver(this);
								waitFor.cancel(true);
								finishActivity(msg);
							}
						}
					},
					new IntentFilter(receiverId)
				);
				Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
				shortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);
				shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				shortcutIntent.setDataAndType(Uri.parse(""), MainActivity.INTENT_TYPE);
				shortcutIntent.putExtra("showHelp", true);
				shortcutIntent.setPackage(getPackageName());
				ShortcutInfo shortcutInfo = new ShortcutInfo
					.Builder(this, receiverId)
					.setShortLabel(getText(R.string.patch_help))
					.setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
					.setIntent(shortcutIntent)
					.build();
				PendingIntent successCallback = PendingIntent.getBroadcast(this, CREATE_SHORTCUT_CODE, broadcastIntent, 0);
				sm.requestPinShortcut(shortcutInfo, successCallback.getIntentSender());
			}
		}
    }

    private void createShortcut() {
		try {
			Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
			shortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);
			shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			shortcutIntent.setDataAndType(Uri.parse(""), MainActivity.INTENT_TYPE);
			shortcutIntent.putExtra("showHelp", true);
			shortcutIntent.setPackage(getPackageName());
			Intent addIntent = new Intent();
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.patch_help));
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));
			addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
			addIntent.putExtra("duplicate", false);
			sendBroadcast(addIntent);
			finishActivity(getString(R.string.success));
		} catch (Exception e) {
			finishActivity(getString(R.string.error));
		}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		receiverId = getPackageName() + ".SHORTCUT." + CREATE_SHORTCUT_CODE;
        if (Build.VERSION.SDK_INT >= 26) {
            createShortcut26();
        } else {
            createShortcut();
		}
    }
}
