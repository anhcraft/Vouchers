package dev.anhcraft.vouchers;

import dev.anhcraft.vouchers.api.VouchersApi;
import dev.anhcraft.vouchers.api.data.PlayerData;
import dev.anhcraft.vouchers.api.data.ServerData;
import dev.anhcraft.vouchers.api.entity.Voucher;
import dev.anhcraft.vouchers.api.entity.VoucherCode;
import dev.anhcraft.vouchers.storage.server.ServerDataConfig;
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
    public ItemStack setExclusive(@Nullable ItemStack item, @Nullable UUID player) {
        return plugin.vouchersManager.changeExclusivity(item, player);
    }

    @Override
    public @Nullable UUID getExclusivePlayer(@Nullable ItemStack item) {
        return plugin.vouchersManager.identifyExclusivity(item);
    }

    @Override
    public @Nullable VoucherCode getVoucherCode(@Nullable String code) {
        return toVoucherCode(code, plugin.serverDataManager.getData().getVoucherCodes().get(code));
    }

    @Override
    public @NotNull List<VoucherCode> getVoucherCodesByVoucher(@NotNull String id) {
        List<VoucherCode> codes = new ArrayList<>();
        for (Map.Entry<String, ServerDataConfig.VoucherCodeDataConfig> cfg : plugin.serverDataManager.getData().getVoucherCodes().entrySet()) {
            if (!cfg.getValue().voucher.equals(id))
                continue;
            VoucherCode voucherCode = toVoucherCode(cfg.getKey(), cfg.getValue());
            if (voucherCode != null)
                codes.add(voucherCode);
        }
        return Collections.unmodifiableList(codes);
    }

    @Override
    public @NotNull List<VoucherCode> getVoucherCodesByIssuer(@NotNull UUID issuer) {
        List<VoucherCode> codes = new ArrayList<>();
        for (Map.Entry<String, ServerDataConfig.VoucherCodeDataConfig> cfg : plugin.serverDataManager.getData().getVoucherCodes().entrySet()) {
            if (!cfg.getValue().issuer.equals(issuer))
                continue;
            VoucherCode voucherCode = toVoucherCode(cfg.getKey(), cfg.getValue());
            if (voucherCode != null)
                codes.add(voucherCode);
        }
        return Collections.unmodifiableList(codes);
    }

    @Override
    public @NotNull List<VoucherCode> getVoucherCodesByUser(@NotNull UUID user) {
        List<VoucherCode> codes = new ArrayList<>();
        for (Map.Entry<String, ServerDataConfig.VoucherCodeDataConfig> cfg : plugin.serverDataManager.getData().getVoucherCodes().entrySet()) {
            if (cfg.getValue().user == null || !cfg.getValue().user.equals(user))
                continue;
            VoucherCode voucherCode = toVoucherCode(cfg.getKey(), cfg.getValue());
            if (voucherCode != null)
                codes.add(voucherCode);
        }
        return Collections.unmodifiableList(codes);
    }

    @Override
    public @NotNull VoucherCode generateVoucherCode(@NotNull Voucher voucher, @NotNull UUID issuer, boolean pseudo) {
        return null;
    }

    @Override
    public boolean publishVoucherCode(@NotNull VoucherCode code) {
        return false;
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

    // ---- INTERNAL

    private @Nullable VoucherCode toVoucherCode(@Nullable String code, @Nullable ServerDataConfig.VoucherCodeDataConfig cfg) {
        if (code == null || cfg == null) {
            return null;
        }
        Voucher voucher = getVoucher(cfg.voucher);
        if (voucher == null) {
            return null;
        }
        return new VoucherCode(
                code,
                voucher,
                cfg.issuer,
                cfg.user,
                new Date(cfg.issueDate),
                cfg.redeemDate == null ? null : new Date(cfg.redeemDate)
        );
    }
}
