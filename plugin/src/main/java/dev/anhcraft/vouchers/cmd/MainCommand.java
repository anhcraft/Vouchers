package dev.anhcraft.vouchers.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.anhcraft.config.bukkit.utils.ColorUtil;
import dev.anhcraft.palette.util.ItemUtil;
import dev.anhcraft.vouchers.Vouchers;
import dev.anhcraft.vouchers.api.entity.Voucher;
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

    @Subcommand("reload")
    @CommandPermission("vouchers.reload")
    public void reload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(GREEN + "Reloaded the plugin!");
    }
}
