package dev.anhcraft.vouchers.api.entity;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class Voucher {
    private final String name;
    private final String[] description;
    private final String[] rewards;
    private final ItemStack customItem;

    public Voucher(@NotNull String name, @NotNull String[] description, @NotNull String[] rewards, @Nullable ItemStack customItem) {
        this.name = name;
        this.description = description;
        this.rewards = rewards;
        this.customItem = customItem;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String[] getDescription() {
        return description;
    }

    @NotNull
    public String[] getRewards() {
        return rewards;
    }

    @Nullable
    public ItemStack getCustomItem() {
        return customItem == null ? null : customItem.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Voucher voucher = (Voucher) o;
        return Objects.equals(name, voucher.name) &&
                Arrays.equals(description, voucher.description) &&
                Arrays.equals(rewards, voucher.rewards) &&
                Objects.equals(customItem, voucher.customItem);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, customItem);
        result = 31 * result + Arrays.hashCode(description);
        result = 31 * result + Arrays.hashCode(rewards);
        return result;
    }
}
