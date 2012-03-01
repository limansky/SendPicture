package org.limansky;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.Toast;

public class SendPictureActivity extends Activity {

	private final int IMAGE_CAPTURE_REQUEST_CODE = 100;
	private final int IMAGE_PICK_REQUEST_CODE = 101;
	private final String LOG_NAME = "SendPicture";

	private ImageAdapter images;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		images = (ImageAdapter)getLastNonConfigurationInstance();

		if (null == images) images = new ImageAdapter(this);

		final Gallery g = (Gallery)findViewById(R.id.imageGallery);
		g.setAdapter(images);
		registerForContextMenu(g);

		final Button shotBtn = (Button)findViewById(R.id.makePhotoButton);
		shotBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				takePhoto();
			}
		});

		final Button loadBtn = (Button) findViewById(R.id.loadImageButton);
		loadBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				loadImage();
			}
		});

		final Button sendBtn = (Button)findViewById(R.id.sendButton);
		sendBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				sendProtocol();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater m = getMenuInflater();
		m.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		final MenuItem sbm = menu.findItem(R.id.send_by_mail_menu);
		sbm.setEnabled(!images.isEmpty());

		return super.onPrepareOptionsMenu(menu);
	}

	private void takePhoto() {
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(i, IMAGE_CAPTURE_REQUEST_CODE);
	}

	private void loadImage() {
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, IMAGE_PICK_REQUEST_CODE);
	}

	private void onImageCaptured(int resultCode, Intent data) {
		if (RESULT_OK == resultCode) {
			setImageFile(data.getData());

		} else if (RESULT_CANCELED != resultCode) {
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setMessage("Failed to get image.");

			b.create().show();
		}
	}

	private void onImagePicked(int resultCode, Intent data) {
		if (RESULT_OK == resultCode) {
			setImageFile(data.getData());
		}
	}

	private void setImageFile(Uri uri) {
		Log.d(LOG_NAME, "setImageFile: " + uri.toString());
		images.addItem(uri);

		final Button sendBtn = (Button) findViewById(R.id.sendButton);
		sendBtn.setEnabled(true);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return images;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case IMAGE_CAPTURE_REQUEST_CODE:
			onImageCaptured(resultCode, data);
			break;

		case IMAGE_PICK_REQUEST_CODE:
			onImagePicked(resultCode, data);
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.options_menu:
			editOptions();
			return true;

		case R.id.send_by_mail_menu:
			sendByMail();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void editOptions() {
		Log.d(LOG_NAME, "before create");
		Intent i = new Intent(this, SettingsActivity.class);
		Log.d(LOG_NAME, "before start act");
		startActivity(i);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		//menu.add(R.string.delete);
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gallery_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			images.removeItem((int)info.id);
			return true;
		} else {
			return super.onContextItemSelected(item);
		}
	}

	private void sendByMail() {
		Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
		i.setType("text/plain");
		i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, images.getUris());
		startActivity(i);
	}

	private void sendProtocol() {;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		final String url = prefs.getString("hostnamePref", "http://borki67km.ru/67/tst_upload.php");
		final String uname = prefs.getString("loginPref", "");
		final String passwd = prefs.getString("passPref", "");

		HttpClient client = new DefaultHttpClient();

		try {
			List<Uri> uris = images.getUris();

			HttpPost post = new HttpPost(url);
			MultipartEntity muentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			post.setHeader("user", uname);
			post.setHeader("password", passwd);

			for (int i = 0; i < uris.size(); i++) {

				final byte [] data = IOUtils.toByteArray(getContentResolver().openInputStream(uris.get(i)));
				final String type = getContentResolver().getType(uris.get(i));

				muentity.addPart("file" + i, new ByteArrayBody(data, type, "file" + i + ".jpg"));
			}

			post.setEntity(muentity);
			HttpResponse r =  client.execute(post);
			Log.d(LOG_NAME, "HTTP Status=" + r.getStatusLine().getStatusCode());

		} catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), R.string.send_error, Toast.LENGTH_LONG);
		}
	}
}
