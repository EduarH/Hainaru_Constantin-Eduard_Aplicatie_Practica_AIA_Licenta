package com.buddy.buddy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.buddy.buddy.R;
import com.buddy.buddy.domain.DriverCategory;
import java.util.ArrayList;


public class DriverCategoryAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<DriverCategory> driverCategories;
    private LayoutInflater inflter;

    public DriverCategoryAdapter(Context context,
                         ArrayList<DriverCategory> driverCategoryArrayList) {
        this.context = context;
        this.driverCategories = driverCategoryArrayList;
        inflter = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return driverCategories.size();
    }

    @Override
    public Object getItem(int i) {
        return driverCategories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return Long.valueOf(i);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_driver_category_dropdown, parent, false);
        }
        TextView textViewName = convertView.findViewById(R.id.text_view);
        DriverCategory currentItem = driverCategories.get(position);
        if (currentItem != null) {
            textViewName.setText(currentItem.getName());
        }
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_driver_category, viewGroup, false);
        }
        TextView textViewName = convertView.findViewById(R.id.text_view);
        DriverCategory currentItem = driverCategories.get(position);
        if (currentItem != null) {
            textViewName.setText(currentItem.getName());
        }
        return convertView;
    }
}
