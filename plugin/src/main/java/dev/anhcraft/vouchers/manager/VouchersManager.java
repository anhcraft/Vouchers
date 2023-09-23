package dev.anhcraft.vouchers.manager;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.palette.util.ItemUtil;
import dev.anhcraft.vouchers.Vouchers;
import dev.anhcraft.vouchers.api.entity.Voucher;
import dev.anhcraft.vouchers.api.entity.VoucherBuilder;
import dev.anhcraft.vouchers.config.VoucherConfig;
import dev.anhcraft.vouchers.util.ConfigHelper;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VouchersManager {
    private static final Pattern CONDITION_TAG_PATTERN = Pattern.compile("\\[[a-z-]+=[A-Za-z0-9-_.]+]");
    private final Vouchers plugin;
    private final Map<String, Voucher> vouchers = new HashMap<>();
    private final NamespacedKey voucherIdentifier;

    public VouchersManager(Vouchers plugin) {
        this.plugin = plugin;
        voucherIdentifier = new NamespacedKey(plugin, "voucher");
    }

    public void reload(YamlConfiguration vouchersConfig) {
        vouchers.clear();
        for (String id : vouchersConfig.getKeys(false)) {
            VoucherConfig config = ConfigHelper.load(VoucherConfig.class, vouchersConfig.getConfigurationSection(id));
            VoucherBuilder voucherBuilder = new VoucherBuilder();
            voucherBuilder.name(config.name);
            voucherBuilder.description(config.description);
            if (config.customItem != null) {
                ItemBuilder itemBuilder = config.customItem;
                if (itemBuilder.material() == Material.AIR) {
                    itemBuilder.material(config.icon);
                }
                if (itemBuilder.name() == null || itemBuilder.name().isEmpty()) {
                    itemBuilder.name(config.name);
                }
                if (itemBuilder.lore().isEmpty()) {
                    itemBuilder.lore(Arrays.asList(config.description));
                    itemBuilder.lore().addAll(Arrays.asList(plugin.mainConfig.defaultVoucherFooter));
                }
                itemBuilder.amount(1); // amount must always be 1
                voucherBuilder.customItem(itemBuilder.build());
            }
            voucherBuilder.rewards(config.rewards);
            vouchers.put(id, voucherBuilder.build());
        }
        plugin.getLogger().info("Loaded " + vouchers.size() + " vouchers");
    }

    public Map<String, Voucher> getVouchers() {
        return vouchers;
    }

    public boolean onUse(Player player, Voucher voucher) {
        boolean executed = false;

        outer:
        for (String reward : voucher.getRewards()) {
            reward = PlaceholderAPI.setPlaceholders(player, reward);

            boolean runAsPlayer = false;
            double chance = 0.0;
            int delay = 0;
            String permission = null;

            int endOfMatcher = 0;

            Matcher m = CONDITION_TAG_PATTERN.matcher(reward);
            while (m.find()) {
                endOfMatcher = Math.max(endOfMatcher, m.end());
                String tag = m.group();
                tag = tag.substring(1, tag.length() - 1);
                String[] args = tag.split("=");
                switch (args[0]) {
                    case "player":
                        runAsPlayer = args.length == 1 || args[1].equalsIgnoreCase("true");
                        break;
                    case "chance":
                        if (args.length == 1) {
                            throwError(player, voucher.getName(), reward, tag);
                            continue outer;
                        }
                        try {
                            chance = Double.parseDouble(args[1]);
                            if (chance < 0 || chance > 1) {
                                throwError(player, voucher.getName(), reward, tag);
                                continue outer;
                            }
                        } catch (NumberFormatException e) {
                            throwError(player, voucher.getName(), reward, tag);
                            continue outer;
                        }
                        break;
                    case "delay":
                        if (args.length == 1) {
                            throwError(player, voucher.getName(), reward, tag);
                            continue outer;
                        }
                        try {
                            delay = Integer.parseInt(args[1]);
                            if (delay < 0) {
                                throwError(player, voucher.getName(), reward, tag);
                                continue outer;
                            }
                        } catch (NumberFormatException e) {
                            throwError(player, voucher.getName(), reward, tag);
                            continue outer;
                        }
                        break;
                    case "permission":
                        if (args.length == 1) {
                            throwError(player, voucher.getName(), reward, tag);
                            continue outer;
                        }
                        permission = args[1];
                        break;
                }
            }

            String cmd = reward.substring(endOfMatcher).trim();
            if (cmd.isEmpty()) continue;
            if (permission != null && !player.hasPermission(permission)) continue;
            if (ThreadLocalRandom.current().nextDouble() > chance) continue;

            CommandSender sender = runAsPlayer ? player : Bukkit.getConsoleSender();

            if (delay == 0) {
                Bukkit.dispatchCommand(sender, cmd);
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(sender, cmd), delay);
            }

            executed = true;
        }

        return executed;
    }

    private void throwError(Player player, String voucher, String reward, String tag) {
        plugin.getLogger().warning(String.format(
                "Invalid tag '%s' in reward '%s' while '%s' is trying to claim '%s'",
                tag, reward, player.getName(), voucher
        ));
    }

    public void postUse(Player player, Voucher voucher) {
        for (String str : plugin.messageConfig.defaultUseMessage) {
            plugin.rawMsg(player, str.replace("{voucher-name}", voucher.getName()));
        }
        player.playSound(player.getLocation(), plugin.mainConfig.defaultUseSound, 1.0f, 1.0f);
    }

    public ItemStack buildVoucher(String id, Voucher voucher) {
        ItemStack item = voucher.getCustomItem();
        if (item == null) {
            ItemBuilder itemBuilder = new ItemBuilder();
            itemBuilder.name(voucher.getName());
            itemBuilder.lore(Arrays.asList(voucher.getDescription()));
            itemBuilder.lore().addAll(Arrays.asList(plugin.mainConfig.defaultVoucherFooter));
            itemBuilder.flag(ItemFlag.HIDE_ATTRIBUTES);
            itemBuilder.flag(ItemFlag.HIDE_POTION_EFFECTS);
            item = itemBuilder.build();
        }
        if (item.getType() == Material.AIR)
            item.setType(plugin.mainConfig.defaultVoucherIcon); // set default material if unspecified
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(voucherIdentifier, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    public String scanVoucher(ItemStack item) {
        if (ItemUtil.isEmpty(item))
            return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return null;
        return meta.getPersistentDataContainer().get(voucherIdentifier, PersistentDataType.STRING);
    }
}
