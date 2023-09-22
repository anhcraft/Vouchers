package dev.anhcraft.vouchers.storage.player;

import dev.anhcraft.vouchers.api.data.PlayerData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class PlayerDataImpl implements PlayerData {
    private final PlayerDataConfig config;

    public PlayerDataImpl(@NotNull PlayerDataConfig config) {
        this.config = config;
    }

    @Override
    public int getDataVersion() {
        return config.dataVersion;
    }

    @Override
    public boolean isDirty() {
        return config.dirty.get();
    }

    @ApiStatus.Internal
    PlayerDataConfig internal() {
        return config;
    }
}
