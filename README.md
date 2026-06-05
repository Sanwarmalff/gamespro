# MiniVerse Games Repository

Welcome to the official game repository for the **MiniVerse** Android app. This repository hosts the `games.json` catalog and the static game files (HTML5/JS/CSS) packaged as `.zip` archives. 

## Repository Structure

To ensure the Android app parses and downloads the games correctly, the repository must maintain the following structure:

```text
repo/
│
├── games.json                 # Master catalog parsed by the Android app
│
└── games/                     # Directory containing all game assets
    ├── snake/
    │   ├── game.zip           # Contains index.html, style.css, game.js
    │   ├── icon.png           # 512x512 App Icon
    │   └── thumbnail.png      # 16:9 Banner Thumbnail (used in RecyclerView)
    │
    ├── racing/
    │   ├── game.zip
    │   ├── icon.png
    │   └── thumbnail.png
    │
    └── puzzle/
        ├── game.zip
        ├── icon.png
        └── thumbnail.png
# gamespro