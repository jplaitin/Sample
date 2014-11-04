package com.example.example.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haave.docscanner.R;

public class ViewDocScannerToggleButton extends LinearLayout {

	private TextView toggleLeftText = null;
	private TextView toggleRightText = null;
	
	private boolean rightSelected = true;
	
	public ViewDocScannerToggleButton(Context context) {
		super(context);
		sharedConstructor(context);
	}

	public ViewDocScannerToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		sharedConstructor(context);
	}

	public ViewDocScannerToggleButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		sharedConstructor(context);
	}

	private void sharedConstructor(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_toggle_btn, this);
		
		toggleLeftText = (TextView) findViewById(R.id.toggle_left);
		toggleRightText = (TextView) findViewById(R.id.toggle_right);
	}
	
	public void setToggleClickListener(OnClickListener listener) {
		setOnClickListener(listener);
	}
	
	public void toggleView() {
		rightSelected = !rightSelected;
		if(rightSelected) {
			toggleLeftText.setBackgroundResource(R.drawable.bg_toggle_left);
			toggleLeftText.setTextColor(getContext().getResources().getColor(R.color.grey_light));
			toggleRightText.setBackgroundResource(R.drawable.bg_toggle_right_selected);
			toggleRightText.setTextColor(getContext().getResources().getColor(R.color.pure_white));
			
		}
		else {
			toggleLeftText.setBackgroundResource(R.drawable.bg_toggle_left_selected);
			toggleLeftText.setTextColor(getContext().getResources().getColor(R.color.pure_white));
			toggleRightText.setBackgroundResource(R.drawable.bg_toggle_right);
			toggleRightText.setTextColor(getContext().getResources().getColor(R.color.grey_light));
		}
		toggleLeftText.invalidate();
		toggleRightText.invalidate();
	}
	
	public void setTexts(String left, String right) {
		toggleLeftText.setText(left);
		toggleRightText.setText(right);
	}
	
	public boolean isRightSelected() {
		return rightSelected;
	}
}
