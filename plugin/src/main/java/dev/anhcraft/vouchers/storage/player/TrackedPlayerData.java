package dev.anhcraft.vouchers.storage.player;

import org.jetbrains.annotations.NotNull;

public class TrackedPlayerData {
    private final PlayerDataImpl playerData;
    private long loadTime;

    public TrackedPlayerData(@NotNull PlayerDataImpl playerData, long loadTime) {
        this.playerData = playerData;
        this.loadTime = loadTime;
    }

    @NotNull
    public PlayerDataImpl getPlayerData() {
        return playerData;
    }

    public long getLoadTime() {
        return Math.abs(loadTime);
    }

    public boolean isShortTerm() {
        return loadTime > 0;
    }

    public void setShortTerm() {
        loadTime = System.currentTimeMillis();
    }

    public void setLongTerm() {
        loadTime = -System.currentTimeMillis();
    }
}
