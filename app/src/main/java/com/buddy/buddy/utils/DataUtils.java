package com.buddy.buddy.utils;

import android.content.Context;

import com.buddy.buddy.R;
import com.buddy.buddy.domain.DriverCategory;

import java.util.ArrayList;

public class DataUtils {

    public static ArrayList<DriverCategory> getAllDriverCategories(Context context) {
        ArrayList<DriverCategory> driverCategories = new ArrayList<>();
        String[] resourceCategories = context.getResources().getStringArray(R.array.buddy_category);
        for (int i = 0; i < resourceCategories.length; i++) {
            String resourceCategory = resourceCategories[i];
            driverCategories.add(new DriverCategory(String.valueOf(i), resourceCategory));
        }
        return driverCategories;
    }
}
