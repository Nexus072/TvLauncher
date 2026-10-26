package com.example.tvlauncher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.Holder> {

    public interface Listener {
        void onClick(AppInfo app);
    }

    private final List<AppInfo> items;
    private final Listener listener;

    public AppAdapter(List<AppInfo> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        AppInfo app = items.get(position);
        h.icon.setImageDrawable(app.icon);
        h.label.setText(app.label);

        h.itemView.setOnClickListener(v -> listener.onClick(app));
        h.itemView.setOnFocusChangeListener((v, focused) -> {
            float scale = focused ? 1.1f : 1f;
            v.animate().scaleX(scale).scaleY(scale).setDuration(120).start();
            h.label.setVisibility(focused ? View.VISIBLE : View.INVISIBLE);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView label;

        Holder(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            label = v.findViewById(R.id.label);
        }
    }
}
