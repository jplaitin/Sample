package com.example.example.views;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class RotatingImageView extends ImageView implements IRotatable {
	private final float scale = getResources().getDisplayMetrics().density; 
	private Bitmap bitmap;
	private boolean isBordersOn = false;

	private static final int ANIMATION_SPEED = 270; // 270 deg/sec

	private int mCurrentDegree = 0; // [0, 359]
	private int mStartDegree = 0;
	private int mTargetDegree = 0;

	private boolean mClockwise = false;

	private long mAnimationStartTime = 0;
	private long mAnimationEndTime = 0;
	private Paint borderPaint;

	public RotatingImageView(Context context) {
		super(context);
		initView(context);
	}

	public RotatingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public RotatingImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private void initView(Context context) {
		bitmap = null;
		
		borderPaint = new Paint();
		borderPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setColor(Color.WHITE);
		borderPaint.setStrokeWidth((2.0f * scale));
	}
	
	/**
	 * If true draws borders around image
	 * @param isBorder
	 */
	public void useImageBorders(boolean isBorder) {
		isBordersOn = isBorder;
	}

	@Override
	public void setOrientation(int degree) {
		//l.i("setOrientation(" + degree + ") currentDegree: " + mCurrentDegree);
		degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
		if (degree == mTargetDegree)
			return;

		mTargetDegree = degree;
		mStartDegree = mCurrentDegree;
		mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();

		int diff = mTargetDegree - mCurrentDegree;
		diff = diff >= 0 ? diff : 360 + diff; // make it in range [0, 359]

		// Make it in range [-179, 180]. That's the shorted distance between the
		// two angles
		diff = diff > 180 ? diff - 360 : diff;

		mClockwise = diff >= 0;
		mAnimationEndTime = mAnimationStartTime + Math.abs(diff) * 1000 / ANIMATION_SPEED;

		invalidate();

	}

	public void setImageBitmap(String absPath) throws IOException {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferQualityOverSpeed = false;
		opts.inPreferredConfig = Config.ARGB_8888;

		Bitmap temp = BitmapFactory.decodeFile(absPath, opts);
		setImage(temp);
	}
	
	public void setImageBitmap(int resourceID) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferQualityOverSpeed = false;
		opts.inPreferredConfig = Config.ARGB_8888;
		Bitmap temp = BitmapFactory.decodeResource(getResources(), resourceID, opts);
		setImage(temp);
	}

	public void setImage(Bitmap bitmap) {
		if (bitmap == null) {
			this.bitmap = null;
			setImageDrawable(null);
			return;
		}
		if (this.bitmap != null) { //setting a new bitmap (one already set)
//			l.i("setting image Drawable null");
			this.bitmap.recycle();
			this.bitmap = null;
		}
		
		LayoutParams param = getLayoutParams();
		setRoundBgBadding(bitmap, param.height);
		final int miniThumbWidth = param.width - getPaddingLeft() - getPaddingRight();
		final int miniThumbHeight = param.height - getPaddingTop() - getPaddingBottom();
		this.bitmap = ThumbnailUtils.extractThumbnail(bitmap, miniThumbWidth, miniThumbHeight);
		bitmap.recycle();

		setImageDrawable(new BitmapDrawable(getContext().getResources(), this.bitmap));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable();
		if (drawable == null)
			return;
		//l.i("onDraw with bitmap");

		Rect bounds = drawable.getBounds();
		int w = bounds.right - bounds.left;
		int h = bounds.bottom - bounds.top;
		if (w == 0 || h == 0)
			return; // nothing to draw

		if (mCurrentDegree != mTargetDegree) {
			long time = AnimationUtils.currentAnimationTimeMillis();
			if (time < mAnimationEndTime) {
				int deltaTime = (int) (time - mAnimationStartTime);
				int degree = mStartDegree + ANIMATION_SPEED * (mClockwise ? deltaTime : -deltaTime) / 1000;
				degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
				mCurrentDegree = degree;
				invalidate();
			} else {
				mCurrentDegree = mTargetDegree;
			}
		}

		int left = getPaddingLeft();
		int top = getPaddingTop();
		int right = getPaddingRight();
		int bottom = getPaddingBottom();
		int width = getWidth() - left - right;
		int height = getHeight() - top - bottom;

		int saveCount = canvas.getSaveCount();
		// Scale down the image first if required.
		if ((getScaleType() == ImageView.ScaleType.FIT_CENTER) && ((width < w) || (height < h))) {
			float ratio = Math.min((float) width / w, (float) height / h);
			canvas.scale(ratio, ratio, width / 2.0f, height / 2.0f);
		}
		canvas.translate(left + width / 2, top + height / 2);
		canvas.rotate(-mCurrentDegree);
		canvas.translate(-w / 2, -h / 2);
		drawable.draw(canvas);
		if(isBordersOn) {
			canvas.drawRect(drawable.getBounds(), borderPaint);
		}
		canvas.restoreToCount(saveCount);
	}

	public void setRoundBgBadding(Bitmap bm, int viewHeight) {
		int paddingTopBottom = (int)(viewHeight*0.06 * scale);
		int paddingLeftRight = paddingTopBottom;
		viewHeight -= paddingTopBottom;
		double ratio = (double)bm.getWidth() / bm.getHeight();
		double tempWidth = ((viewHeight * viewHeight) / (1.0 + (1.0 / (ratio * ratio))));
		double tempHeight = ((viewHeight * viewHeight) / (1.0 + (ratio * ratio)));
		int newWidth = (int) Math.sqrt(tempWidth);
		int newHeight = (int) Math.sqrt(tempHeight);
		paddingTopBottom += (viewHeight - newHeight) / 2;
		paddingLeftRight += (viewHeight - newWidth) / 2;
//		l.i("bm.getWidth() "+ bm.getWidth() +" bm.getHeight() "+ bm.getHeight());
//		l.i("newWidth: "+ newWidth +" newHeight: "+ newHeight);
//		l.i("ratio: "+ ratio +" tempWidth: "+ tempWidth +" tempHeight: "+ tempHeight);
//		l.i("paddingTopBottom: " + paddingTopBottom + " paddingLeftRight: " + paddingLeftRight);
		setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);
	}
}
