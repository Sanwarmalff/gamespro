package com.miniverse;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.miniverse.databinding.ActivityGamePlayerBinding;
import com.miniverse.utils.GameManager;

import java.io.File;

public class GamePlayerActivity extends AppCompatActivity {

    private ActivityGamePlayerBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup ViewBinding
        binding = ActivityGamePlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide system UI for immersive fullscreen mode
        hideSystemUI();

        String gameId = getIntent().getStringExtra("GAME_ID");
        if (gameId == null) {
            Toast.makeText(this, "Error loading game.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupWebView();
        loadGame(gameId);

        // Setup exit button
        binding.fabBack.setOnClickListener(v -> finish());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = binding.webView.getSettings();
        
        // Essential settings for HTML5 games
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        
        // Optimize performance
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        // Prevent opening external browser
        binding.webView.setWebViewClient(new WebViewClient());
        binding.webView.setWebChromeClient(new WebChromeClient());
    }

    private void loadGame(String gameId) {
        GameManager gameManager = GameManager.getInstance(this);
        File gameDir = gameManager.getGameDirectory(gameId);
        File indexFile = new File(gameDir, "index.html");

        if (indexFile.exists()) {
            // Load the local index.html file into the WebView
            binding.webView.loadUrl("file://" + indexFile.getAbsolutePath());
        } else {
            Toast.makeText(this, "Game files not found. Please redownload.", Toast.LENGTH_SHORT).show();
            gameManager.removeInstalledGame(gameId); // Clean up corrupted state
            finish();
        }
    }

    private void hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), binding.getRoot());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    protected void onDestroy() {
        if (binding.webView != null) {
            binding.webView.destroy();
        }
        super.onDestroy();
    }
}
