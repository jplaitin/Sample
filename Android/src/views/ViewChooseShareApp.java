package com.example.example.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haave.docscanner.R;
import com.haave.docscanner.view.adapters.AdapterChooseShareApp;
import com.haave.docscanner.view.adapters.ChooseApplicationItem;
import com.haave.docscanner.view.adapters.ChooseApplicationSelectionListener;

public class ViewChooseShareApp extends RelativeLayout {
private ListView appListView = null;
	private ChooseApplicationItem[] listData = null;
	private ChooseApplicationSelectionListener chooseListener = null;
	private TextView title;
	
	public ViewChooseShareApp(Context context) {
		super(context);
		sharedConstructor(context);
	}

	public ViewChooseShareApp(Context context, AttributeSet attrs) {
		super(context, attrs);
		sharedConstructor(context);
	}

	public ViewChooseShareApp(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		sharedConstructor(context);
	}
	
	private void sharedConstructor(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_share_application_list, this);
		
		appListView = (ListView)findViewById(R.id.application_list);
		appListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(chooseListener != null) {
					ChooseApplicationItem item = listData[position];
					chooseListener.itemSelected(item);
				}
			}
		});
		title	= (TextView)findViewById(R.id.choose_application_title);
	}
	
	public void setListData(ChooseApplicationItem[] listData) {
		this.listData = listData;
		appListView.setAdapter(new AdapterChooseShareApp(getContext(), listData));
	}
	
	public void setSelectionListener(ChooseApplicationSelectionListener l) {
		chooseListener = l;
	}
	
	public void setTitleText(String text){
		title.setText(text);
	}
	
}
