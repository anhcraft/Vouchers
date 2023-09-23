package dev.anhcraft.vouchers.listener;

import dev.anhcraft.palette.util.ItemUtil;
import dev.anhcraft.vouchers.Vouchers;
import dev.anhcraft.vouchers.api.entity.Voucher;
import dev.anhcraft.vouchers.api.event.VoucherRedeemEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final Vouchers plugin;

    public PlayerListener(Vouchers plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void interact(PlayerInteractEvent e) {
        if ((e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) || e.getHand() != EquipmentSlot.HAND) return;
        if (e.useItemInHand() == Event.Result.DENY && plugin.mainConfig.ignoreDeniedInteract) return;

        ItemStack item = e.getItem();
        if (ItemUtil.isEmpty(item)) return;
        String id = plugin.vouchersManager.scanVoucher(item);
        if (id == null) return;

        Voucher voucher = plugin.vouchersManager.getVouchers().get(id);
        if (voucher == null) {
            plugin.msg(e.getPlayer(), plugin.messageConfig.invalidVoucher);
            return;
        }
        e.setCancelled(true); // always cancel

        VoucherRedeemEvent event = new VoucherRedeemEvent(e.getPlayer(), voucher, item);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        boolean ok = plugin.vouchersManager.onUse(e.getPlayer(), voucher);
        if (!ok && plugin.mainConfig.preventNoRewards) {
            plugin.msg(e.getPlayer(), plugin.messageConfig.noRewardsGiven);
            plugin.pluginLogger.scope("redeem")
                    .add("player", e.getPlayer())
                    .add("voucher", id)
                    .add("virtual", false)
                    .add("success", false)
                    .flush();
            return;
        }
        if (item.getAmount() == 1)
            e.getPlayer().getInventory().setItemInMainHand(null);
        else {
            item.setAmount(item.getAmount() - 1);
            e.getPlayer().getInventory().setItemInMainHand(item);
        }
        plugin.pluginLogger.scope("redeem")
                .add("player", e.getPlayer())
                .add("voucher", id)
                .add("virtual", false)
                .add("success", true)
                .flush();
        plugin.vouchersManager.postUse(e.getPlayer(), voucher);
    }
}
