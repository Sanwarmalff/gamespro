package com.miniverse.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.miniverse.databinding.FragmentSettingsBinding;
import com.miniverse.models.Game;
import com.miniverse.utils.GameManager;

import java.util.List;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private GameManager gameManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gameManager = GameManager.getInstance(requireContext());

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Clear Image Cache
        binding.cardClearCache.setOnClickListener(v -> {
            new Thread(() -> {
                // Glide requires clearDiskCache to be called on a background thread
                Glide.get(requireContext()).clearDiskCache();
                
                requireActivity().runOnUiThread(() -> {
                    // clearMemory must be called on the main thread
                    Glide.get(requireContext()).clearMemory();
                    Toast.makeText(requireContext(), "Image cache cleared successfully.", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });

        // Delete All Installed Games
        binding.cardDeleteAllGames.setOnClickListener(v -> {
            List<Game> installedGames = gameManager.getInstalledGames();
            
            if (installedGames.isEmpty()) {
                Toast.makeText(requireContext(), "No games to delete.", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete All Games")
                    .setMessage("Are you sure you want to delete all offline game files? This action cannot be undone.")
                    .setPositiveButton("Delete All", (dialog, which) -> {
                        // Iterate and delete all game files
                        for (Game game : installedGames) {
                            gameManager.removeInstalledGame(game.getId());
                        }
                        Toast.makeText(requireContext(), "All installed games have been deleted.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        
        // About Section (Visual only, no logic needed for now)
        binding.cardAbout.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "MiniVerse - Enjoy offline gaming!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
