package dev.anhcraft.vouchers.api.entity;

import com.google.common.base.Preconditions;
import dev.anhcraft.vouchers.api.util.GroupSettings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VoucherBuilder {
    Material icon;
    String name;
    String[] description;
    String[] rewards;
    ItemStack customItem;
    GroupSettings cooldown = GroupSettings.EMPTY_COOLDOWN;
    GroupSettings usageLimit = GroupSettings.EMPTY_USAGE_LIMIT;
    String condition;
    boolean doubleCheck;
    boolean physicalId;
    String[] useMessage;

    public VoucherBuilder icon(@NotNull Material icon) {
        this.icon = icon;
        return this;
    }

    public VoucherBuilder name(@NotNull String name) {
        this.name = name;
        return this;
    }

    public VoucherBuilder description(@Nullable String... description) {
        if (description != null)
            this.description = description.clone();
        return this;
    }

    public VoucherBuilder rewards(@Nullable String... rewards) {
        if (rewards != null)
            this.rewards = rewards.clone();
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

    public VoucherBuilder useMessage(@Nullable String... useMessage) {
        if (useMessage != null)
            this.useMessage = useMessage.clone();
        return this;
    }

    public Voucher build() {
        Preconditions.checkNotNull(icon, "Icon must not be null");
        Preconditions.checkNotNull(name, "Name must not be null");

        return new Voucher(this);
    }
}