package com.miniverse.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.miniverse.GamePlayerActivity;
import com.miniverse.R;
import com.miniverse.adapters.GameAdapter;
import com.miniverse.databinding.FragmentGamesBinding;
import com.miniverse.models.Game;
import com.miniverse.utils.GameDownloader;
import com.miniverse.utils.GameManager;

import java.util.ArrayList;
import java.util.List;

public class GamesFragment extends Fragment {

    private FragmentGamesBinding binding;
    private GameAdapter adapter;
    private GameManager gameManager;
    private GameDownloader gameDownloader;
    
    private List<Game> allGames = new ArrayList<>();
    private String currentQuery = "";
    private String currentCategory = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGamesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gameManager = GameManager.getInstance(requireContext());
        gameDownloader = new GameDownloader(gameManager);

        setupRecyclerView();
        setupSearchAndFilters();

        binding.swipeRefreshLayout.setOnRefreshListener(this::loadGames);

        loadGames();
    }

    private void setupRecyclerView() {
        adapter = new GameAdapter((game, position, holder) -> {
            if (game.isInstalled()) {
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

        binding.rvGames.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGames.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        // Handle Search Text Changes
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle Category Chip Selection
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    currentCategory = chip.getText().toString();
                    applyFilters();
                }
            }
        });
    }

    private void applyFilters() {
        List<Game> filteredList = new ArrayList<>();
        
        for (Game game : allGames) {
            boolean matchesSearch = game.getTitle().toLowerCase().contains(currentQuery);
            boolean matchesCategory = currentCategory.equals("All") || game.getCategory().equalsIgnoreCase(currentCategory);
            
            if (matchesSearch && matchesCategory) {
                filteredList.add(game);
            }
        }
        
        adapter.setGames(filteredList);
        
        if (filteredList.isEmpty()) {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void loadGames() {
        binding.swipeRefreshLayout.setRefreshing(true);
        binding.tvEmptyState.setVisibility(View.GONE);

        gameDownloader.fetchGamesList(GameDownloader.DEFAULT_JSON_URL, new GameDownloader.GameListCallback() {
            @Override
            public void onSuccess(List<Game> games) {
                binding.swipeRefreshLayout.setRefreshing(false);
                
                for (Game game : games) {
                    game.setInstalled(gameManager.isGameInstalled(game.getId()));
                    game.setFavorite(gameManager.isFavorite(game.getId()));
                }
                
                allGames = games;
                applyFilters(); // Apply active search and filters immediately
            }

            @Override
            public void onError(String error) {
                binding.swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), "Failed to load games: " + error, Toast.LENGTH_SHORT).show();
                
                if (allGames.isEmpty()) {
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    binding.tvEmptyState.setText(error);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
