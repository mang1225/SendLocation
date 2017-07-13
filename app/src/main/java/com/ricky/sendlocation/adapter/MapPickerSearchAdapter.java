package com.ricky.sendlocation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ricky.sendlocation.R;
import com.ricky.sendlocation.bean.LocationBean;

import java.util.List;


public class MapPickerSearchAdapter extends BaseAdapter {

  private final Context context;
  private LayoutInflater mInflater;
  private List<LocationBean> resultList;
  private int notifyTip;

  public MapPickerSearchAdapter(Context context, List<LocationBean> items) {
    resultList = items;
    mInflater = LayoutInflater.from(context);
    this.context = context;
    this.notifyTip = 0;
  }

  @Override
  public int getCount() {
    return resultList.size();
  }

  @Override
  public Object getItem(int index) {
    return resultList.get(index);
  }

  @Override
  public long getItemId(int i) {
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup viewGroup) {

    MyViewHolder holder = null;
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.picker_item_place, viewGroup, false);
      holder = new MyViewHolder();
      holder.titleView = (TextView) convertView.findViewById(R.id.title);
      holder.subtitleView = (TextView) convertView.findViewById(R.id.subtitle);
      holder.iconView = (ImageView) convertView.findViewById(R.id.iconView);
      convertView.setTag(holder);
    } else {
      holder = (MyViewHolder) convertView.getTag();
    }
    holder.titleView.setText(resultList.get(position).getLocName());
    holder.subtitleView.setText(resultList.get(position).getAddStr());
    holder.iconView.setVisibility(View.GONE);

    return convertView;
  }

  static class MyViewHolder {

    TextView titleView;
    TextView subtitleView;
    ImageView iconView;
  }

}