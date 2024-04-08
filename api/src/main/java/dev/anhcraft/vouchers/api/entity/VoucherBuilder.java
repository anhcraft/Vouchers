package dev.anhcraft.vouchers.api.entity;

import com.google.common.base.Preconditions;
import dev.anhcraft.vouchers.api.util.GroupSettings;
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
    private GroupSettings cooldown = GroupSettings.empty(GroupSettings.COOLDOWN_PERM);
    private GroupSettings usageLimit = GroupSettings.empty(GroupSettings.USAGE_LIMIT_PERM);
    private String condition;
    private boolean doubleCheck;
    private boolean physicalId;

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

    public VoucherBuilder cooldown(@NotNull GroupSettings cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    public VoucherBuilder usageLimit(@NotNull GroupSettings usageLimit) {
        this.usageLimit = usageLimit;
        return this;
    }

    public VoucherBuilder condition(@Nullable String condition) {
        this.condition = condition;
        return this;
    }

    public VoucherBuilder doubleCheck(boolean doubleCheck) {
        this.doubleCheck = doubleCheck;
        return this;
    }

    public VoucherBuilder physicalId(boolean physicalId) {
        this.physicalId = physicalId;
        return this;
    }

    public Voucher build() {
        Preconditions.checkNotNull(icon, "Icon must not be null");
        Preconditions.checkNotNull(name, "Name must not be null");
        Preconditions.checkNotNull(description, "Description must not be null");
        return new Voucher(icon, name, description.toArray(new String[0]), rewards.toArray(new String[0]), customItem, usageLimit, cooldown, condition, doubleCheck, physicalId);
    }
}