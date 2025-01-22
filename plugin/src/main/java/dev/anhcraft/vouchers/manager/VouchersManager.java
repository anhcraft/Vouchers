package dev.anhcraft.vouchers.manager;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import com.jeff_media.morepersistentdatatypes.DataType;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.jvmkit.utils.ObjectUtil;
import dev.anhcraft.jvmkit.utils.PresentPair;
import dev.anhcraft.palette.util.ItemUtil;
import dev.anhcraft.vouchers.Vouchers;
import dev.anhcraft.vouchers.api.data.PlayerData;
import dev.anhcraft.vouchers.api.data.ServerData;
import dev.anhcraft.vouchers.api.entity.Voucher;
import dev.anhcraft.vouchers.util.TimeUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class VouchersManager {
    private final Vouchers plugin;
    private final RewardExecutor rewardExecutor;
    private final VoucherStore voucherStore;
    private final Map<PresentPair<UUID, String>, Long> doubleCheckQueue = new HashMap<>();
    private final NamespacedKey voucherIdentifier;
    private final NamespacedKey physicalIdentifier;
    private final NamespacedKey exclusivePlayerIdentifier;

    public VouchersManager(Vouchers plugin) {
        this.plugin = plugin;
        this.rewardExecutor = new RewardExecutor(plugin);
        this.voucherStore = new VoucherStore(plugin);
        voucherIdentifier = new NamespacedKey(plugin, "voucher");
        physicalIdentifier = new NamespacedKey(plugin, "physical-id");
        exclusivePlayerIdentifier = new NamespacedKey(plugin, "exclusive-player");
    }

    public void cleanData(UUID player) {
        doubleCheckQueue.keySet().removeIf(p -> p.getFirst().equals(player));
    }

    public void reload(YamlConfiguration vouchersConfig) {
        voucherStore.reload(vouchersConfig);
    }

    public Map<String, Voucher> getVouchers() {
        return voucherStore.getVouchers();
    }

    public int preUse(Player player, String id, String physicalId, Voucher voucher, int expectedBulkSize) {
        plugin.debug(2, "Checking '%s' prerequisite for '%s'", id, player.getName());

        if (voucher.hasPhysicalId()) {
            if (Vouchers.getApi().getServerData().isPhysicalIdUsed(physicalId)) {
                plugin.msg(player, plugin.messageConfig.physicalVoucherUsed);
                return 0;
            }
        }

        var globalUsage = Vouchers.getApi().getServerData().getUsageLimitCount(id);
        var globalUsageLimit = voucher.getUsageLimit().getGlobal();
        plugin.debug(2, "- Global usage limit: %d/%d", globalUsage, globalUsageLimit);
        if (globalUsageLimit > 0 && (expectedBulkSize = Math.min(expectedBulkSize, globalUsageLimit - globalUsage)) < 1) {
            plugin.msg(player, plugin.messageConfig.globalUsageLimit.replace("{max}", String.valueOf(globalUsageLimit)));
            return 0;
        }

        var playerData = Vouchers.getApi().getPlayerData(player);
        var playerUsage = playerData.getUsageLimitCount(id);
        var playerUsageLimit = voucher.getUsageLimit().evaluate(player);
        plugin.debug(2, "- Player usage limit: %d/%d", playerUsage, playerUsageLimit);
        if (playerUsageLimit > 0 && (expectedBulkSize = Math.min(expectedBulkSize, playerUsageLimit - playerUsage)) < 1) {
            plugin.msg(player, plugin.messageConfig.playerUsageLimit.replace("{max}", String.valueOf(playerUsageLimit)));
            return 0;
        }

        var cooldown = voucher.getCooldown().evaluate(player);
        if (cooldown > 0) {
            var nextCooldown = playerData.getLastUsed(id) + cooldown * 1000L;
            var remainTime = Math.max(0, (nextCooldown - System.currentTimeMillis()) / 1000);
            plugin.debug(2, "- Cooldown remain: %d", remainTime);
            if (remainTime > 0) {
                plugin.msg(player, plugin.messageConfig.inCooldown.replace("{time}", TimeUtils.format(remainTime)));
                return 0;
            }
            // If cooldown exists, then we cannot bulk open
            expectedBulkSize = 1;
            // TODO An option to allow bypass cooldown in bulk?
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
                    return 0;
                }
            } catch (EvaluationException | ParseException e) {
                plugin.debug(2, "- Failed to evaluate condition: %s", condition);
                plugin.msg(player, plugin.messageConfig.conditionNotSatisfied);
                return 0;
            }
        }

        if (voucher.shouldDoubleCheck()) {
            PresentPair<UUID, String> p = new PresentPair<>(player.getUniqueId(), id);
            Long v = doubleCheckQueue.get(p);
            if (v != null && v >= System.currentTimeMillis()) {
                return expectedBulkSize;
            }
            plugin.msg(player, plugin.messageConfig.doubleCheck);
            doubleCheckQueue.put(p, System.currentTimeMillis() + plugin.mainConfig.doubleCheckTimeout * 1000L);
            return 0;
        }

        return expectedBulkSize;
    }

    public List<String> onUse(Player player, Voucher voucher) {
        return rewardExecutor.executeReward(player, voucher);
    }

    public void postUse(Player player, String id, String physicalId, Voucher voucher, int bulkSize) {
        for (String str : ObjectUtil.optional(voucher.getUseMessage(), plugin.messageConfig.defaultUseMessage)) {
            if (str == null) continue;
            plugin.rawMsg(player, PlaceholderAPI.setPlaceholders(player, str)
                    .replace("{voucher-name}", voucher.getName())
                    .replace("{bulk-size}", String.valueOf(bulkSize)));
        }
        player.playSound(player.getLocation(), plugin.mainConfig.defaultUseSound, 1.0f, 1.0f);
        PlayerData pd = Vouchers.getApi().getPlayerData(player);
        pd.setLastUsed(id, System.currentTimeMillis());
        pd.increaseUsageLimitCount(id, bulkSize);
        ServerData sd = Vouchers.getApi().getServerData();
        sd.increaseUsageCount(id);
        sd.increaseUsageLimitCount(id, bulkSize);
        if (voucher.hasPhysicalId() && physicalId != null)
            sd.usePhysicalId(physicalId);
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
        if (meta != null) {
            meta.getPersistentDataContainer().set(voucherIdentifier, PersistentDataType.STRING, id);
            if (voucher.hasPhysicalId()) {
                String physicalId = id + "/" + System.currentTimeMillis() + "/" +
                  ThreadLocalRandom.current().nextInt(1000, 10000);
                meta.getPersistentDataContainer().set(physicalIdentifier, PersistentDataType.STRING, physicalId);
            }
            item.setItemMeta(meta);
        }
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

    public String getPhysicalId(ItemStack item) {
        if (ItemUtil.isEmpty(item))
            return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return null;
        return meta.getPersistentDataContainer().get(physicalIdentifier, PersistentDataType.STRING);
    }
}
