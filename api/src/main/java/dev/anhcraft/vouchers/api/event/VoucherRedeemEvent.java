package dev.anhcraft.vouchers.api.event;

import dev.anhcraft.vouchers.api.entity.Voucher;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VoucherRedeemEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Voucher voucher;
    private final ItemStack itemStack;
    private boolean cancelled;

    public VoucherRedeemEvent(@NotNull Player who, Voucher voucher, ItemStack itemStack) {
        super(who);
        this.voucher = voucher;
        this.itemStack = itemStack;
    }

    @NotNull
    public Voucher getVoucher() {
        return voucher;
    }

    /**
     * Gets the item stack involved in this redemption.
     * @return the item stack or {@code null} if the voucher used is virtual
     */
    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
