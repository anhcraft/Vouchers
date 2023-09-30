package dev.anhcraft.vouchers.manager;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import com.jeff_media.morepersistentdatatypes.DataType;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.jvmkit.utils.ObjectUtil;
import dev.anhcraft.palette.util.ItemUtil;
import dev.anhcraft.vouchers.Vouchers;
import dev.anhcraft.vouchers.api.data.PlayerData;
import dev.anhcraft.vouchers.api.data.ServerData;
import dev.anhcraft.vouchers.api.entity.Voucher;
import dev.anhcraft.vouchers.api.entity.VoucherBuilder;
import dev.anhcraft.vouchers.api.util.GroupSettings;
import dev.anhcraft.vouchers.config.VoucherConfig;
import dev.anhcraft.vouchers.util.ConfigHelper;
import dev.anhcraft.vouchers.util.TimeUtils;
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

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VouchersManager {
    private static final Pattern CONDITION_TAG_PATTERN = Pattern.compile("\\[[a-z-]+=[A-Za-z0-9-_.]+]");
    private final Vouchers plugin;
    private final Map<String, Voucher> vouchers = new HashMap<>();
    private final NamespacedKey voucherIdentifier;
    private final NamespacedKey exclusivePlayerIdentifier;

    public VouchersManager(Vouchers plugin) {
        this.plugin = plugin;
        voucherIdentifier = new NamespacedKey(plugin, "voucher");
        exclusivePlayerIdentifier = new NamespacedKey(plugin, "exclusive-player");
    }

    public void reload(YamlConfiguration vouchersConfig) {
        vouchers.clear();
        for (String id : vouchersConfig.getKeys(false)) {
            VoucherConfig config = ConfigHelper.load(VoucherConfig.class, vouchersConfig.getConfigurationSection(id));
            VoucherBuilder voucherBuilder = new VoucherBuilder();
            voucherBuilder.icon(ObjectUtil.optional(config.icon, plugin.mainConfig.defaultVoucherIcon));
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
            if (config.cooldown != null) {
                voucherBuilder.cooldown(GroupSettings.of(GroupSettings.COOLDOWN_PERM, config.cooldown, false));
            }
            if (config.usageLimit != null) {
                voucherBuilder.usageLimit(GroupSettings.of(GroupSettings.USAGE_LIMIT_PERM, config.usageLimit, true));
            }
            voucherBuilder.condition(config.condition);
            vouchers.put(id, voucherBuilder.build());
        }
        plugin.getLogger().info("Loaded " + vouchers.size() + " vouchers");
    }

    public Map<String, Voucher> getVouchers() {
        return vouchers;
    }

    public boolean preUse(Player player, String id, Voucher voucher) {
        plugin.debug(2, "Checking '%s' prerequisite for '%s'", id, player.getName());

        var globalUsage = Vouchers.getApi().getServerData().getUsageLimitCount(id);
        var globalUsageLimit = voucher.getUsageLimit().getGlobal();
        plugin.debug(2, "- Global usage limit: %d/%d", globalUsage, globalUsageLimit);
        if (globalUsageLimit > 0 && globalUsage >= globalUsageLimit) {
            plugin.msg(player, plugin.messageConfig.globalUsageLimit.replace("{max}", String.valueOf(globalUsageLimit)));
            return false;
        }

        var playerData = Vouchers.getApi().getPlayerData(player);
        var playerUsage = playerData.getUsageLimitCount(id);
        var playerUsageLimit = voucher.getUsageLimit().evaluate(player);
        plugin.debug(2, "- Player usage limit: %d/%d", playerUsage, playerUsageLimit);
        if (playerUsageLimit > 0 &&playerUsage >= playerUsageLimit) {
            plugin.msg(player, plugin.messageConfig.playerUsageLimit.replace("{max}", String.valueOf(playerUsageLimit)));
            return false;
        }

        var nextCooldown = playerData.getLastUsed(id) + voucher.getCooldown().evaluate(player) * 1000L;
        var remainTime = Math.max(0, (nextCooldown - System.currentTimeMillis()) / 1000);
        plugin.debug(2, "- Cooldown remain: %d", remainTime);
        if (remainTime > 0) {
            plugin.msg(player, plugin.messageConfig.inCooldown.replace("{time}", TimeUtils.format(remainTime)));
            return false;
        }

        var condition = voucher.getCondition();
        if (condition != null) {
            plugin.debug(2, "- Before PlaceholderAPI-applied condition: %s", condition);
            condition = PlaceholderAPI.setPlaceholders(player, condition);
            plugin.debug(2, "- After PlaceholderAPI-applied condition: %s", condition);
            try {
                // Currently EvalEx does not support single quotes so this is a weird workaround
                var expression = new Expression(condition.replace("'", "\"")).evaluate();
                if (!expression.isBooleanValue() || !expression.getBooleanValue()) {
                    plugin.msg(player, plugin.messageConfig.conditionNotSatisfied);
                    return false;
                }
            } catch (EvaluationException | ParseException e) {
                plugin.debug(2, "- Failed to evaluate condition: %s", condition);
                plugin.msg(player, plugin.messageConfig.conditionNotSatisfied);
                return false;
            }
        }

        return true;
    }

    public List<String> onUse(Player player, Voucher voucher) {
        List<String> executedCommands = new ArrayList<>();

        plugin.debug("Executing %d rewards from '%s' for '%s'", voucher.getRewards().length, voucher.getName(), player.getName());

        outer:
        for (String reward : voucher.getRewards()) {
            plugin.debug(2, "- Before PlaceholderAPI-applied: %s", reward);
            reward = PlaceholderAPI.setPlaceholders(player, reward);
            plugin.debug(2, "- After PlaceholderAPI-applied: %s", reward);

            boolean runAsPlayer = false;
            double chance = 0.0;
            int delay = 0;
            String permission = null;

            int endOfMatcher = 0;

            Matcher m = CONDITION_TAG_PATTERN.matcher(reward);
            while (m.find()) {
                endOfMatcher = Math.max(endOfMatcher, m.end());
                String tag = m.group();
                String[] args = tag.substring(1, tag.length() - 1).split("=");
                switch (args[0]) {
                    case "player":
                        plugin.debug(2, "- Run as player: %s", tag);
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
                            plugin.debug(2, "- Chance=%f: %s", chance, tag);
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
                            plugin.debug(2, "- Delay=%ds: %s", delay, tag);
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
                        plugin.debug(2, "- Permission=%s: %s", args[1], tag);
                        break;
                }
            }

            String cmd = reward.substring(endOfMatcher).trim();
            plugin.debug(2, "- Command: %s", cmd);

            if (cmd.isEmpty()) {
                plugin.debug(2, "=> FAILED: Skipped due to empty command given");
                continue;
            }
            if (permission != null && !player.hasPermission(permission)) {
                plugin.debug(2, "=> FAILED: Skipped due to no permission");
                continue;
            }
            if (chance > 0 && ThreadLocalRandom.current().nextDouble() > chance) {
                plugin.debug(2, "=> FAILED: Skipped due to unlucky");
                continue;
            }
            plugin.debug(2, "=> SUCCESS");

            CommandSender sender = runAsPlayer ? player : Bukkit.getConsoleSender();

            if (delay == 0) {
                Bukkit.dispatchCommand(sender, cmd);
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(sender, cmd), delay*20L);
            }

            executedCommands.add(cmd);
        }

        return executedCommands;
    }

    private void throwError(Player player, String voucher, String reward, String tag) {
        plugin.getLogger().warning(String.format(
                "Invalid tag '%s' in reward '%s' while '%s' is trying to claim '%s'",
                tag, reward, player.getName(), voucher
        ));
    }

    public void postUse(Player player, String id, Voucher voucher) {
        for (String str : plugin.messageConfig.defaultUseMessage) {
            plugin.rawMsg(player, str.replace("{voucher-name}", voucher.getName()));
        }
        player.playSound(player.getLocation(), plugin.mainConfig.defaultUseSound, 1.0f, 1.0f);
        PlayerData pd = Vouchers.getApi().getPlayerData(player);
        pd.setLastUsed(id, System.currentTimeMillis());
        pd.increaseUsageLimitCount(id);
        ServerData sd = Vouchers.getApi().getServerData();
        sd.increaseUsageCount(id);
        sd.increaseUsageLimitCount(id);
    }

    public ItemStack buildVoucher(String id, Voucher voucher) {
        ItemStack item = voucher.getCustomItem();
        if (item == null) {
            ItemBuilder itemBuilder = new ItemBuilder();
            itemBuilder.material(voucher.getIcon());
            itemBuilder.name(voucher.getName());
            itemBuilder.lore(Arrays.asList(voucher.getDescription()));
            itemBuilder.lore().addAll(Arrays.asList(plugin.mainConfig.defaultVoucherFooter));
            itemBuilder.flag(ItemFlag.HIDE_ATTRIBUTES);
            itemBuilder.flag(ItemFlag.HIDE_POTION_EFFECTS);
            item = itemBuilder.build();
        }
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

    public UUID identifyExclusivity(ItemStack item) {
        if (ItemUtil.isEmpty(item))
            return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return null;
        if (!meta.getPersistentDataContainer().has(voucherIdentifier, PersistentDataType.STRING))
            return null;
        return meta.getPersistentDataContainer().get(exclusivePlayerIdentifier, DataType.UUID);
    }

    public ItemStack changeExclusivity(ItemStack item, UUID id) {
        if (ItemUtil.isEmpty(item))
            return item;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return item;
        if (!meta.getPersistentDataContainer().has(voucherIdentifier, PersistentDataType.STRING))
            return item;
        if (id != null)
            meta.getPersistentDataContainer().set(exclusivePlayerIdentifier, DataType.UUID, id);
        else
            meta.getPersistentDataContainer().remove(exclusivePlayerIdentifier);
        item.setItemMeta(meta);
        return item;
    }
}
