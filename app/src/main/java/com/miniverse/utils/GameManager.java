package com.miniverse.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.miniverse.models.Game;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static final String PREFS_NAME = "MiniVersePrefs";
    private static final String KEY_INSTALLED_GAMES = "installed_games";
    private static final String KEY_FAVORITES = "favorite_games";

    private static GameManager instance;
    private final SharedPreferences prefs;
    private final Gson gson;
    private final Context context;

    private GameManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public static synchronized GameManager getInstance(Context context) {
        if (instance == null) {
            instance = new GameManager(context);
        }
        return instance;
    }

    // --- Installed Games Management ---

    public List<Game> getInstalledGames() {
        String json = prefs.getString(KEY_INSTALLED_GAMES, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Game>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public boolean isGameInstalled(String gameId) {
        for (Game g : getInstalledGames()) {
            if (g.getId().equals(gameId)) return true;
        }
        return false;
    }

    public void addInstalledGame(Game game) {
        List<Game> games = getInstalledGames();
        // Remove if it already exists to update it properly
        games.removeIf(g -> g.getId().equals(game.getId()));
        
        // Update local state and save path
        game.setInstalled(true);
        game.setLocalFolderPath(getGameDirectory(game.getId()).getAbsolutePath());
        
        games.add(game);
        prefs.edit().putString(KEY_INSTALLED_GAMES, gson.toJson(games)).apply();
    }

    public void removeInstalledGame(String gameId) {
        List<Game> games = getInstalledGames();
        games.removeIf(g -> g.getId().equals(gameId));
        prefs.edit().putString(KEY_INSTALLED_GAMES, gson.toJson(games)).apply();
        
        // Delete actual files from internal storage
        deleteDirectory(getGameDirectory(gameId));
    }

    // --- Favorites Management ---

    public List<Game> getFavoriteGames() {
        String json = prefs.getString(KEY_FAVORITES, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Game>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public boolean isFavorite(String gameId) {
        for (Game g : getFavoriteGames()) {
            if (g.getId().equals(gameId)) return true;
        }
        return false;
    }

    public void toggleFavorite(Game game) {
        List<Game> favorites = getFavoriteGames();
        boolean removed = favorites.removeIf(g -> g.getId().equals(game.getId()));
        if (!removed) {
            game.setFavorite(true);
            favorites.add(game);
        }
        prefs.edit().putString(KEY_FAVORITES, gson.toJson(favorites)).apply();
    }

    // --- File Management ---

    public File getGamesBaseDirectory() {
        File dir = new File(context.getFilesDir(), "games");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public File getGameDirectory(String gameId) {
        File dir = new File(getGamesBaseDirectory(), gameId);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private void deleteDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        fileOrDirectory.delete();
    }
}
