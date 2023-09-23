package dev.anhcraft.vouchers.storage.player;

import com.google.common.base.Preconditions;
import dev.anhcraft.vouchers.api.data.PlayerData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class PlayerDataImpl implements PlayerData {
    private final PlayerDataConfig config;

    PlayerDataImpl(@NotNull PlayerDataConfig config) {
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

    @Override
    public long getLastUsed(String voucherId) {
        return config.lastUsed.getOrDefault(voucherId, 0L);
    }

    @Override
    public void setLastUsed(String voucherId, long timestamp) {
        Preconditions.checkArgument(timestamp >= 0, "Timestamp must not be negative");
        if (timestamp > 0) config.lastUsed.put(voucherId, timestamp);
        else config.lastUsed.remove(voucherId);
        config.markDirty();
    }

    @Override
    public int getUsageLimitCount(String voucherId) {
        return config.usageLimitCount.getOrDefault(voucherId, 0);
    }

    @Override
    public void setUsageLimitCount(String voucherId, int count) {
        Preconditions.checkArgument(count >= 0, "Count must not be negative");
        if (count > 0) config.usageLimitCount.put(voucherId, count);
        else config.usageLimitCount.remove(voucherId);
        config.markDirty();
    }
}
