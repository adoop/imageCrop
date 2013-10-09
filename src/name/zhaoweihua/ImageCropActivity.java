
/**
 * First editor zhaoweihua
 * Update by Lin Alabama  2013-10-09
 * update:add auto create image file to  /CropImage  return image path to Cropback
 * mail:addw806@aliyun.com
 */

package name.zhaoweihua;

import java.io.File;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * @author ZWH first editor
 * 
 */
public class ImageCropActivity extends Activity {
	private Context m_context;
	private Button btn_choose;
	private ImageView img_shower;
	/** message id: as photo get picked src path */
	private static final int PHOTO_PICKED_WITH_DATA = 0x10;
	/** message id: get check id crop data */
	private static final int PHOTO_CROP_DATA = 0x11;
	/** message id: async load dialog */
	protected static final int SHOW_LOAD_DIALOG = 0x201;
	/** message id: dismiss async load dialog */
	protected static final int DISMISS_LOAD_DIALOG = 0x202;
	/** tips async load dialog */
	private ProgressDialog myLoadProgressDialog;
	/**
	 * this handler operate UI before async load image
	 */
	private Handler myViewhandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SHOW_LOAD_DIALOG:
				myLoadProgressDialog = new ProgressDialog(m_context);
				myLoadProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_SPINNER);//

				myLoadProgressDialog
						.setMessage("loading image please wait ...");
				myLoadProgressDialog.setIndeterminate(false);
				myLoadProgressDialog.show();
				break;
			case DISMISS_LOAD_DIALOG:
				myLoadProgressDialog.dismiss();
				break;
			default:
				break;
			}

		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		m_context = ImageCropActivity.this;

		findViews();
		setListeners();

	}

	/** findViewById */
	private void findViews() {
		btn_choose = (Button) findViewById(R.id.btn_choosePic);
		img_shower = (ImageView) findViewById(R.id.img_shower);

	}

	private void setListeners() {
		btn_choose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickPhotoFromGallery();
			}
		});
	}

	/** startActivityForResult源宒恁寁芞ㄛonActivityResult諉彶殿隙腔芞恅璃 */
	private void pickPhotoFromGallery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/jpeg");
		intent.putExtra("return-data", true);
		startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTO_PICKED_WITH_DATA:

				Uri uri = data.getData();
				Intent intent = new Intent(this,
						name.zhaoweihua.crop.CropImage.class);
				Bundle extras = new Bundle();
				extras.putString("circleCrop", "true");
				extras.putInt("aspectX", 200);
				extras.putInt("aspectY", 200);
				intent.setDataAndType(uri, "image/jpeg");
				intent.putExtras(extras);
				startActivityForResult(intent, PHOTO_CROP_DATA);
				break;
			case PHOTO_CROP_DATA: // show back data

				String srcData = data.getExtras().getString("data-src");
				myViewhandler.sendEmptyMessage(SHOW_LOAD_DIALOG);
				BitmapWorkerTask task = new BitmapWorkerTask(img_shower);
				task.execute(srcData);

			}
		}

	}

	/**
	 * this function is decode big bitmap(uri) as small one bitmap
	 * 
	 * @param src
	 * @param reqWidth
	 * @param reqHeight
	 * @return bitmap
	 */
	public static Bitmap decodeBitmapFromFile(String src,

	int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions

		final BitmapFactory.Options options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(src, options);

		// Calculate inSampleSize

		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		// options.inPreferredConfig =Bitmap.Config.RGB_565;
		// Decode bitmap with inSampleSize set

		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(src, options);

	}

	/**
	 * Calculate inSampleSize
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return int
	 */
	public static int calculateInSampleSize(

	BitmapFactory.Options options, int reqWidth, int reqHeight) {

		// Raw height and width of image

		final int height = options.outHeight;

		final int width = options.outWidth;

		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			if (width > height) {

				inSampleSize = Math.round((float) height / (float) reqHeight);

			} else {

				inSampleSize = Math.round((float) width / (float) reqWidth);

			}

		}

		return inSampleSize;

	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

		private final WeakReference<ImageView> imageViewReference;

		// for define to close avoid warning leak.
		// AlertDialog ImageDialog;
		public BitmapWorkerTask(ImageView imageView) {

			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);

		}

		// Decode image in background.

		@Override
		protected Bitmap doInBackground(String... params) {

			final Bitmap bitmap = decodeBitmapFromFile(params[0], 300, 300);

			return bitmap;

		}

		// Once complete, see if ImageView is still around and set bitmap.

		@Override
		protected void onPostExecute(Bitmap bitmap) {

			if (imageViewReference != null && bitmap != null) {

				final ImageView imageView = imageViewReference.get();

				if (imageView != null) {

					Log.d("imageview------------------",
							"debug view image load");
					imageView.setImageBitmap(bitmap);
					myViewhandler.sendEmptyMessage(DISMISS_LOAD_DIALOG);
				}

			}

		}

	}
}