package ru.maximoff.aepatcher;

import android.os.AsyncTask;
import com.gmail.heagoo.common.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ManifestParser extends AsyncTask<String,Void,Boolean> {
	private MainActivity main;

	public ManifestParser(MainActivity m) {
		main = m;
	}

	@Override
	protected Boolean doInBackground(String[] p1) {
		InputStream fis = null;
		try {
			File file = new File(p1[0]);
			String prefix = parsePrefix(file);
			fis = new FileInputStream(file);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(fis);
			Element mf = (Element) document.getElementsByTagName("manifest").item(0);
			if (mf.hasAttribute("package")) {
				main.setPackageName(mf.getAttribute("package"));
				Element app = (Element) mf.getElementsByTagName("application").item(0);
				if (app.hasAttribute(prefix + "name")) {
					main.setAppName(app.getAttribute(prefix + "name"));
				}
				NodeList activities = app.getElementsByTagName("activity");
				for (int i = 0; i < activities.getLength(); i++) {
					Element act = (Element) activities.item(i);
					if (act.hasAttribute(prefix + "name")) {
						String name = act.getAttribute(prefix + "name");
						main.addActivity(name);
						NodeList sub = act.getElementsByTagName("category");
						for (int j = 0; j < sub.getLength(); j++) {
							Element cat = (Element) sub.item(j);
							if (cat.hasAttribute(prefix + "name") && "android.intent.category.LAUNCHER".equals(cat.getAttribute(prefix + "name"))) {
								main.addActivityLaunch(name);
							}
						}
					}
				}
				NodeList aliases = app.getElementsByTagName("activity-alias");
				for (int i = 0; i < aliases.getLength(); i++) {
					Element ali = (Element) aliases.item(i);
					if (ali.hasAttribute(prefix + "targetActivity")) {
						String name = ali.getAttribute(prefix + "targetActivity");
						NodeList sub = ali.getElementsByTagName("category");
						for (int j = 0; j < sub.getLength(); j++) {
							Element cat = (Element) sub.item(j);
							if (cat.hasAttribute(prefix + "name") && "android.intent.category.LAUNCHER".equals(cat.getAttribute(prefix + "name"))) {
								main.addActivityLaunch(name);
							}
						}
					}
				}
			}
			return true;
		} catch (Exception e) {}
		finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {}
			}
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		main.startPatch(result);
	}

	private String parsePrefix(File f) {
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			String str = IOUtils.readString(is);
			Pattern p = Pattern.compile("\\sxmlns:(.+?)=\"http://schemas.android.com/apk/res/android\"");
			Matcher m = p.matcher(str);
			if (m.find()) {
				return m.group(1) + ":";
			}
		} catch (Exception e) {}
		finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {}
			}
		}
		return "android:";
	}
}
