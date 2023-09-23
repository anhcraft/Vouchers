package dev.anhcraft.vouchers.storage.server;

import com.google.common.base.Preconditions;
import dev.anhcraft.vouchers.api.data.ServerData;

public class ServerDataImpl implements ServerData {
    private final ServerDataConfig config;

    ServerDataImpl(ServerDataConfig config) {
        this.config = config;
    }

    @Override
    public boolean isDirty() {
        return config.dirty.get();
    }

    @Override
    public int getDataVersion() {
        return config.dataVersion;
    }

    ServerDataConfig internal() {
        return config;
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

    @Override
    public int getUsageCount(String voucherId) {
        return config.usageCount.getOrDefault(voucherId, 0);
    }

    @Override
    public void setUsageCount(String voucherId, int count) {
        Preconditions.checkArgument(count >= 0, "Count must not be negative");
        if (count > 0) config.usageCount.put(voucherId, count);
        else config.usageCount.remove(voucherId);
        config.markDirty();
    }
}
