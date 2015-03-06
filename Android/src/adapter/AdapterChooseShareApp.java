package com.example.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.haave.docscanner.R;

public class AdapterChooseShareApp extends ArrayAdapter<ChooseApplicationItem> {
//	private DebugLog l = new DebugLog(AdapterChooseShareApp.class.getSimpleName());
	private final LayoutInflater inflater;
	private final ChooseApplicationItem[] data; 
	
	public AdapterChooseShareApp(Context context, ChooseApplicationItem[] objects) {
		super(context, R.layout.view_application_row, objects);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		data = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = inflater.inflate(R.layout.view_application_row, parent, false);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.row_application_icon);
		TextView textView 	= (TextView) rowView.findViewById(R.id.row_application_label);
		
		textView.setText(data[position].appName);
		imageView.setImageDrawable(data[position].icon);
		
		return rowView;
	}

}

