package com.example.tvlauncher;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public final String packageName;
    public final String activityName;
    public final String label;
    public final Drawable icon;

    public AppInfo(String packageName, String activityName, String label, Drawable icon) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.label = label;
        this.icon = icon;
    }
}
