package com.miniverse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.android.material.button.MaterialButton;
import com.miniverse.R;
import com.miniverse.models.Game;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> gameList = new ArrayList<>();
    private final OnGameClickListener listener;

    public interface OnGameClickListener {
        void onActionClick(Game game, int position, GameViewHolder holder);
    }

    public GameAdapter(OnGameClickListener listener) {
        this.listener = listener;
    }

    public void setGames(List<Game> games) {
        this.gameList = games;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = gameList.get(position);
        holder.bind(game, listener, position);
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivThumbnail;
        private final TextView tvTitle;
        private final TextView tvCategory;
        private final MaterialButton btnAction;
        private final ProgressBar pbDownload;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnAction = itemView.findViewById(R.id.btnAction);
            pbDownload = itemView.findViewById(R.id.pbDownload);
        }

        public void bind(Game game, OnGameClickListener listener, int position) {
            tvTitle.setText(game.getTitle());
            String categoryText = game.getCategory() + " • v" + game.getVersion();
            tvCategory.setText(categoryText);

            Glide.with(itemView.getContext())
                    .load(game.getThumbnailUrl())
                    .transform(new CenterCrop())
                    .into(ivThumbnail);

            if (game.isInstalled()) {
                btnAction.setText(R.string.play_now);
                btnAction.setIconResource(android.R.drawable.ic_media_play);
            } else {
                btnAction.setText(R.string.download);
                btnAction.setIconResource(android.R.drawable.stat_sys_download);
            }

            // Reset progress bar state on bind
            pbDownload.setVisibility(View.GONE);
            pbDownload.setProgress(0);
            btnAction.setEnabled(true);

            btnAction.setOnClickListener(v -> listener.onActionClick(game, position, this));
        }

        // Helper method to update download progress dynamically
        public void updateProgress(int progress) {
            if (progress >= 0 && progress < 100) {
                pbDownload.setVisibility(View.VISIBLE);
                pbDownload.setProgress(progress);
                btnAction.setEnabled(false);
                btnAction.setText(R.string.loading);
            } else {
                pbDownload.setVisibility(View.GONE);
                btnAction.setEnabled(true);
                btnAction.setText(R.string.play_now);
                btnAction.setIconResource(android.R.drawable.ic_media_play);
            }
        }
    }
}
