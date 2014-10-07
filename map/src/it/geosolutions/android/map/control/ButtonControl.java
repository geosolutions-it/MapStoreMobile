package it.geosolutions.android.map.control;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.view.AdvancedMapView;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ButtonControl extends MapControl{
	

	
	private Drawable mDrawable;
	private int mSize;
	private int mOffset;
	private int y;
	
	private Paint bgPaint;
	
	private Rect mRect;
	

	public ButtonControl(AdvancedMapView view) {
		super(view);
		init(view);

	}
	public ButtonControl(AdvancedMapView m,boolean enabled){
		super(m,enabled);
		init(view);


	}
	public void init(AdvancedMapView view){

		final Resources res = view.getContext().getResources();
		
		mDrawable =  res.getDrawable(R.drawable.ic_device_access_location_searching);
		float dpiFactor = res.getDisplayMetrics().density;
		mSize = (int) (40 * dpiFactor);
		mOffset = (int) (10 * dpiFactor);
		
		y = view.getHeight() - mSize - mOffset;
		
		bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgPaint.setColor(0xffBBBBBB);
		bgPaint.setStyle(Style.FILL);
		
		mRect = new Rect(mOffset,y, mOffset+ mSize, y + mSize);
		
	}
	
	@Override
	public void draw(Canvas canvas) {
		
		canvas.drawRect(mRect, bgPaint);
		
		mDrawable.setBounds(mRect);
		
		mDrawable.draw(canvas);
		
		
	}
	

	@Override
	public void refreshControl(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

}
