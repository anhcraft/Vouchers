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
    /**
     * Gets all voucher ids.
     * @return a set of voucher ids
     */
    @NotNull
    Set<String> getVoucherIds();

    /**
     * Gets all vouchers available.
     * @return a list of vouchers
     */
    @NotNull
    List<Voucher> getVouchers();

    /**
     * Registers a new voucher.
     * @param id the id
     * @param voucher the voucher
     * @return {@code true} if success, or {@code false} if failed (e.g: duplication)
     */
    boolean registerVoucher(@NotNull String id, @NotNull Voucher voucher);

    /**
     * Gets the voucher given its id.
     * @param id the id
     * @return the voucher or {@code null} if not found
     */
    @Nullable
    Voucher getVoucher(@Nullable String id);

    /**
     * Builds a voucher item given its id.
     * @param id the id
     * @return the item
     */
    @NotNull
    ItemStack buildVoucher(@NotNull String id);

    /**
     * Gets the voucher id from the given item.<br>
     * If the item is a voucher, an ID is <b>always</b> returned no matter if that voucher exists or not. To know if
     * that voucher exists, use {@link #getVoucher(String)} to check.
     * @param item the item
     * @return the voucher id or {@code null} if the item is not a voucher
     */
    @Nullable
    String scanVoucher(@Nullable ItemStack item);

    /**
     * Sets the "exclusive" status for the given voucher item.
     * @param item the item (non-voucher items allowed)
     * @param player the player who is the only one can redeem this voucher
     *               (or {@code null} to remove the "exclusive" status)
     * @return the voucher or the original item if it is not a voucher
     */
    @Nullable
    ItemStack setExclusive(@Nullable ItemStack item, @Nullable UUID player);

    /**
     * Unsets the "exclusive" status for the given voucher item.<br>
     * This is the same as calling {@link #setExclusive(ItemStack, UUID)} with owner is {@code null}.
     * @param item the item (non-voucher items allowed)
     * @return the voucher or the original item if it is not a voucher
     */
    @Nullable
    default ItemStack unsetExclusive(@Nullable ItemStack item) {
        return setExclusive(item, null);
    }

    /**
     * Gets the player who this voucher is exclusive for.
     * @param item the item (non-voucher items allowed)
     * @return the player; otherwise, {@code null} if the item is not a voucher or no player was defined
     */
    @Nullable
    UUID getExclusivePlayer(@Nullable ItemStack item);

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
