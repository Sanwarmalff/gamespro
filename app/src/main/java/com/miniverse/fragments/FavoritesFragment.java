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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.miniverse.GamePlayerActivity;
import com.miniverse.adapters.GameAdapter;
import com.miniverse.databinding.FragmentFavoritesBinding;
import com.miniverse.models.Game;
import com.miniverse.utils.GameDownloader;
import com.miniverse.utils.GameManager;

import java.util.List;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private GameAdapter adapter;
    private GameManager gameManager;
    private GameDownloader gameDownloader;
    private List<Game> favoriteGames;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gameManager = GameManager.getInstance(requireContext());
        gameDownloader = new GameDownloader(gameManager);

        setupRecyclerView();
        setupSwipeToRemove();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavoriteGames();
    }

    private void setupRecyclerView() {
        adapter = new GameAdapter((game, position, holder) -> {
            if (gameManager.isGameInstalled(game.getId())) {
                Intent intent = new Intent(requireContext(), GamePlayerActivity.class);
                intent.putExtra("GAME_ID", game.getId());
                startActivity(intent);
            } else {
                holder.updateProgress(0);
                gameDownloader.downloadAndInstallGame(game, new GameDownloader.DownloadCallback() {
                    @Override
                    public void onProgress(int percent) {
                        holder.updateProgress(percent);
                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), game.getTitle() + " installed!", Toast.LENGTH_SHORT).show();
                        game.setInstalled(true);
                        adapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        holder.updateProgress(-1);
                    }
                });
            }
        });

        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFavorites.setAdapter(adapter);
    }

    private void setupSwipeToRemove() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Game gameToRemove = favoriteGames.get(position);

                // Remove from favorites local storage
                gameManager.toggleFavorite(gameToRemove); 
                favoriteGames.remove(position);
                adapter.notifyItemRemoved(position);

                Snackbar.make(binding.getRoot(), gameToRemove.getTitle() + " removed from favorites.", Snackbar.LENGTH_SHORT).show();

                if (favoriteGames.isEmpty()) {
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvFavorites);
    }

    private void loadFavoriteGames() {
        favoriteGames = gameManager.getFavoriteGames();
        
        // Sync installation state just in case it was downloaded or deleted elsewhere
        for (Game game : favoriteGames) {
            game.setInstalled(gameManager.isGameInstalled(game.getId()));
        }
        
        if (favoriteGames.isEmpty()) {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            binding.rvFavorites.setVisibility(View.GONE);
        } else {
            binding.tvEmptyState.setVisibility(View.GONE);
            binding.rvFavorites.setVisibility(View.VISIBLE);
            adapter.setGames(favoriteGames);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
