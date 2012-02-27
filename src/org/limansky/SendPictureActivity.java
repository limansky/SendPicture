package org.limansky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SendPictureActivity extends Activity {

	private final int IMAGE_CAPTURE_REQUEST_CODE = 100;
	private final int IMAGE_PICK_REQUEST_CODE = 101;
	private final String CURRENT_URI = "CurrentUri";
	private final String LOG_NAME = "SendPicture";

	private Uri currentUri;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final String uriStr = null == savedInstanceState ? null : savedInstanceState.getString(CURRENT_URI);

        if (null != uriStr) {
        	Log.d(LOG_NAME, uriStr);

        	currentUri = Uri.parse(uriStr);

//	        final ImageView iv = (ImageView) findViewById(R.id.imageViewer);
//	        iv.setImageURI(currentUri);

        } else {
        	Log.d(LOG_NAME, "uriStr is null");
        }


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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater m = getMenuInflater();
    	m.inflate(R.menu.mainmenu, menu);
    	return true;
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
    	//Gallery gal = (Gallery) findViewById(R.id.imageGallery);
    	//iv.setImageURI(uri);
    	//gal.add
    	currentUri = uri;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	if (null != currentUri) {
    		outState.putString(CURRENT_URI, currentUri.toString());
    	}
    	super.onSaveInstanceState(outState);
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
}