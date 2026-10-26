package com.example.tvlauncher;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // Приложения нижнего ряда в нужном порядке. Меняйте здесь имена пакетов.
    static final String[] DOCK_PACKAGES = {
            "ru.kinopoisk.tv",
            "ru.rt.video.app.tv",
            "com.yandex.browser.tv",
            "tv.twitch.android.app"
    };
    static final int COLUMNS = 5;
    static final int DOCK_SIZE = 4;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView clockView;
    private TextView dateView;

    private final Runnable clockTick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 30_000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clockView = findViewById(R.id.clock);
        dateView = findViewById(R.id.date);

        List<AppInfo> dock = new ArrayList<>();
        List<AppInfo> grid = new ArrayList<>();
        loadApps(dock, grid);

        RecyclerView dockView = findViewById(R.id.dock);
        dockView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dockView.setAdapter(new AppAdapter(dock, this::launch));

        RecyclerView gridView = findViewById(R.id.grid);
        gridView.setLayoutManager(new GridLayoutManager(this, COLUMNS));
        gridView.setAdapter(new AppAdapter(grid, this::launch));

        dockView.post(dockView::requestFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(clockTick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(clockTick);
    }

    @Override
    public void onBackPressed() {
        // На домашнем экране «Назад» ничего не делает.
    }

    private void loadApps(List<AppInfo> dock, List<AppInfo> grid) {
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> found = pm.queryIntentActivities(query, 0);

        Set<String> seen = new HashSet<>();
        List<AppInfo> all = new ArrayList<>();
        for (ResolveInfo ri : found) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || !seen.add(pkg)) continue;
            all.add(new AppInfo(pkg, ri.activityInfo.name,
                    ri.loadLabel(pm).toString(), ri.loadIcon(pm)));
        }

        for (String pkg : DOCK_PACKAGES) {
            for (AppInfo app : all) {
                if (app.packageName.equals(pkg) && !dock.contains(app)) {
                    dock.add(app);
                    break;
                }
            }
        }
        // Если ни одного из выбранных приложений нет, берём первые по списку.
        if (dock.isEmpty()) {
            for (int i = 0; i < Math.min(DOCK_SIZE, all.size()); i++) {
                dock.add(all.get(i));
            }
        }

        for (AppInfo app : all) {
            if (!dock.contains(app)) grid.add(app);
        }
        Collections.sort(grid, Comparator.comparing((AppInfo a) -> a.label.toLowerCase(Locale.ROOT)));
    }

    private void launch(AppInfo app) {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                .setComponent(new ComponentName(app.packageName, app.activityName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Не удалось открыть " + app.label, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateClock() {
        Date now = new Date();
        clockView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
        dateView.setText(new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now));
    }
}
