package ru.maximoff.aepatcher;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.view.WindowManager;
import java.lang.reflect.Method;
import java.util.Locale;

public class Utils {
	public static int dpAsPx(Context context, float dp) {
		float scale = context.getResources().getDisplayMetrics().density;
		return Math.round(dp * scale);
	}
	
	public static void loadLanguage(Context context, String language) {
		Locale locale;
		if (language.equals("en")) {
			locale = Locale.ROOT;
		} else {
			locale = new Locale(language);
		}
		Locale.setDefault(locale);
		Configuration configuration;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
			configuration = new Configuration(context.getResources().getConfiguration());
			configuration.setLocale(locale);
			configuration.setLayoutDirection(locale);
		} else {
			configuration = context.getResources().getConfiguration();
			configuration.locale = locale;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				configuration.setLayoutDirection(locale);
			}
		}
		context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
	}
	
    // нормальные люди используют Environment.isExternalStorageManager()
	public static boolean isExternalStorageManager()  {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return true;
        }
		try {
			Method m = Class.forName(Environment.class.getName()).getDeclaredMethod("isExternalStorageManager");
			m.setAccessible(true);
			return (boolean) m.invoke(null);
		} catch (Exception ignore) {
			return false;
		}
	}
	
	public static void toggleScreenOn(Activity act, boolean screenOn) {
		if (screenOn) {
			act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
	
	public static int getColor(Context ctx, int color) {
		if (Build.VERSION.SDK_INT >= 23) {
			return ctx.getColor(color);
		}
		return ctx.getResources().getColor(color);
	}
	
	public static String formatTime(long ms) {
		long millis = ms % 1000;
		long second = (ms / 1000) % 60;
		long minute = (ms / (1000 * 60)) % 60;
		long hour = (ms / (1000 * 60 * 60)) % 24;
		return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
	}
}