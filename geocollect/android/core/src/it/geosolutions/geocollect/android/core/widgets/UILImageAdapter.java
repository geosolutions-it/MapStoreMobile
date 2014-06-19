package it.geosolutions.geocollect.android.core.widgets;

import it.geosolutions.geocollect.android.core.R;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class UILImageAdapter extends BaseAdapter {

	private Context context;
	private String[] imageUrls;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	static class ViewHolder {
		ImageView imageView;
		ProgressBar progressBar;
	}

	public UILImageAdapter(Context context, String[] imageUrls, DisplayImageOptions options){
		super();
		this.context = context;
		this.imageLoader = ImageLoader.getInstance();
		
		// TODO: this code should be run by the Application onCreate() callback, that is not executed at the moment
		// The AndroidManifest and the Application class must be tuned for that to work
		if(!this.imageLoader.isInited()){
			Log.w("UILLoader: ", "ImageLoader was not initialized, init in progress..");
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
			.threadPriority(Thread.NORM_PRIORITY - 2)
			.threadPoolSize(5)
			.memoryCacheExtraOptions(640, 480)
			//.denyCacheImageMultipleSizesInMemory()
			.diskCacheFileNameGenerator(new Md5FileNameGenerator())
			.tasksProcessingOrder(QueueProcessingType.FIFO)
			.writeDebugLogs() //  TODO: Remove for release app
			.build();
			this.imageLoader.init(config);
		}
		this.setImageUrls(imageUrls);
		this.options = options;
	}
	
	@Override
	public int getCount() {
		return getImageUrls().length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		View view = convertView;
		if (view == null) {
			// TODO: Null checks on this line
			view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.uil_item_grid_image, parent, false);
			holder = new ViewHolder();
			assert view != null;
			holder.imageView = (ImageView) view.findViewById(R.id.image);
			holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		imageLoader.displayImage(getImageUrls()[position], holder.imageView, options, new SimpleImageLoadingListener() {
									 @Override
									 public void onLoadingStarted(String imageUri, View view) {
										 holder.progressBar.setProgress(0);
										 holder.progressBar.setVisibility(View.VISIBLE);
									 }

									 @Override
									 public void onLoadingFailed(String imageUri, View view,
											 FailReason failReason) {
										 holder.progressBar.setVisibility(View.GONE);
									 }

									 @Override
									 public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
										 holder.progressBar.setVisibility(View.GONE);
									 }
								 }, new ImageLoadingProgressListener() {
									 @Override
									 public void onProgressUpdate(String imageUri, View view, int current,
											 int total) {
										 holder.progressBar.setProgress(Math.round(100.0f * current / total));
									 }
								 }
		);

		return view;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	public String[] getImageUrls() {
		return imageUrls;
	}

	public void setImageUrls(String[] imageUrls) {
		this.imageUrls = imageUrls;
	}
}
