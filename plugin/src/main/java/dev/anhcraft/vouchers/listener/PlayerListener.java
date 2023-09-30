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

import java.util.List;
import java.util.UUID;

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

        UUID exclusivePlayer = plugin.vouchersManager.identifyExclusivity(item);
        if (exclusivePlayer != null && !exclusivePlayer.equals(e.getPlayer().getUniqueId())) {
            plugin.msg(e.getPlayer(), plugin.messageConfig.exclusivityNotSatisfied);
            return;
        }

        if (!plugin.vouchersManager.preUse(e.getPlayer(), id, voucher)) {
            return;
        }

        VoucherRedeemEvent event = new VoucherRedeemEvent(e.getPlayer(), voucher, item);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.pluginLogger.scope("redeem")
                    .add("player", e.getPlayer())
                    .add("voucher", id)
                    .add("virtual", false)
                    .add("success", false)
                    .add("error", "cancelled")
                    .flush();
            return;
        }

        List<String> executedCommands = plugin.vouchersManager.onUse(e.getPlayer(), voucher);
        if (executedCommands.isEmpty() && plugin.mainConfig.preventNoRewards) {
            plugin.msg(e.getPlayer(), plugin.messageConfig.noRewardsGiven);
            plugin.pluginLogger.scope("redeem")
                    .add("player", e.getPlayer())
                    .add("voucher", id)
                    .add("virtual", false)
                    .add("success", false)
                    .add("error", "empty")
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
                .add("commands", executedCommands)
                .flush();
        plugin.vouchersManager.postUse(e.getPlayer(), id, voucher);
    }
}
