package com.miniverse.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

public static final String DEFAULT_JSON_URL =
        "https://raw.githubusercontent.com/Sanwarmalff/gamespro/main/games.json";

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
    this.mainHandler = new Handler(Looper.getMainLooper());
}

public void fetchGamesList(String url, GameListCallback callback) {

    Log.d("MINIVERSE", "Loading URL: " + url);

    Request request = new Request.Builder()
            .url(url)
            .build();

    client.newCall(request).enqueue(new Callback() {

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {

            Log.e("MINIVERSE", "NETWORK ERROR", e);

            mainHandler.post(() ->
                    callback.onError("NETWORK ERROR:\n" + e.getMessage())
            );
        }

        @Override
        public void onResponse(@NonNull Call call,
                               @NonNull Response response) throws IOException {

            Log.d("MINIVERSE",
                    "HTTP CODE = " + response.code());

            if (response.isSuccessful() && response.body() != null) {

                String json = response.body().string();

                Log.d("MINIVERSE", "JSON:");
                Log.d("MINIVERSE", json);

                Type listType =
                        new TypeToken<List<Game>>() {
                        }.getType();

                try {

                    List<Game> games =
                            gson.fromJson(json, listType);

                    mainHandler.post(() ->
                            callback.onSuccess(games));

                } catch (Exception e) {

                    Log.e("MINIVERSE",
                            "JSON PARSE ERROR", e);

                    mainHandler.post(() ->
                            callback.onError(
                                    "JSON PARSE ERROR:\n"
                                            + e.getMessage()
                            )
                    );
                }

            } else {

                String bodyText = "";

                try {
                    if (response.body() != null) {
                        bodyText = response.body().string();
                    }
                } catch (Exception ignored) {
                }

                String error =
                        "HTTP ERROR\n\n" +
                                "Code: " + response.code() +
                                "\nMessage: " + response.message() +
                                "\n\nBody:\n" + bodyText;

                Log.e("MINIVERSE", error);

                String finalError = error;

                mainHandler.post(() ->
                        callback.onError(finalError)
                );
            }
        }
    });
}

public void downloadAndInstallGame(Game game,
                                   DownloadCallback callback) {

    Request request = new Request.Builder()
            .url(game.getZipUrl())
            .build();

    client.newCall(request).enqueue(new Callback() {

        @Override
        public void onFailure(@NonNull Call call,
                              @NonNull IOException e) {

            mainHandler.post(() ->
                    callback.onError(
                            "Download Error:\n"
                                    + e.getMessage()
                    )
            );
        }

        @Override
        public void onResponse(@NonNull Call call,
                               @NonNull Response response) {

            if (!response.isSuccessful()
                    || response.body() == null) {

                mainHandler.post(() ->
                        callback.onError(
                                "Download Failed\nHTTP "
                                        + response.code()
                        )
                );

                return;
            }

            File gameDir =
                    gameManager.getGameDirectory(
                            game.getId());

            File zipFile =
                    new File(
                            gameDir,
                            game.getId() + ".zip"
                    );

            try (InputStream is =
                         response.body().byteStream();

                 FileOutputStream fos =
                         new FileOutputStream(zipFile)) {

                byte[] buffer = new byte[8192];

                int count;

                while ((count = is.read(buffer))
                        != -1) {

                    fos.write(
                            buffer,
                            0,
                            count
                    );
                }

                fos.flush();

                ZipFile zip =
                        new ZipFile(zipFile);

                zip.extractAll(
                        gameDir.getAbsolutePath()
                );

                zipFile.delete();

                mainHandler.post(() -> {
                    gameManager.addInstalledGame(game);
                    callback.onSuccess();
                });

            } catch (Exception e) {

                mainHandler.post(() ->
                        callback.onError(
                                "Install Error:\n"
                                        + e.getMessage()
                        )
                );
            }
        }
    });
}

}
