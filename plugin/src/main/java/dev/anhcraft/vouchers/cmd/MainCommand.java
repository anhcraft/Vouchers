package dev.anhcraft.vouchers.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.anhcraft.config.bukkit.utils.ColorUtil;
import dev.anhcraft.palette.util.ItemUtil;
import dev.anhcraft.vouchers.Vouchers;
import dev.anhcraft.vouchers.api.entity.Voucher;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.ChatColor.*;

@CommandAlias("voucher|vouchers")
public class MainCommand extends BaseCommand {
    private final Vouchers plugin;

    public MainCommand(Vouchers plugin) {
        this.plugin = plugin;
    }

    @HelpCommand
    @CatchUnknown
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("list")
    @CommandPermission("vouchers.list")
    public void list(CommandSender sender) {
        sender.sendMessage(GREEN + "All vouchers: " + WHITE + String.join(", ", plugin.vouchersManager.getVouchers().keySet()));
    }

    @Subcommand("give")
    @CommandPermission("vouchers.give")
    @CommandCompletion("@players @vouchers")
    public void give(CommandSender sender, OnlinePlayer op, String voucherId, @Default("1") int amount) {
        if (amount < 0) {
            sender.sendMessage(RED + "Amount must not be negative");
            return;
        }
        if (amount == 0) {
            sender.sendMessage(YELLOW + "Given none of " + voucherId + " to " + op.player.getName());
            return;
        }
        Voucher voucher = plugin.vouchersManager.getVouchers().get(voucherId);
        if (voucher == null) {
            sender.sendMessage(RED + "Voucher not found: " + voucherId);
            return;
        }
        ItemStack itemStack = plugin.vouchersManager.buildVoucher(voucherId, voucher);
        itemStack.setAmount(amount);
        ItemUtil.addToInventory(op.player, itemStack);
        sender.sendMessage(GREEN + "Given " + amount + " of " + ColorUtil.colorize(voucher.getName()) + GREEN + " to " + op.player.getName());
    }

    @Subcommand("reset cooldown")
    @CommandPermission("vouchers.reset.cooldown")
    @CommandCompletion("@players @vouchers")
    public void resetCooldown(CommandSender sender, OfflinePlayer reference, String voucherId) {
        if (!reference.hasPlayedBefore()) {
            sender.sendMessage(RED + "This player has not played before!");
            return;
        }
        if (!reference.isOnline())
            sender.sendMessage(YELLOW + "Fetching player data as he is currently offline...");

        Vouchers.getApi().requirePlayerData(reference.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(RED + throwable.getMessage());
                return;
            }
            playerData.setLastUsed(voucherId, 0);
            sender.sendMessage(GREEN + "Cooldown reset");
        });
    }

    @Subcommand("reset limit")
    @CommandPermission("vouchers.reset.limit")
    @CommandCompletion("@players @vouchers")
    public void resetUsageLimit(CommandSender sender, String ref, String voucherId) {
        if (ref.equals("*")) {
            Vouchers.getApi().getServerData().setUsageLimitCount(voucherId, 0);
            sender.sendMessage(GREEN + "Usage limit reset");
            return;
        }

        var reference = Bukkit.getOfflinePlayer(ref);
        if (!reference.hasPlayedBefore()) {
            sender.sendMessage(RED + "This player has not played before!");
            return;
        }
        if (!reference.isOnline())
            sender.sendMessage(YELLOW + "Fetching player data as he is currently offline...");

        Vouchers.getApi().requirePlayerData(reference.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(RED + throwable.getMessage());
                return;
            }
            playerData.setUsageLimitCount(voucherId, 0);
            sender.sendMessage(GREEN + "Usage limit reset");
        });
    }

    @Subcommand("reload")
    @CommandPermission("vouchers.reload")
    public void reload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(GREEN + "Reloaded the plugin!");
    }
}
