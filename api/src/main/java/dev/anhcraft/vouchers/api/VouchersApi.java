package dev.anhcraft.vouchers.api;

import dev.anhcraft.vouchers.api.data.PlayerData;
import dev.anhcraft.vouchers.api.data.ServerData;
import dev.anhcraft.vouchers.api.entity.Voucher;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface VouchersApi {
    @NotNull
    Set<String> getVoucherIds();

    @NotNull
    List<Voucher> getVouchers();

    boolean registerVoucher(@NotNull String id, @NotNull Voucher voucher);

    @Nullable
    Voucher getVoucher(@Nullable String id);

    @NotNull
    ItemStack buildVoucher(@NotNull String id);

    @Nullable
    String scanVoucher(@Nullable ItemStack voucher);

    /**
     * Gets player data of an online player.<br>
     * For any online player, the data always exists during their session.
     * @param player The player
     * @return The player data
     */
    @NotNull
    PlayerData getPlayerData(@NotNull Player player);

    /**
     * Gets player data of a player from the cache without fetching or blocking.<br>
     * If the player is online, the data always exists. If the player is offline and his data was recently fetched,
     * then this returns immediately. Otherwise, this returns {@code null}.<br>
     * To attempt to fetch the data of any player, use {@link #requirePlayerData(UUID)}.
     * @param id The player ID
     * @return The player data
     */
    @NotNull
    Optional<PlayerData> getPlayerData(@NotNull UUID id);

    /**
     * Requires the player data from the cache or fetches if not exists yet.<br>
     * This returns a {@link CompletableFuture} which will be done asynchronously. For online player and offline player
     * whose data exists in cache, the result is immediately returned. Otherwise, it will take some time.
     * @param id The player ID
     * @return The player data
     */
    @NotNull
    CompletableFuture<PlayerData> requirePlayerData(@NotNull UUID id);

    /**
     * Gets the server data.
     * @return The server data
     */
    @NotNull
    ServerData getServerData();
}
