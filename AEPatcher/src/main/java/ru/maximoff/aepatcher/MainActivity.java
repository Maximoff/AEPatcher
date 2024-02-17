package ru.maximoff.aepatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.gmail.heagoo.apkeditor.ResListAdapter;
import com.gmail.heagoo.apkeditor.patch.IPatchContext;
import com.gmail.heagoo.apkeditor.patch.PatchExecutor;
import com.gmail.heagoo.common.SDCard;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.maximoff.aepatcher.R;

public class MainActivity extends Activity implements IPatchContext {
	private final String ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION = "android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION";
	private final String ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION = "android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION";
	private final int REQUEST_PERM = 1010;
	private final int REQUEST_MANAGER = 1011;
	private final String[] PERMISSIONS = new String[]{"READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"};
	private boolean patchApplied = false;
	private View rootView;
	private Button positiveBtn;
	private Button negativeBtn;
	private LogAdapter adapter;
	private String projectPath;
	private String patchPath;
	private String apkPath;
	private RotateAnimation rotation;
	private long timeSpent;

	private String packageName = null;
	private String applicationName = null;
	private List<String> activities = new ArrayList<>();
	private List<String> activitiesLaunch = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		onNewIntent(getIntent());
    }

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent == null || intent.getData() == null || intent.getType() == null) {
			Toast.makeText(this, R.string.intent_error, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (intent.getType().equals("application/ru.maximoff.aepatcher-patch")) {
			int appTheme = intent.getIntExtra("appTheme", 0);
			int themeId, iconId;
			switch (appTheme) {
				case 0: // light
				default:
					themeId = R.style.AppTheme;
					iconId = R.drawable.ic_settings;
					break;

				case 1: // dark
					themeId = R.style.AppThemeDark;
					iconId = R.drawable.ic_settings_white;
					break;

				case 2: // black
					themeId = R.style.AppThemeBlack;
					iconId = R.drawable.ic_settings_white;
					break;
			}
			setTheme(themeId);
			if (intent.hasExtra("appLanguage")) {
				String language = intent.getStringExtra("appLanguage");
				Utils.loadLanguage(this, language);
			}
			if (intent.hasExtra("keepScreenOn")) {
				boolean screenOn = intent.getBooleanExtra("keepScreenOn", true);
				Utils.toggleScreenOn(this, screenOn);
			}
			if (intent.hasExtra("projectPath")) {
				projectPath = intent.getStringExtra("projectPath");
			}
			if (intent.hasExtra("patchPath")) {
				patchPath = intent.getStringExtra("patchPath");
			}
			if (intent.hasExtra("apkPath")) {
				apkPath = intent.getStringExtra("apkPath");
			}
			if (projectPath == null || patchPath == null) {
				Toast.makeText(this, R.string.intent_error, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			rootView = getLayoutInflater().inflate(R.layout.main, null);
			final AlertDialog dialog = new AlertDialog.Builder(this)
				.setIcon(iconId)
				.setTitle(R.string.app_name)
				.setView(rootView)
				.setPositiveButton(R.string.cancel, null)
				.setNegativeButton(R.string.copy, null)
				.setNeutralButton(R.string.github, null)
				.setCancelable(false)
				.create();
			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
					@Override
					public void onShow(DialogInterface p1) {
						ImageView dialogIcon = dialog.findViewById(android.R.id.icon);
						if (dialogIcon != null) {
							int icSize = Utils.dpAsPx(MainActivity.this, 24.0f);
							int margin = Utils.dpAsPx(MainActivity.this, 5.0f);
							dialogIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
							MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) dialogIcon.getLayoutParams();
							mp.width = icSize;
							mp.height = icSize;
							mp.setMargins(mp.leftMargin, mp.topMargin, margin, mp.bottomMargin);
							dialogIcon.setLayoutParams(mp);
							dialogIcon.requestLayout();
							rotation = new RotateAnimation(0.0f, 360.0f , Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
							rotation.setInterpolator(new LinearInterpolator());
							rotation.setRepeatCount(Animation.INFINITE);
							rotation.setDuration(2500);
							dialogIcon.setAnimation(rotation);
							dialogIcon.startAnimation(rotation);
						}
						positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
						positiveBtn.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View p1) {
									if (patchApplied) {
										dialog.cancel();
										MainActivity.this.exit();
									} else {
										new AlertDialog.Builder(MainActivity.this)
											.setMessage(R.string.cancel_msg)
											.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface p1, int p2) {
													p1.cancel();
													dialog.cancel();
													MainActivity.this.exit();
												}
											})
											.setNegativeButton(R.string.cancel, null)
											.create()
											.show();
									}
								}
							});
						negativeBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
						negativeBtn.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View p1) {
									ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
									ClipData clip = ClipData.newPlainText(getString(R.string.app_name), adapter.toString());
									clipboard.setPrimaryClip(clip);
								}
							});
						negativeBtn.setVisibility(View.GONE);
						Button git = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
						git.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View p1) {
									Intent i = new Intent(Intent.ACTION_VIEW);
									i.setData(Uri.parse("https://github.com/Maximoff/AEPatcher"));
									startActivity(i);
								}
							});
					}
				});
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface p1) {
						if (rotation != null) {
							rotation.cancel();
						}
					}
				});
			dialog.show();
			TextView textView = rootView.findViewById(R.id.mainTextView1);
			textView.setText(R.string.need_permissions);
			checkPermissions();
		} else {
			Toast.makeText(this, R.string.intent_error, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private void initLayout() {
		rootView.findViewById(R.id.mainTextView1).setVisibility(View.GONE);
		ListView list = rootView.findViewById(R.id.mainListView1);
		list.setVisibility(View.VISIBLE);
		list.setFastScrollEnabled(true);
		adapter = new LogAdapter(this, list);
		list.setAdapter(adapter);
		adapter.add(getString(R.string.project_fmt, projectPath));
		adapter.add(getString(R.string.patch_fmt, patchPath));
		if (apkPath != null) {
			adapter.add(getString(R.string.apk_fmt, apkPath));
		}
		adapter.add("");
		new ManifestParser(this).execute(projectPath + "/AndroidManifest.xml");
	}

	public void startPatch(boolean manifest) {
		timeSpent = System.currentTimeMillis();
		patchExecutor = new PatchExecutor(this, patchPath);
		patchExecutor.applyPatch();
	}

	@Override
	public void onBackPressed() {
		exit();
	}

	public void exit() {
		try {
			String tmpDir = SDCard.makeWorkingDir(this);
			SDCard.deleteDir(new File(tmpDir));
		} catch (Exception e) {}
		if (Build.VERSION.SDK_INT >= 21) {
			finishAndRemoveTask();
		} else {
			super.finish();
		}
		android.os.Process.killProcess(android.os.Process.myPid());
		System.runFinalizersOnExit(true);
		System.exit(0);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
		switch (requestCode) {
			case REQUEST_PERM:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					initLayout();
				} else if (!shouldShowRequestPermissionRationale(permissions[0])) {
					Toast.makeText(this, R.string.need_permissions, Toast.LENGTH_SHORT).show();
					openSettings();
					finish();
				} else {
					checkPermissions();
				}
				break;

			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_MANAGER) {
            checkPermissions();
			return;
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void checkPermissions() {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
			if (!Utils.isExternalStorageManager()) {
				try {
					Intent intent = new Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
					intent.addCategory("android.intent.category.DEFAULT");
					intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
					startActivityForResult(intent, REQUEST_MANAGER);
				} catch (Exception e) {
					try {
						Intent intent = new Intent();
						intent.setAction(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
						startActivityForResult(intent, REQUEST_MANAGER);
					} catch (Exception f) {
						Toast.makeText(this, getString(R.string.general_error, f.toString()), Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				initLayout();
			}
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			List<String> permissions = new ArrayList<>();
			for (String permission : PERMISSIONS) {
				permission = "android.permission." + permission;
				if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
					permissions.add(permission);
				}
			}
			if (permissions.isEmpty()) {
				initLayout();
				return;
			}
			String[] request = permissions.toArray(new String[permissions.size()]);
			requestPermissions(request, REQUEST_PERM);
		} else {
			initLayout();
		}
	}

	private void openSettings() {
		Intent appSettings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
		startActivity(appSettings);
	}

	// ApkEditor methods
	private Map<String, String> globalVariableValues = new HashMap<String, String>();
	private PatchExecutor patchExecutor;
	private ResListAdapter resAdapter;

	public void setPackageName(String n) {
		packageName = n;
	}

	public void setAppName(String n) {
		applicationName = n;
	}

	public void addActivity(String n) {
		if (n.startsWith(".")) {
			n = packageName + n;
		} else if (n.indexOf(".") == -1) {
			n = packageName + "." + n;
		}
		if (!activities.contains(n)) {
			activities.add(n);
		}
	}

	public void addActivityLaunch(String n) {
		if (n.startsWith(".")) {
			n = packageName + n;
		} else if (n.indexOf(".") == -1) {
			n = packageName + "." + n;
		}
		if (!activitiesLaunch.contains(n)) {
			activitiesLaunch.add(n);
		}
	}

	@Override
	public String getDecodeRootPath() {
		return projectPath;
	}

	@Override
	public List<String> getSmaliFolders() {
		List<String> folders = new ArrayList<>();
		File[] dirs = new File(projectPath).listFiles(new FileFilter() {
				@Override
				public boolean accept(File p1) {
					return (p1.isDirectory() && p1.getName().startsWith("smali"));
				}
			});
        if (dirs != null) {
			for (File d : dirs) {
				folders.add(d.getName());
			}
		}
		return folders;
	}

	@Override
	public String getApplicationName() {
		return applicationName;
	}

	@Override
	public List<String> getActivities() {
		return activities;
	}

	@Override
	public List<String> getLauncherActivities() {
		return activitiesLaunch;
	}

	@Override
	public List<String> getPatchNames() {
		if (patchExecutor != null) {
            return patchExecutor.getRuleNames();
        }
		return null;
	}

	@Override
	public void info(int resourceId, boolean bold, boolean red, boolean green, Object... args) {
		String txt = getString(resourceId);
        if (args != null) {
            txt = String.format(txt, args);
        }
        appendText(txt, bold, red, green);
	}

	@Override
	public void info(int resourceId, boolean bold, boolean red, Object... args) {
		String txt = getString(resourceId);
        if (args != null) {
            txt = String.format(txt, args);
        }
        appendText(txt, bold, red, false);
	}

	@Override
	public void info(int resourceId, boolean bold, Object... args) {
		String txt = getString(resourceId);
        if (args != null) {
            txt = String.format(txt, args);
        }
        appendText(txt, bold, false, false);
	}

	@Override
	public void info(String format, boolean bold, Object... args) {
		String txt = format;
        if (args != null) {
            txt = String.format(txt, args);
        }
        appendText(txt, bold, false, false);
	}

	@Override
	public void error(int resourceId, Object... args) {
		String txt = getString(resourceId);
        if (args != null) {
            txt = String.format(txt, args);
        }
        appendText(txt, false, true, false);
	}

	private void appendText(final String txt, final boolean bold, final boolean red, final boolean green) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SpannableString spanString = new SpannableString(txt);
					if (green) {
						ForegroundColorSpan span = new ForegroundColorSpan(
                            Utils.getColor(MainActivity.this, R.color.green_text));
						spanString.setSpan(span, 0, txt.length(),
										   Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} else if (red) {
						ForegroundColorSpan span = new ForegroundColorSpan(
                            Utils.getColor(MainActivity.this, R.color.red_text));
						spanString.setSpan(span, 0, txt.length(),
										   Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					if (bold) {
						StyleSpan span = new StyleSpan(Typeface.BOLD);
						spanString.setSpan(span, 0, txt.length(),
										   Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					adapter.add(spanString);
				}
			});
    }

	@Override
	public void patchFinished() {
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					timeSpent = (System.currentTimeMillis() - timeSpent);
					info(R.string.tspent, false, false, true, Utils.formatTime(timeSpent));
					patchApplied = true;
					positiveBtn.setText(R.string.ok);
					negativeBtn.setVisibility(View.VISIBLE);
					if (rotation != null) {
						rotation.cancel();
					}
				}
			});
	}

	@Override
	public void setVariableValue(String key, String value) {
        globalVariableValues.put(key, value);
    }

    @Override
    public String getVariableValue(String key) {
        return globalVariableValues.get(key);
    }

	public ResListAdapter getResListAdapter() {
		if (resAdapter == null) {
			resAdapter = new ResListAdapter(projectPath);
		}
		return resAdapter;
	}

	public PackageInfo getApkInfo() {
		if (apkPath != null) {
			return getPackageManager().getPackageArchiveInfo(apkPath, 0);
		}
		return null;
	}

	public boolean isDexDecoded() {
		return !getSmaliFolders().isEmpty();
	}

	public void decodeDex(PatchExecutor p0) {
		// dexs not decoded
		error(R.string.general_error, getString(R.string.need_decode));
		patchFinished();
	}

	public void setManifestModified(boolean p0) {
		// ??
	}

	public String getApkPath() {
		return apkPath;
	}
}
