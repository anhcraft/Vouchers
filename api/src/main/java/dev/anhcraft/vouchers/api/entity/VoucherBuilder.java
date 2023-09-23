package dev.anhcraft.vouchers.api.entity;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoucherBuilder {
    private Material icon;
    private String name;
    private List<String> description = new ArrayList<>(3);
    private List<String> rewards = new ArrayList<>(3);
    private ItemStack customItem;

    public VoucherBuilder icon(@NotNull Material icon) {
        this.icon = icon;
        return this;
    }

    public VoucherBuilder name(@NotNull String name) {
        this.name = name;
        return this;
    }

    public VoucherBuilder description(@NotNull String... description) {
        this.description.addAll(Arrays.asList(description));
        return this;
    }

    public VoucherBuilder rewards(@NotNull String... rewards) {
        this.rewards.addAll(Arrays.asList(rewards));
        return this;
    }

    public VoucherBuilder customItem(@Nullable ItemStack customItem) {
        this.customItem = customItem;
        return this;
    }

    public Voucher build() {
        return new Voucher(icon, name, description.toArray(new String[0]), rewards.toArray(new String[0]), customItem);
    }
}