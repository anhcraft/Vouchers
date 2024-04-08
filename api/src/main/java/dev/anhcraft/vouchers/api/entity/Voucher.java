package dev.anhcraft.vouchers.api.entity;

import dev.anhcraft.vouchers.api.util.GroupSettings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class Voucher {
    private final Material icon;
    private final String name;
    private final String[] description;
    private final String[] rewards;
    private final ItemStack customItem;
    private final GroupSettings usageLimit;
    private final GroupSettings cooldown;
    private final String condition;
    private final boolean doubleCheck;
    private final boolean physicalId;

    public Voucher(@NotNull Material icon,
                   @NotNull String name,
                   @NotNull String[] description,
                   @NotNull String[] rewards,
                   @Nullable ItemStack customItem,
                   @NotNull GroupSettings usageLimit, @NotNull GroupSettings cooldown,
                   @Nullable String condition, boolean doubleCheck) {
        this(icon, name, description, rewards, customItem, usageLimit, cooldown, condition, doubleCheck, false);
    }

    public Voucher(@NotNull Material icon,
                   @NotNull String name,
                   @NotNull String[] description,
                   @NotNull String[] rewards,
                   @Nullable ItemStack customItem,
                   @NotNull GroupSettings usageLimit, @NotNull GroupSettings cooldown,
                   @Nullable String condition, boolean doubleCheck, boolean physicalId) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.rewards = rewards;
        this.customItem = customItem;
        this.usageLimit = usageLimit;
        this.cooldown = cooldown;
        this.condition = condition;
        this.doubleCheck = doubleCheck;
        this.physicalId = physicalId;
    }

    @NotNull
    public Material getIcon() {
        return icon;
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

    @NotNull
    public GroupSettings getUsageLimit() {
        return usageLimit;
    }

    @NotNull
    public GroupSettings getCooldown() {
        return cooldown;
    }

    @Nullable
    public String getCondition() {
        return condition;
    }

    public boolean shouldDoubleCheck() {
        return doubleCheck;
    }

    public boolean hasPhysicalId() {
        return physicalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Voucher)) return false;
        Voucher voucher = (Voucher) o;
        return icon == voucher.icon &&
                Objects.equals(name, voucher.name) &&
                Arrays.equals(description, voucher.description) &&
                Arrays.equals(rewards, voucher.rewards) &&
                Objects.equals(customItem, voucher.customItem) &&
                Objects.equals(usageLimit, voucher.usageLimit) &&
                Objects.equals(cooldown, voucher.cooldown) &&
                Objects.equals(condition, voucher.condition) &&
                Objects.equals(doubleCheck, voucher.doubleCheck);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(icon, name, customItem, usageLimit, cooldown, condition, doubleCheck);
        result = 31 * result + Arrays.hashCode(description);
        result = 31 * result + Arrays.hashCode(rewards);
        return result;
    }
}
