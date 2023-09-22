package dev.anhcraft.vouchers;

import dev.anhcraft.vouchers.api.entity.Voucher;
import dev.anhcraft.vouchers.api.VouchersApi;
import dev.anhcraft.vouchers.api.data.PlayerData;
import dev.anhcraft.vouchers.api.data.ServerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class VouchersApiImpl implements VouchersApi {
    private final Vouchers plugin;

    public VouchersApiImpl(Vouchers plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Set<String> getVoucherIds() {
        return Collections.unmodifiableSet(plugin.vouchersManager.getVouchers().keySet());
    }

    @Override
    public @NotNull List<Voucher> getVouchers() {
        return new ArrayList<>(plugin.vouchersManager.getVouchers().values());
    }

    @Override
    public boolean registerVoucher(@NotNull String id, @NotNull Voucher voucher) {
        return plugin.vouchersManager.getVouchers().putIfAbsent(id, voucher) == null;
    }

    @Override
    public @Nullable Voucher getVoucher(String id) {
        return plugin.vouchersManager.getVouchers().get(id);
    }

    @Override
    public @NotNull ItemStack buildVoucher(@NotNull String id) {
        return plugin.vouchersManager.buildVoucher(id, Objects.requireNonNull(getVoucher(id), "Voucher not found: " + id));
    }

    @Override
    public @Nullable String scanVoucher(@Nullable ItemStack voucher) {
        return plugin.vouchersManager.scanVoucher(voucher);
    }

    @Override
    public @NotNull PlayerData getPlayerData(@NotNull Player player) {
        return plugin.playerDataManager.getData(player);
    }

    @Override
    public @NotNull Optional<PlayerData> getPlayerData(@NotNull UUID id) {
        return plugin.playerDataManager.getData(id);
    }

    @Override
    public @NotNull CompletableFuture<PlayerData> requirePlayerData(@NotNull UUID id) {
        return plugin.playerDataManager.requireData(id);
    }

    @Override
    public @NotNull ServerData getServerData() {
        return plugin.serverDataManager.getData();
    }
}
