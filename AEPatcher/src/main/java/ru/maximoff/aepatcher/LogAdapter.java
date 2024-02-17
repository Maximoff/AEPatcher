package ru.maximoff.aepatcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import android.text.SpannableString;
import android.widget.ListView;
import android.os.Handler;

public class LogAdapter extends BaseAdapter {
	private List<SpannableString> items = new ArrayList<>();
	private Context context;
	private ListView listView;

	public LogAdapter(Context ctx, ListView lv) {
		context = ctx;
		listView = lv;
	}

	public void add(String s) {
		items.add(new SpannableString(s));
		notifyDataSetChanged();
	}

	public void add(SpannableString s) {
		items.add(s);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int p1) {
		return items.get(p1);
	}

	@Override
	public long getItemId(int p1) {
		return p1 * 17;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					listView.setSelection(getCount() - 1);
				}
			}, 100L);
	}

	@Override
	public View getView(int p1, View p2, ViewGroup p3) {
		ViewHolder holder;
		if (p2 == null) {
			p2 = LayoutInflater.from(context).inflate(R.layout.log_item, null);
			holder = new ViewHolder();
			holder.text = p2.findViewById(R.id.logitemTextView1);
			p2.setTag(holder);
		} else {
			holder = (ViewHolder) p2.getTag();
		}
		holder.text.setText(items.get(p1));
		return p2;
	}

	private class ViewHolder {
		public TextView text;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < items.size(); i++) {
			if (i > 0) {
				sb.append("\n");
			}
			sb.append(items.get(i));
		}
		return sb.toString();
	}
}
