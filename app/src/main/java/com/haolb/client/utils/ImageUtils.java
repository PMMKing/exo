package com.haolb.client.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.view.Display;
import android.widget.TextView;


import com.haolb.client.app.MainApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;

public final class ImageUtils {

	private static final Context sContext = MainApplication.getInstance();

	/**
	 * 创建图片的缩略图
	 * 
	 * @param resId
	 *            资源文件id
	 * @param sampleSize
	 *            缩略图的尺寸（单位为px）
	 * @return 创建缩略图的Bitmap
	 */
	public static Bitmap getThumbnail(int resId, float sampleSize) {
		if (resId == 0) {
			return null;
		}
		Bitmap bitmap = null;
		try {
			// 获取这个图片的宽和高
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			bitmap = BitmapFactory.decodeResource(sContext.getResources(),
					resId, options);
			// 此时返回bm为空
			// 计算缩放比
			int max = Math.max(options.outHeight, options.outWidth);
			int be = (int) (max / sampleSize);
			if (be <= 0) {
				be = 1;
			}
			options.inSampleSize = be;
			// 重新读入图片，这次要把options.inJustDecodeBounds 设为 false
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeResource(sContext.getResources(),
					resId, options);
		} catch (OutOfMemoryError t) {
			System.gc();
			try {
				Method m = Runtime.class.getDeclaredMethod("runFinalization",
						boolean.class);
				m.setAccessible(true);
				m.invoke(Runtime.getRuntime(), true);
			} catch (Exception e) {
			}
		}
		return bitmap;
	}

	/**
	 * 创建图片的缩略图
	 * 
	 * @param imageUri
	 * @param sampleSize
	 *            缩略图的尺寸（单位为px）
	 * @return 创建缩略图的Bitmap
	 */
	public static Bitmap getThumbnail(Uri imageUri, float sampleSize) {
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 获取这个图片的宽和高
			bitmap = BitmapFactory.decodeStream(sContext.getContentResolver()
					.openInputStream(imageUri), null, options);
			// 此时返回bm为空
			// 计算缩放比
			int max = Math.max(options.outHeight, options.outWidth);

			int be = (int) (max / sampleSize);
			if (be <= 0) {
				be = 1;
			}
			options.inSampleSize = be;
			// 重新读入图片，这次要把options.inJustDecodeBounds 设为 false
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeStream(sContext.getContentResolver()
					.openInputStream(imageUri), null, options);
			return bitmap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (OutOfMemoryError t) {
			System.gc();
			try {
				Method m = Runtime.class.getDeclaredMethod("runFinalization",
						boolean.class);
				m.setAccessible(true);
				m.invoke(Runtime.getRuntime(), true);
			} catch (Exception e) {
			}
		}
		return bitmap;
	}

	/**
	 * 创建图片的缩略图
	 * 
	 * @param imageFile
	 * @param sampleSize
	 *            缩略图的尺寸（单位为px）
	 * @return 创建缩略图的Bitmap
	 */
	public static Bitmap getThumbnail(File imageFile, float sampleSize) {
		// 获取这个图片的宽和高
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),
					options);
			// 此时返回bm为空
			// 计算缩放比
			int max = Math.max(options.outHeight, options.outWidth);

			int be = (int) (max / sampleSize);
			if (be <= 0) {
				be = 1;
			}
			options.inSampleSize = be;
			// 重新读入图片，这次要把options.inJustDecodeBounds 设为 false
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),
					options);
		} catch (OutOfMemoryError t) {
			QLog.e("", t.getLocalizedMessage(), t);
			System.gc();
			try {
				Method m = Runtime.class.getDeclaredMethod("runFinalization",
						boolean.class);
				m.setAccessible(true);
				m.invoke(Runtime.getRuntime(), true);
			} catch (Exception e) {
			}
		}
		return bitmap;
	}

	/**
	 * Change the drawable to btimap.
	 * 
	 * @param drawable
	 *            A drawable object.
	 * @return A Bitmap object.
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 获得当前屏幕的尺寸
	 * 
	 * @param activity
	 * @return
	 */
	public static Display getScreenDisplay(Activity activity) {
		return activity.getWindowManager().getDefaultDisplay();
	}

	/**
	 * 为textview指定一个等高的icon
	 * 
	 * @param textView
	 * @param resId
	 */
	public static void setSignIcon(TextView textView, int resId) {
		Drawable drawable = getEqualHeightDrawable(textView, resId);
		textView.setCompoundDrawables(null, null, drawable, null);
	}

	/**
	 * 为textview指定一个等高的icon,若resId不合法将返回null
	 * 
	 * @param textView
	 * @param resId
	 */
	public static Drawable getEqualHeightDrawable(TextView textView, int resId) {
		Drawable drawable = null;
		if (resId > 0) {
			try {
				drawable = textView.getContext().getResources()
						.getDrawable(resId);
				drawable.setBounds(0, 0, (int) textView.getPaint()
						.getTextSize(), (int) textView.getPaint().getTextSize());
			} catch (Exception e) {
				// do nothing
			}
		}
		return drawable;
	}

	/**
	 * 获取图片的宽和高
	 * 
	 * @param path
	 * @return options.outWidth单位px
	 */
	public static BitmapFactory.Options getImageSize(String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		return options;
	}

	/**
	 * 获取图片的宽和高
	 * 
	 * @param resources
	 * @param resId
	 * @return
	 * @author zexu.ge
	 * @since 2013年12月25日下午5:16:08
	 */
	public static BitmapFactory.Options getImageSize(Resources resources,
			int resId) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resId, options);
		return options;
	}

	/**
	 * 旋转bitmap
	 * 
	 * @param b
	 * @param degrees
	 * @return
	 */
	public static Bitmap rotate(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) b.getWidth() / 2,
					(float) b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
			}
		}
		return b;
	}

	/**
	 * 代码代替xml生成文字状态变色节省资源
	 * 
	 * @param normal
	 * @param pressed
	 * @author ran.feng
	 * @since 2012年9月2日下午3:50:30
	 */
	public static ColorStateList createTextSelector(int normal, int pressed) {
		final ColorStateList colorStateList = new ColorStateList(new int[][] {
				new int[] { android.R.attr.state_pressed,
						android.R.attr.state_enabled },
				new int[] { android.R.attr.state_selected,
						android.R.attr.state_enabled },
				new int[] { android.R.attr.state_enabled } }, new int[] {
				pressed, pressed, normal });
		return colorStateList;
	}

	/**
	 * 代码代替xml生成Drawable状态变色节省资源
	 * 
	 * @param normal
	 * @param pressed
	 * @author ran.feng
	 * @since 2012年9月2日下午3:56:34
	 */
	public static StateListDrawable createBGSelector(int normal, int pressed) {
		final StateListDrawable drawable = new StateListDrawable();
		ColorDrawable cdNormal = new ColorDrawable(normal);
		ColorDrawable cdPressed = new ColorDrawable(pressed);
		drawable.addState(new int[] { android.R.attr.state_pressed,
				android.R.attr.state_enabled }, cdPressed);
		drawable.addState(new int[] { android.R.attr.state_selected,
				android.R.attr.state_enabled }, cdPressed);
		drawable.addState(new int[] { android.R.attr.state_checked,
				android.R.attr.state_enabled }, cdPressed);
		drawable.addState(new int[] { android.R.attr.state_enabled }, cdNormal);
		return drawable;
	}


}
