package dev.anhcraft.vouchers.api.event;

import dev.anhcraft.vouchers.api.data.PlayerData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called when {@link PlayerData} is loaded.
 */
@ApiStatus.Experimental
public class AsyncPlayerDataLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final UUID uuid;
    private final PlayerData data;

    public AsyncPlayerDataLoadEvent(@NotNull UUID uuid, @NotNull PlayerData data) {
        super(true);
        this.uuid = uuid;
        this.data = data;
    }

    @NotNull
    public UUID getPlayerId() {
        return uuid;
    }

    @NotNull
    public PlayerData getData() {
        return data;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
