package com.example.basketballgame;

import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basketballgame.data.LeaderboardEntry;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<LeaderboardEntry> entries;

    public LeaderboardAdapter(List<LeaderboardEntry> entries) {
        this.entries = entries;
    }

    public void update(List<LeaderboardEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout layout = new LinearLayout(parent.getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(dp(parent, 16), dp(parent, 12), dp(parent, 16), dp(parent, 12));
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setBackgroundResource(R.drawable.btn_rounded_dark);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rootParams.setMargins(dp(parent, 6), dp(parent, 8), dp(parent, 6), 0);
        layout.setLayoutParams(rootParams);

        TextView position = new TextView(parent.getContext());
        position.setId(android.R.id.text1);
        position.setTextColor(0xFFFFFFFF);
        position.setTextSize(18);
        position.setGravity(Gravity.CENTER);
        position.setMinEms(2);
        position.setTextSize(16);
        position.setAlpha(0.95f);
        LinearLayout.LayoutParams posParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.18f);
        layout.addView(position, posParams);

        TextView name = new TextView(parent.getContext());
        name.setId(android.R.id.text2);
        name.setTextColor(0xFFFFFFFF);
        name.setTextSize(18);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setTextSize(16);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.56f);
        layout.addView(name, nameParams);

        TextView score = new TextView(parent.getContext());
        score.setTextColor(0xFFFFEE58);
        score.setTextSize(18);
        score.setGravity(Gravity.END);
        score.setTextSize(18);
        score.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.26f);
        layout.addView(score, scoreParams);

        return new ViewHolder(layout, position, name, score);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = entries.get(position);
        holder.position.setText(String.valueOf(position + 1));
        holder.name.setText(entry.playerName == null || entry.playerName.isEmpty() ? "Игрок" : entry.playerName);
        holder.score.setText(String.valueOf(entry.score));

        int rank = position + 1;
        int accent;
        if (rank == 1) accent = 0xFFFFD54F;
        else if (rank == 2) accent = 0xFFB0BEC5;
        else if (rank == 3) accent = 0xFFD7A37A;
        else accent = 0x66FFFFFF;

        holder.position.setTextColor(accent);
        holder.score.setTextColor(accent);
        holder.itemView.setAlpha(rank <= 3 ? 1f : 0.94f);
        holder.itemView.setScaleX(1f);
        holder.itemView.setScaleY(1f);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView position;
        TextView name;
        TextView score;

        ViewHolder(@NonNull LinearLayout itemView, TextView position, TextView name, TextView score) {
            super(itemView);
            this.position = position;
            this.name = name;
            this.score = score;
        }
    }

    private int dp(ViewGroup parent, int value) {
        return (int) (value * parent.getResources().getDisplayMetrics().density);
    }
}
