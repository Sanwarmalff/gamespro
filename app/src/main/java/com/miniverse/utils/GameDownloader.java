package com.miniverse.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.miniverse.models.Game;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GameDownloader {

    // Default repository URL (can be customized or passed dynamically)
    public static final String DEFAULT_JSON_URL = "https://raw.githubusercontent.com/Sanwarmalff/gamespro/main/games.json";
    
    private final OkHttpClient client;
    private final Gson gson;
    private final GameManager gameManager;
    private final Handler mainHandler;

    public interface GameListCallback {
        void onSuccess(List<Game> games);
        void onError(String error);
    }

    public interface DownloadCallback {
        void onProgress(int percent);
        void onSuccess();
        void onError(String error);
    }

    public GameDownloader(GameManager gameManager) {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.gameManager = gameManager;
        // Ensures callbacks are executed on the Main (UI) Thread
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void fetchGamesList(String url, GameListCallback callback) {
        Request request = new Request.Builder().url(url).build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Type listType = new TypeToken<List<Game>>(){}.getType();
                    try {
                        List<Game> games = gson.fromJson(json, listType);
                        mainHandler.post(() -> callback.onSuccess(games));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("Failed to parse games list."));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Server returned an error."));
                }
            }
        });
    }

    public void downloadAndInstallGame(Game game, DownloadCallback callback) {
        Request request = new Request.Builder().url(game.getZipUrl()).build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful() || response.body() == null) {
                    mainHandler.post(() -> callback.onError("Download failed."));
                    return;
                }

                File gameDir = gameManager.getGameDirectory(game.getId());
                File zipFile = new File(gameDir, game.getId() + ".zip");

                try (InputStream is = response.body().byteStream();
                     FileOutputStream fos = new FileOutputStream(zipFile)) {

                    long totalBytes = response.body().contentLength();
                    long downloadedBytes = 0;
                    byte[] buffer = new byte[8192]; // 8KB buffer
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;
                        
                        if (totalBytes > 0) {
                            int progress = (int) ((downloadedBytes * 100) / totalBytes);
                            // Only update UI every so often or safely post to avoid flooding the main thread, 
                            // but for simplicity, we post the progress directly here.
                            mainHandler.post(() -> callback.onProgress(progress));
                        }
                    }
                    fos.flush();

                    // Extract the ZIP file directly into the game's directory
                    try (ZipFile zip = new ZipFile(zipFile)) {
                        zip.extractAll(gameDir.getAbsolutePath());
                    }
                    
                    // Clean up the ZIP file after successful extraction to save space
                    if (zipFile.exists()) {
                        zipFile.delete();
                    }

                    mainHandler.post(() -> {
                        gameManager.addInstalledGame(game);
                        callback.onSuccess();
                    });

                } catch (Exception e) {
                    // Clean up partially downloaded or failed files
                    if (zipFile.exists()) {
                        zipFile.delete();
                    }
                    mainHandler.post(() -> callback.onError("Installation failed: " + e.getMessage()));
                }
            }
        });
    }
}
