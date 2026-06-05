package com.miniverse.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miniverse.GamePlayerActivity;
import com.miniverse.adapters.GameAdapter;
import com.miniverse.databinding.FragmentInstalledBinding;
import com.miniverse.models.Game;
import com.miniverse.utils.GameManager;

import java.util.List;

public class InstalledFragment extends Fragment {

    private FragmentInstalledBinding binding;
    private GameAdapter adapter;
    private GameManager gameManager;
    private List<Game> installedGames;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInstalledBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gameManager = GameManager.getInstance(requireContext());
        
        setupRecyclerView();
        setupSwipeToDelete();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the list every time the fragment becomes visible
        loadInstalledGames();
    }

    private void setupRecyclerView() {
        adapter = new GameAdapter((game, position, holder) -> {
            if (game.isInstalled()) {
                Intent intent = new Intent(requireContext(), GamePlayerActivity.class);
                intent.putExtra("GAME_ID", game.getId());
                startActivity(intent);
            }
        });

        binding.rvInstalledGames.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvInstalledGames.setAdapter(adapter);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want drag & drop, only swipe
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Game gameToDelete = installedGames.get(position);

                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Game")
                        .setMessage("Are you sure you want to delete " + gameToDelete.getTitle() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Delete from storage and local database
                            gameManager.removeInstalledGame(gameToDelete.getId());
                            installedGames.remove(position);
                            adapter.notifyItemRemoved(position);
                            
                            Toast.makeText(requireContext(), "Game deleted.", Toast.LENGTH_SHORT).show();
                            
                            // Show empty state if list is now empty
                            if (installedGames.isEmpty()) {
                                binding.tvEmptyState.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            adapter.notifyItemChanged(position); // Revert swipe visual
                        })
                        .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                        .show();
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvInstalledGames);
    }

    private void loadInstalledGames() {
        installedGames = gameManager.getInstalledGames();
        
        if (installedGames.isEmpty()) {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            binding.rvInstalledGames.setVisibility(View.GONE);
        } else {
            binding.tvEmptyState.setVisibility(View.GONE);
            binding.rvInstalledGames.setVisibility(View.VISIBLE);
            adapter.setGames(installedGames);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
