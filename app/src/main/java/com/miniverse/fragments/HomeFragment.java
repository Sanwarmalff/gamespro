package com.miniverse.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.miniverse.GamePlayerActivity;
import com.miniverse.adapters.GameAdapter;
import com.miniverse.databinding.FragmentHomeBinding;
import com.miniverse.models.Game;
import com.miniverse.utils.GameDownloader;
import com.miniverse.utils.GameManager;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private GameAdapter adapter;
    private GameManager gameManager;
    private GameDownloader gameDownloader;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Utilities
        gameManager = GameManager.getInstance(requireContext());
        gameDownloader = new GameDownloader(gameManager);

        setupRecyclerView();

        // Setup Swipe to Refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadGames);

        // Initial Data Load
        loadGames();
    }

    private void setupRecyclerView() {
        adapter = new GameAdapter((game, position, holder) -> {
            if (game.isInstalled()) {
                // Game is installed, launch GamePlayerActivity
                Intent intent = new Intent(requireContext(), GamePlayerActivity.class);
                intent.putExtra("GAME_ID", game.getId());
                startActivity(intent);
            } else {
                // Trigger Download
                holder.updateProgress(0); // Show initial progress bar UI
                
                gameDownloader.downloadAndInstallGame(game, new GameDownloader.DownloadCallback() {
                    @Override
                    public void onProgress(int percent) {
                        holder.updateProgress(percent);
                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), game.getTitle() + " installed!", Toast.LENGTH_SHORT).show();
                        // Update local state and trigger UI change in adapter
                        game.setInstalled(true);
                        adapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        holder.updateProgress(-1); // Hide progress bar and reset button
                    }
                });
            }
        });

        binding.rvGames.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGames.setAdapter(adapter);
    }

    private void loadGames() {
        binding.swipeRefreshLayout.setRefreshing(true);
        binding.tvEmptyState.setVisibility(View.GONE);

        // Fetch games.json from GitHub
        gameDownloader.fetchGamesList(GameDownloader.DEFAULT_JSON_URL, new GameDownloader.GameListCallback() {
            @Override
            public void onSuccess(List<Game> games) {
                binding.swipeRefreshLayout.setRefreshing(false);
                
                if (games.isEmpty()) {
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    // Sync the remote JSON list with our local installation states
                    for (Game game : games) {
                        game.setInstalled(gameManager.isGameInstalled(game.getId()));
                        game.setFavorite(gameManager.isFavorite(game.getId()));
                    }
                    adapter.setGames(games);
                }
            }

            @Override
            public void onError(String error) {
                binding.swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), "Failed to load games: " + error, Toast.LENGTH_SHORT).show();
                
                if (adapter.getItemCount() == 0) {
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    binding.tvEmptyState.setText(error);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks by clearing view binding reference
    }
}
