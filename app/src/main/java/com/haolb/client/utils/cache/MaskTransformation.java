package com.haolb.client.utils.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.ImageView;

import com.haolb.client.utils.QLog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Transformation;


/**
 * Created by chaos on 14-5-17.
 */
public class MaskTransformation implements Transformation,Callback{

	private static final String TAG = MaskTransformation.class.getSimpleName();

	private Context mContext;
	private ImageView mImg = null;

	private Drawable mDrawable = null;

	public static MaskTransformation newMask(Context context,ImageView view){
		return new MaskTransformation(context,view);
	}

	public MaskTransformation(Context context,ImageView iv) {
		this.mContext = context;
		this.mImg = iv;
	}
	public static StateListDrawable makeColorMask(Context context, Bitmap bi) {
		return makeColorMask(context, bi, 0x09000000);
	}
	public static StateListDrawable makeColorMask(Context context, Bitmap bi, int argb) {
		// init
		Bitmap unstate = bi.copy(Config.ARGB_8888, false);
		Bitmap pressed = bi.copy(Config.ARGB_8888, true);
		Canvas canvas = new Canvas(pressed);
		canvas.drawColor(argb);

		StateListDrawable stateDrawable = new StateListDrawable();
		int statePressed = android.R.attr.state_pressed;
		stateDrawable.addState(new int[]{-statePressed}, new BitmapDrawable(unstate));
		stateDrawable.addState(new int[]{statePressed}, new BitmapDrawable(pressed));
		return stateDrawable;
	}
	public Bitmap transform(Bitmap bitmap) {
		log("transform");
		mDrawable = makeColorMask(mContext,Bitmap.createBitmap(bitmap));
		return bitmap;
	}

	public String key() {
		log("key");
		return MaskTransformation.class.getSimpleName() + hashCode();
	}

	public void onSuccess(){
		log("onSuccess");
		if(mImg != null && mDrawable != null){
			log("onSuccess --> setting ");
			mImg.setImageBitmap(null);
			mImg.setImageDrawable(mDrawable);
		}
	}

	public void onError(){
		log("onError");
		if(mImg != null && mDrawable != null){
			log("onSuccess --> setting ");
			mImg.setImageBitmap(null);
			mImg.setImageDrawable(mDrawable);
		}
	}

	public static void log(String msg){
		QLog.d(TAG, msg);
	}
}
