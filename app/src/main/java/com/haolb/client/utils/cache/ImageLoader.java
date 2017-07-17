package com.haolb.client.utils.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.haolb.client.R;
import com.haolb.client.app.NetConnChangeReceiver;
import com.haolb.client.utils.QLog;
import com.squareup.picasso.Cache;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;

/**
 * 图片加载封装类 Created by jingmin.xing on 2014/6/11.
 */
public class ImageLoader {
	private static ImageLoader loader;
	private Picasso picasso;
	private Context context;
	private static float PER_CAN_RELEASE = 0.8f;
	/** 3G下默认加载图 **/
	private Bitmap mLoadingBitmap3g;
	private static final boolean ALWAY_DEFAULT = true;
	private QunarImageDownloader qunarImageDownloader;
	private Cache cache;

	public static ImageLoader getInstance(Context context) {
		if (loader == null) {
			synchronized (ImageLoader.class) {
				if (loader == null) {
					loader = new ImageLoader(context);
				}
			}
		}
		return loader;
	}

	private ImageLoader(Context contextInput) {
		context = contextInput.getApplicationContext();
		mLoadingBitmap3g = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.default_avatar);
		Picasso oldPicasso = Picasso.with(context);
		qunarImageDownloader = QunarImageDownloader
				.createQunarImageDownloader(context);
		buildPicasso(context, oldPicasso, qunarImageDownloader);
	}

	/***
	 * 生产picasso实例
	 * 
	 * @param context
	 * @param oldPicasso
	 */
	private void buildPicasso(Context context, Picasso oldPicasso,
			QunarImageDownloader downloader) {
		boolean canModifyCacheFromPicasso = false;
		Field field = null;
		Object obj = null;
		Downloader oldDownloader = null;
		try {
			field = Picasso.class.getDeclaredField("dispatcher");
			field.setAccessible(true);
			// 获取oldPicasso的dispatcher对象
			Object dispatcher = field.get(oldPicasso);
			if (dispatcher != null) {
				// 设置picasso的下载器
				Field downloaderField = dispatcher.getClass().getDeclaredField(
						"downloader");
				if (downloaderField != null) {
					downloaderField.setAccessible(true);
					oldDownloader = (Downloader) downloaderField
							.get(dispatcher);
					if (downloader != null) {
						downloaderField.set(dispatcher, downloader);
						canModifyCacheFromPicasso = true;
					}
				}
			}
		} catch (Exception e) {
            QLog.e(e);
		}
		// cache
		try {
			field = Picasso.class.getDeclaredField("cache");
			field.setAccessible(true);
			obj = field.get(oldPicasso);
			if (obj != null && obj instanceof Cache) {
				this.cache = (Cache) obj;
			}
		} catch (Exception e) {
			QLog.e(e);
		}

		// 如果能够设置oldPicasso的默认下载器为downloader。目的达到后直接返回
		if (canModifyCacheFromPicasso) {
			picasso = oldPicasso;
			return;
		}
		// 如果不能访问oldPicasso的下载器对象，此处代表必须new一个picasso对象，须确保跟oldPicasso对象使用同一个cache，以防止两个picasso对象拥有不同的cache。
		Picasso.Builder builder = new Picasso.Builder(context);
		if (downloader != null) {
			builder.downloader(downloader);
		} else if (oldDownloader != null) {
			builder.downloader(oldDownloader);
		}
		if (this.cache == null) {
			// cache
			try {
				field = Picasso.class.getDeclaredField("cache");
				field.setAccessible(true);
				obj = field.get(oldPicasso);
				if (obj != null && obj instanceof Cache) {
					builder.memoryCache((Cache) obj);
					this.cache = (Cache) obj;
				}
			} catch (Exception e) {
				QLog.e(e);
			}
		} else {
			builder.memoryCache(this.cache);
		}
		// service
		try {
			field = Picasso.class.getDeclaredField("dispatcher");
			field.setAccessible(true);
			Object dispatcher = field.get(oldPicasso);
			if (dispatcher != null) {
				field = dispatcher.getClass().getDeclaredField("service");
				field.setAccessible(true);
				obj = field.get(dispatcher);
				if (obj != null && obj instanceof ExecutorService) {
					builder.executor((ExecutorService) obj);
				}
			}
		} catch (Exception e) {
			QLog.e(e);
		}
		// transformer
		try {
			field = Picasso.class.getDeclaredField("requestTransformer");
			field.setAccessible(true);
			obj = field.get(oldPicasso);
			if (obj != null && obj instanceof Picasso.RequestTransformer) {
				builder.requestTransformer((Picasso.RequestTransformer) obj);
			}
		} catch (Exception e) {
			QLog.e(e);
		}
		// listener
		try {
			field = Picasso.class.getDeclaredField("listener");
			field.setAccessible(true);
			obj = field.get(oldPicasso);
			if (obj != null && obj instanceof Picasso.Listener) {
				builder.listener((Picasso.Listener) obj);
			}
		} catch (Exception e) {
			QLog.e(e);
		}
		picasso = builder.build();
		// picasso.setDebugging(QunarAppConstants.DEBUG);
	}

	public boolean checkHasDiskCache(String url) {
		if (qunarImageDownloader != null) {
			return qunarImageDownloader.checkHasDiskCache(url);
		}
		return false;
	}

	public boolean checkTaskIsRunning(String url) {
		if (qunarImageDownloader != null) {
			return qunarImageDownloader.checkTaskIsRunning(url);
		}
		return false;
	}

	public void loadImageMask(Object imageData, ImageView view,
			int placeHolderId) {
		MaskTransformation mask = MaskTransformation.newMask(context, view);
		loadImage(imageData, view, placeHolderId, mask, mask);
	}

	public void resizeImage(Object imageData, ImageView imageView,
			int widthPix, int heightPix, Drawable placeHolderDrawable,
			Transformation transformation, Callback callback) {
		capacityLoadImage(imageData, imageView, null, widthPix, heightPix,
				placeHolderDrawable, null, mLoadingBitmap3g, transformation,
				ALWAY_DEFAULT, callback);
	}

	public void resizeImage(Object imageData, ImageView imageView,
			int widthPix, int heightPix, Drawable placeHolderDrawable,
			Transformation transformation, boolean alwaysload, Callback callback) {
		capacityLoadImage(imageData, imageView, null, widthPix, heightPix,
				placeHolderDrawable, null, mLoadingBitmap3g, transformation,
				alwaysload, callback);
	}

	public void resizeImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, int widthPix, int heightPix,
			Drawable placeHolderDrawable, Transformation transformation,
			Callback callback) {
		capacityLoadImage(imageData, imageView, qOnClickListener, widthPix,
				heightPix, placeHolderDrawable, null, mLoadingBitmap3g,
				transformation, ALWAY_DEFAULT, callback);
	}

	public void resizeImage(Object imageData, int widthPix, int heightPix,
			Drawable placeHolderDrawable, Transformation transformation,
			Target target) {
		capacityLoadImage(imageData, null, widthPix, heightPix,
				placeHolderDrawable, null, mLoadingBitmap3g, transformation,
				target, ALWAY_DEFAULT);
	}

	public void resizeImage(Object imageData, ImageView imageView,
			int widthPix, int heightPix, int placeHolderId) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, imageView, null, widthPix, heightPix,
				placeDrawable, null, mLoadingBitmap3g, null, ALWAY_DEFAULT,
				null);
	}

	public void resizeImage(Object imageData, ImageView imageView,
			int widthPix, int heightPix, int placeHolderId, boolean alwayload) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, imageView, null, widthPix, heightPix,
				placeDrawable, null, mLoadingBitmap3g, null, alwayload, null);
	}

	public void resizeImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, int widthPix, int heightPix,
			int placeHolderId) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, imageView, qOnClickListener, widthPix,
				heightPix, placeDrawable, null, mLoadingBitmap3g, null,
				ALWAY_DEFAULT, null);
	}

	public void loadImage(Object imageData, ImageView imageView) {
		loadImage(imageData, imageView, null);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener) {
		loadImage(imageData, imageView, qOnClickListener, ALWAY_DEFAULT);
	}

	public void loadImage(Object imageData, int width, int height,
			ImageView imageView) {
		loadImage(imageData, width, height, imageView, null);
	}

	public void loadImage(Object imageData, int width, int height,
			ImageView imageView, OnClickListener qOnClickListener) {
		loadImage(imageData, imageView, qOnClickListener, width, height, 0, 0,
				ALWAY_DEFAULT, null);
	}

	public void loadImage(Object imageData, ImageView imageView,
			boolean alwayload) {
		loadImage(imageData, imageView, alwayload, null);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, boolean alwayload) {
		loadImage(imageData, imageView, qOnClickListener, alwayload, null);
	}

	public void loadImage(Object imageData, ImageView imageView,
			boolean alwayload, Callback callback) {
		loadImage(imageData, imageView, 0, 0, 0, 0, alwayload, callback);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, boolean alwayload,
			Callback callback) {
		loadImage(imageData, imageView, qOnClickListener, 0, 0, 0, 0,
				alwayload, callback);
	}

	public void loadImage(Object imageData, ImageView imageView, Target target,
			boolean alwayload) {
		capacityLoadImage(imageData, imageView, null, 0, 0, null, null, null,
				null, target, alwayload);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, Target target, boolean alwayload) {
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0, null,
				null, null, null, target, alwayload);
	}

	public void loadImage(Object imageData, ImageView imageView, int widthPix,
			int heightPix, int placeHolderId, Target target, boolean alwayload) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, imageView, null, widthPix, heightPix,
				placeDrawable, null, null, null, target, alwayload);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, int widthPix, int heightPix,
			int placeHolderId, Target target, boolean alwayload) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, imageView, qOnClickListener, widthPix,
				heightPix, placeDrawable, null, null, null, target, alwayload);
	}

	public void loadImage(Object imageData, ImageView imageView,
			int placeHolderId) {
		loadImage(imageData, imageView, placeHolderId, ALWAY_DEFAULT);
	}

	public void loadImage(Object imageData, ImageView imageView,
			int placeHolderId, boolean alwayload) {
		loadImage(imageData, imageView, 0, 0, placeHolderId, 0, alwayload, null);
	}

	public void loadImage(Object imageData, ImageView imageView,
			int placeHolderId, int errHolderId) {
		loadImage(imageData, imageView, 0, 0, placeHolderId, errHolderId,
				ALWAY_DEFAULT, null);
	}

	public void loadImage(Object imageData, ImageView imageView,
			int placeHolderId, int errHolderId, boolean alwayload) {
		loadImage(imageData, imageView, 0, 0, placeHolderId, errHolderId,
				alwayload);
	}

	public void loadImage(Object imageData, ImageView imageView, int widthPix,
			int heightPix, int placeHolderId, int errResourceId) {
		loadImage(imageData, imageView, widthPix, heightPix, placeHolderId,
				errResourceId, ALWAY_DEFAULT);
	}

	public void loadImage(Object imageData, ImageView imageView, int widthPix,
			int heightPix, int placeHolderId, int errResourceId,
			boolean alwayload) {
		loadImage(imageData, imageView, widthPix, heightPix, placeHolderId,
				errResourceId, alwayload, null);
	}

	public void loadImage(Object imageData, ImageView imageView, int widthPix,
			int heightPix, int placeHolderId, int errHolderId,
			boolean alwayload, Callback callback) {
		loadImage(imageData, imageView, null, widthPix, heightPix,
				placeHolderId, errHolderId, alwayload, callback);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, int widthPix, int heightPix,
			int placeHolderId, int errHolderId, boolean alwayload,
			Callback callback) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		Drawable errDrawable = null;
		if (errHolderId > 0) {
			errDrawable = context.getResources().getDrawable(errHolderId);
		}
		capacityLoadImage(imageData, imageView, qOnClickListener, widthPix,
				heightPix, placeDrawable, errDrawable, mLoadingBitmap3g, null,
				alwayload, callback);
	}

	public void loadImage(Object imageData, ImageView imageView,
			Drawable placeHolderDrawable, Transformation transformation) {
		loadImage(imageData, imageView, null, placeHolderDrawable,
				transformation);
	}

	public void loadImage(Object imageData, ImageView imageView,
			Drawable placeHolderDrawable, boolean alwayload,
			Transformation transformation) {
		loadImage(imageData, imageView, null, placeHolderDrawable, alwayload,
				transformation);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, Drawable placeHolderDrawable,
			Transformation transformation) {
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0,
				placeHolderDrawable, null, mLoadingBitmap3g, transformation,
				ALWAY_DEFAULT, null);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, Drawable placeHolderDrawable,
			boolean alwayload, Transformation transformation) {
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0,
				placeHolderDrawable, null, mLoadingBitmap3g, transformation,
				alwayload, null);
	}

	/**
	 * addby: tao.sha 直接返回Bitmap，不设置ImageView
	 *
	 * @param imageData
	 * @param target
	 *            回调对象，（必须自己增加引用，picaasa只有weak引用
	 */
	public void loadImage(Object imageData, Target target, int placeHolderId) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, null, null, 0, 0, placeDrawable, null,
				mLoadingBitmap3g, null, target, ALWAY_DEFAULT);
	}

	public void loadImage(Object imageData, Target target, int placeHolderId,
			boolean alwayLoad) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, null, null, 0, 0, placeDrawable, null,
				mLoadingBitmap3g, null, target, alwayLoad);
	}

	public void loadImage(Object imageData, ImageView imageView,
			Callback callback, int placeHolderId) {
		loadImage(imageData, imageView, null, callback, placeHolderId);
	}

	public void loadImage(Object imageData, ImageView imageView,
			Callback callback, int placeHolderId, boolean alwayLoad) {
		loadImage(imageData, imageView, null, callback, placeHolderId,
				alwayLoad);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, Callback callback,
			int placeHolderId) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0,
				placeDrawable, null, mLoadingBitmap3g, null, ALWAY_DEFAULT,
				callback);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, Callback callback,
			int placeHolderId, boolean alwayLoad) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0,
				placeDrawable, null, mLoadingBitmap3g, null, alwayLoad,
				callback);
	}

	public void loadImage(Object imageData, ImageView imageView,
			int placeHolderId, Transformation transformation, Callback callback) {
		loadImage(imageData, imageView, null, placeHolderId, transformation,
				callback);
	}

	public void loadImage(Object imageData, ImageView imageView,
			int placeHolderId, Transformation transformation,
			Callback callback, boolean alwayLoad) {
		loadImage(imageData, imageView, null, placeHolderId, transformation,
				callback, alwayLoad);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, int placeHolderId,
			Transformation transformation, Callback callback) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0,
				placeDrawable, null, mLoadingBitmap3g, transformation,
				ALWAY_DEFAULT, callback);
	}

	public void loadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, int placeHolderId,
			Transformation transformation, Callback callback, boolean alwayLoad) {
		Drawable placeDrawable = null;
		if (placeHolderId > 0) {
			placeDrawable = context.getResources().getDrawable(placeHolderId);
		}
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0,
				placeDrawable, null, mLoadingBitmap3g, transformation,
				alwayLoad, callback);
	}

	/** 智能加载图片 */
	public void capacityLoadImage(Object imageData, ImageView imageView,
			int placeHolder) {
		capacityLoadImage(imageData, imageView, null, 0, 0, context
				.getResources().getDrawable(placeHolder), null,
				mLoadingBitmap3g, null, ALWAY_DEFAULT, null);
	}

	public void capacityLoadImage(Object imageData, ImageView imageView,
			int placeHolder, boolean alwayLoad) {
		capacityLoadImage(imageData, imageView, null, 0, 0, context
				.getResources().getDrawable(placeHolder), null,
				mLoadingBitmap3g, null, alwayLoad, null);
	}

	public void capacityLoadImage(Object imageData, ImageView imageView,
			Drawable placeHolderDrawable) {
		capacityLoadImage(imageData, imageView, null, 0, 0,
				placeHolderDrawable, null, mLoadingBitmap3g, null,
				ALWAY_DEFAULT, null);
	}

	public void capacityLoadImage(Object imageData, ImageView imageView,
			Drawable placeHolderDrawable, boolean alwayLoad) {
		capacityLoadImage(imageData, imageView, null, 0, 0,
				placeHolderDrawable, null, mLoadingBitmap3g, null, alwayLoad,
				null);
	}

	public void capacityLoadImage(Object imageData, ImageView imageView,
			int placeHolder, Bitmap bitmap3g) {
		capacityLoadImage(imageData, imageView, null, 0, 0, context
				.getResources().getDrawable(placeHolder), null, bitmap3g, null,
				ALWAY_DEFAULT, null);
	}

	public void capacityLoadImage(Object imageData, ImageView imageView,
			int placeHolder, Bitmap bitmap3g, boolean alwayLoad) {
		capacityLoadImage(imageData, imageView, null, 0, 0, context
				.getResources().getDrawable(placeHolder), null, bitmap3g, null,
				alwayLoad, null);
	}

	public void capacityLoadImage(Object imageData, ImageView imageView,
			Drawable placeHolderDrawable, Bitmap bitmap3g) {
		capacityLoadImage(imageData, imageView, null, 0, 0,
				placeHolderDrawable, null, bitmap3g, null, ALWAY_DEFAULT, null);
	}

	public void capacityLoadImage(Object imageData, ImageView imageView,
			Drawable placeHolderDrawable, Bitmap bitmap3g, boolean alwayLoad) {
		capacityLoadImage(imageData, imageView, null, 0, 0,
				placeHolderDrawable, null, bitmap3g, null, alwayLoad, null);
	}

	public void capacityLoadImage(Object imageData, ImageView imageView,
			Drawable placeHolderDrawable, Transformation transformation,
			Bitmap bitmap3g) {
		capacityLoadImage(imageData, imageView, null, 0, 0,
				placeHolderDrawable, null, bitmap3g, transformation,
				ALWAY_DEFAULT, null);
	}

	public void capacityLoadImage(Object imageData, ImageView imageView,
			Drawable placeHolderDrawable, Transformation transformation,
			Bitmap bitmap3g, boolean alwayLoad) {
		capacityLoadImage(imageData, imageView, null, 0, 0,
				placeHolderDrawable, null, bitmap3g, transformation, alwayLoad,
				null);
	}

	public void capacityLoadImage(Object imageData, ImageView imageView,
			OnClickListener qOnClickListener, int placeHolder, Bitmap bitmap3g) {
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0, context
				.getResources().getDrawable(placeHolder), null, bitmap3g, null,
				ALWAY_DEFAULT, null);
	}

	public void capacityLoadImage(OnClickListener qOnClickListener,
			Object imageData, ImageView imageView,
			Drawable placeHolderDrawable, Bitmap bitmap3g) {
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0,
				placeHolderDrawable, null, bitmap3g, null, ALWAY_DEFAULT, null);
	}

	public void capacityLoadImage(OnClickListener qOnClickListener,
			Object imageData, ImageView imageView,
			Drawable placeHolderDrawable, boolean alwayLoad, Bitmap bitmap3g) {
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0,
				placeHolderDrawable, null, bitmap3g, null, alwayLoad, null);
	}

	public void capacityLoadImage(Object imageData,
			OnClickListener qOnClickListener, ImageView imageView,
			Drawable placeHolderDrawable, Transformation transformation,
			Bitmap bitmap3g) {
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0,
				placeHolderDrawable, null, bitmap3g, transformation,
				ALWAY_DEFAULT, null);
	}

	public void capacityLoadImage(Object imageData,
			OnClickListener qOnClickListener, ImageView imageView,
			Drawable placeHolderDrawable, boolean alwayLoad,
			Transformation transformation, Bitmap bitmap3g) {
		capacityLoadImage(imageData, imageView, qOnClickListener, 0, 0,
				placeHolderDrawable, null, bitmap3g, transformation, alwayLoad,
				null);
	}

	/**
	 * 智能方式加载
	 *
	 * @param imageData
	 *            imageData (resourceID,File,Uri,String)四种类型
	 * @param imageView
	 * @param qOnClickListener
	 *            imageView设置的点击事件回调
	 * @param widthPix
	 * @param heightPix
	 * @param placeHolderDrawable
	 * @param errDrawable
	 * @param bitmap3g
	 * @param transformation
	 *            转化圆角
	 * @param alwayLoad
	 * @param callback
	 */
	public void capacityLoadImage(final Object imageData,
			final ImageView imageView, final OnClickListener qOnClickListener,
			final int widthPix, final int heightPix,
			final Drawable placeHolderDrawable, final Drawable errDrawable,
			Bitmap bitmap3g, final Transformation transformation,
			final boolean alwayLoad, final Callback callback) {
		if (imageView != null) {
			if (placeHolderDrawable != null) {
				imageView.setImageDrawable(placeHolderDrawable);
			}
			if (qOnClickListener != null) {
				imageView.setOnClickListener(qOnClickListener);
			}
		}
		if (imageData == null) {
			if (imageView != null && errDrawable != null) {
				imageView.setImageDrawable(errDrawable);
			}
			if (callback != null) {
				callback.onError();
			}
			return;
		}
		Bitmap bitmap = null;
		String keyForDiskCache = null;
		RequestCreator requestCreator = null;
		if (imageData instanceof String) {
			keyForDiskCache = (String) imageData;
			if (TextUtils.isEmpty(keyForDiskCache)) {
				keyForDiskCache = null;
			}
			requestCreator = picasso.load(keyForDiskCache);
		} else if (imageData instanceof File) {
			File file = (File) imageData;
			keyForDiskCache = Uri.fromFile(file).toString();
			requestCreator = picasso.load(file);
		} else if (imageData instanceof Uri) {
			Uri uri = (Uri) imageData;
			keyForDiskCache = uri.toString();
			requestCreator = picasso.load(uri);
		} else if (imageData instanceof Integer) {
			int resourceId = (Integer) imageData;
			keyForDiskCache = resourceId + "";
			requestCreator = picasso.load(resourceId);
			;
		}
		if (requestCreator == null) {
			if (callback != null) {
				callback.onError();
			}
			return;
		}
		if (widthPix > 0 && heightPix > 0) {
			requestCreator.resize(widthPix, heightPix);
		}
		if (placeHolderDrawable != null) {
			requestCreator.placeholder(placeHolderDrawable);
		}
		if (errDrawable != null) {
			requestCreator.error(errDrawable);
		}
		if (transformation != null) {
			requestCreator.transform(transformation);
		}
		// 此处无须判断内存缓存列表中是否存在目的加载地址，picasso自己封装了这部分的逻辑，我们只需要检查硬盘缓存列表是否存在即可
		// check exist in disk cache
		boolean existInDiskCache = checkHasDiskCache(keyForDiskCache);
		if (!NetConnChangeReceiver.netGetted) {
			NetConnChangeReceiver.dealNetworkInfo(context);
		}
		boolean isAutoLoad = NetConnChangeReceiver.wifi
				  || alwayLoad;
		if (existInDiskCache || isAutoLoad || imageData instanceof Integer) {
			requestCreator.into(imageView, callback);
		} else {
			boolean isLoading = false;
			if (!TextUtils.isEmpty(keyForDiskCache)) {
				isLoading = checkTaskIsRunning(keyForDiskCache);
			}
			if (!isLoading) {
				if (imageView != null) {
					if (bitmap3g != null) {
						imageView.setImageBitmap(bitmap3g);
					}
					// 智能加载
					final RequestCreator finalRequestCreator = requestCreator;
					imageView.setOnClickListener(
							new OnClickListener() {
								public void onClick(View v) {
									imageView
											.setOnClickListener(qOnClickListener);
									if (null == qOnClickListener) {
										imageView.setClickable(false);
									}
									finalRequestCreator.into(imageView,
											callback);
								}
							}) ;
				}
			}
		}
	}

	public void capacityLoadImage(final Object imageData,
			final ImageView imageView, final int widthPix, final int heightPix,
			final Drawable placeHolderDrawable, final Drawable errDrawable,
			Bitmap bitmap3g, final Transformation transformation,
			final boolean alwayLoad, final Callback callback) {
		capacityLoadImage(imageData, imageView, null, widthPix, heightPix,
				placeHolderDrawable, errDrawable, bitmap3g, transformation,
				alwayLoad, callback);
	}

	public void capacityLoadImage(final Object imageData,
			final ImageView imageView, final int widthPix, final int heightPix,
			final Drawable placeHolderDrawable, final Drawable errDrawable,
			Bitmap bitmap3g, final Transformation transformation,
			final Target target, final boolean alwayLoad) {
		capacityLoadImage(imageData, imageView, null, widthPix, heightPix,
				placeHolderDrawable, errDrawable, bitmap3g, transformation,
				target, alwayLoad);
	}

	public void capacityLoadImage(final Object imageData,
			final ImageView imageView, final OnClickListener qOnClickListener,
			final int widthPix, final int heightPix,
			final Drawable placeHolderDrawable, final Drawable errDrawable,
			Bitmap bitmap3g, final Transformation transformation,
			final Target target, final boolean alwayLoad) {
		if (imageView != null) {
			if (placeHolderDrawable != null) {
				imageView.setImageDrawable(placeHolderDrawable);
			}
			if (qOnClickListener != null) {
				imageView.setOnClickListener(qOnClickListener);
			}
		}
		if (imageData == null) {
			if (target != null) {
				target.onPrepareLoad(placeHolderDrawable);
			}
			if (imageView != null && errDrawable != null) {
				imageView.setImageDrawable(errDrawable);
			}
			if (target != null) {
				target.onBitmapFailed(null);
			}
			return;
		}
		Bitmap bitmap = null;
		String keyForDiskCache = null;
		RequestCreator requestCreator = null;
		if (imageData instanceof String) {
			keyForDiskCache = (String) imageData;
			if (TextUtils.isEmpty(keyForDiskCache)) {
				keyForDiskCache = null;
			}
			requestCreator = picasso.load(keyForDiskCache);
		} else if (imageData instanceof File) {
			File file = (File) imageData;
			keyForDiskCache = Uri.fromFile(file).toString();
			requestCreator = picasso.load(file);
		} else if (imageData instanceof Uri) {
			Uri uri = (Uri) imageData;
			keyForDiskCache = uri.toString();
			requestCreator = picasso.load(uri);
		} else if (imageData instanceof Integer) {
			int resourceId = (Integer) imageData;
			keyForDiskCache = resourceId + "";
			requestCreator = picasso.load(resourceId);
		}
		if (requestCreator == null) {
			if (target != null) {
				target.onPrepareLoad(placeHolderDrawable);
			}
			if (target != null) {
				target.onBitmapFailed(null);
			}
			return;
		}
		if (widthPix > 0 && heightPix > 0) {
			requestCreator.resize(widthPix, heightPix);
		}
		if (placeHolderDrawable != null) {
			requestCreator.placeholder(placeHolderDrawable);
		}
		if (errDrawable != null) {
			requestCreator.error(errDrawable);
		}
		if (transformation != null) {
			requestCreator.transform(transformation);
		}
		// 此处无须判断内存缓存列表中是否存在目的加载地址，picasso自己封装了这部分的逻辑，我们只需要检查硬盘缓存列表是否存在即可
		// check exist in disk cache
		boolean existInDiskCache = checkHasDiskCache(keyForDiskCache);
		if (!NetConnChangeReceiver.netGetted) {
			NetConnChangeReceiver.dealNetworkInfo(context);
		}
		boolean isAutoLoad = NetConnChangeReceiver.wifi
				 || alwayLoad;
		if (existInDiskCache || isAutoLoad || imageData instanceof Integer) {
			if (target != null) {
				requestCreator.into(target);
			}
		} else {
			boolean isLoading = false;
			if (!TextUtils.isEmpty(keyForDiskCache)) {
				isLoading = checkTaskIsRunning(keyForDiskCache);
			}
			if (!isLoading) {
				if (imageView != null) {
					if (bitmap3g != null) {
						imageView.setImageBitmap(bitmap3g);
					}
					// 智能加载
					final RequestCreator finalRequestCreator = requestCreator;
					imageView.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							imageView.setOnClickListener(qOnClickListener);
							if (null == qOnClickListener) {
								imageView.setClickable(false);
							}
							if (target != null) {
								finalRequestCreator.into(target);
							}
						}
					});
				}
			}
		}
	}

	public RequestCreator buildRequestCreatorWithPlaceholder(int resourceId) {
		return buildRequestCreator(0, 0, resourceId);
	}

	public RequestCreator buildRequestCreator(int widthPix, int heightPix,
			int resourceId) {
		RequestCreator requestCreator = picasso.load(resourceId);
		if (widthPix > 0 && heightPix > 0) {
			requestCreator.resize(widthPix, heightPix);
		}
		return requestCreator;
	}

	public RequestCreator buildRequestCreator(int widthPix, int heightPix,
			Object imageData) {
		return buildRequestCreator(widthPix, heightPix, imageData, 0);
	}

	public RequestCreator buildRequestCreator(int widthPix, int heightPix,
			Object imageData, int placeHolderId) {
		RequestCreator requestCreator = null;
		if (imageData == null) {
			return null;
		} else if (imageData instanceof String) {
			String url = (String) imageData;
			if (TextUtils.isEmpty(url)) {
				url = null;
			}
			requestCreator = picasso.load(url);
		} else if (imageData instanceof File) {
			File file = (File) imageData;
			requestCreator = picasso.load(file);
		} else if (imageData instanceof Uri) {
			Uri uri = (Uri) imageData;
			requestCreator = picasso.load(uri);
		} else if (imageData instanceof Integer) {
			int resourceId = (Integer) imageData;
			requestCreator = picasso.load(resourceId);
		} else {
			return null;
		}
		if (widthPix > 0 && heightPix > 0) {
			requestCreator.resize(widthPix, heightPix);
		}
		if (placeHolderId > 0) {
			requestCreator.placeholder(placeHolderId);
		}
		return requestCreator;
	}

	/**
	 * 清空Iamgeloader的内存缓存
	 */
	public void clearMemoryCache() {
		if (this.cache != null) {
			this.cache.clear();
		} else {
			try {
				Field field = Picasso.class.getDeclaredField("cache");
				field.setAccessible(true);
				Object obj = field.get(picasso);
				if (obj != null && obj instanceof Cache) {
					Cache cache = (Cache) obj;
					cache.clear();
					this.cache = cache;
				}
			} catch (Exception e) {
				QLog.e(e);
			}
		}
	}

	/**
	 * 清空指定URL的内存缓存
	 */
	public void clearURLCache(String url) {
		if (this.cache != null) {
			this.cache.set(url, null);
		} else {
			try {
				Field field = Picasso.class.getDeclaredField("cache");
				field.setAccessible(true);
				Object obj = field.get(picasso);
				if (obj != null && obj instanceof Cache) {
					Cache cache = (Cache) obj;
					Bitmap bitmap = cache.get(url);
					if (bitmap != null) {
						cache.set(url, null);
					}
					this.cache = cache;
				}
			} catch (Exception e) {
				QLog.e(e);
			}
		}
	}

	/***
	 * 是否达到主动释放缓存列表的条件
	 *
	 * @return
	 */
	public boolean canReleaseCache() {
		if (this.cache != null) {
			return ((float) this.cache.size()) / ((float) this.cache.maxSize()) >= PER_CAN_RELEASE;
		} else {
			try {
				Field field = Picasso.class.getDeclaredField("cache");
				field.setAccessible(true);
				Object obj = field.get(picasso);
				if (obj != null && obj instanceof Cache) {
					Cache cache = (Cache) obj;
					this.cache = cache;
					return ((float) cache.size()) / ((float) cache.maxSize()) >= PER_CAN_RELEASE;
				}
			} catch (Exception e) {
				QLog.e(e);
			}
			return false;
		}
	}

	/***
	 * 根据imageview取消加载任务
	 * 
	 * @param imageView
	 */
	public void cancelRequest(ImageView imageView) {
		picasso.cancelRequest(imageView);
		if (qunarImageDownloader != null) {
			qunarImageDownloader.onCancel();
		}
	}

	/**
	 * 根据target取消加载任务
	 * 
	 * @param target
	 */
	public void cancelRequest(Target target) {
		picasso.cancelRequest(target);
		if (qunarImageDownloader != null) {
			qunarImageDownloader.onCancel();
		}
	}

	/***
	 * 千万要注意：使用该方法较危险，在setPauseWork（true）后，要记得setPauseWork（false）
	 * 否则会发生可怕的后果（后续图片加载功就没法继续工作） 原因：在于Imageloader此处是一个单例。
	 * 
	 * @param pauseWork
	 *            是否暂停下载任务
	 */
	public void setPauseWork(boolean pauseWork) {
		if (qunarImageDownloader != null) {
			qunarImageDownloader.setPauseWork(pauseWork);
		}
	}
}
