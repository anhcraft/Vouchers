package dev.anhcraft.vouchers.storage.player;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Exclude;
import dev.anhcraft.config.annotations.Optional;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.vouchers.Vouchers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Configurable
class PlayerDataConfig {
    @Exclude
    public AtomicBoolean dirty = new AtomicBoolean(false);

    public void markDirty() {
        dirty.set(true);
    }

    @PostHandler
    private void handle() {
        dirty = new AtomicBoolean(false); // sometimes this disappears
    }

    public int dataVersion = Vouchers.LATEST_PLAYER_DATA_VERSION;
    @Optional
    public Map<String, Long> lastUsed = new HashMap<>(3);
    @Optional
    public Map<String, Integer> usageLimitCount = new HashMap<>(3);
}
