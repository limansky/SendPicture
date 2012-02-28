package org.limansky;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	private final List<Uri> images = new ArrayList<Uri>();
	private final Context context;

	public ImageAdapter(Context context) {
		this.context = context;
	}

	public int getCount() {
		return images.size();
	}

	public Object getItem(int index) {
		return index;
	}

	public long getItemId(int index) {
		return index;
	}

	public View getView(int index, View arg1, ViewGroup arg2) {

		ImageView view = new ImageView(context);
		view.setImageURI(images.get(index));

		return view;
	}

	public void addItem(Uri uri) {
		images.add(uri);
		notifyDataSetChanged();
	}

	public void removeItem(int index) {
		images.remove(index);
		notifyDataSetChanged();
	}
}
