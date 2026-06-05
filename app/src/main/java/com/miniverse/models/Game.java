package com.miniverse.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Game implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("zip_url")
    private String zipUrl;

    @SerializedName("icon_url")
    private String iconUrl;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("version")
    private String version;

    // Local state fields (transient means they won't be expected in the GitHub JSON parsing)
    private transient boolean isInstalled;
    private transient boolean isFavorite;
    private transient String localFolderPath;

    public Game() {
        // Required empty constructor for serialization
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getZipUrl() { return zipUrl; }
    public String getIconUrl() { return iconUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getVersion() { return version; }
    public boolean isInstalled() { return isInstalled; }
    public boolean isFavorite() { return isFavorite; }
    public String getLocalFolderPath() { return localFolderPath; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setZipUrl(String zipUrl) { this.zipUrl = zipUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setVersion(String version) { this.version = version; }
    public void setInstalled(boolean installed) { isInstalled = installed; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setLocalFolderPath(String localFolderPath) { this.localFolderPath = localFolderPath; }
}
