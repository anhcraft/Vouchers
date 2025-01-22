package dev.anhcraft.vouchers.manager;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.jvmkit.utils.ObjectUtil;
import dev.anhcraft.vouchers.Vouchers;
import dev.anhcraft.vouchers.api.entity.Voucher;
import dev.anhcraft.vouchers.api.entity.VoucherBuilder;
import dev.anhcraft.vouchers.api.util.GroupSettings;
import dev.anhcraft.vouchers.config.VoucherConfig;
import dev.anhcraft.vouchers.util.ConfigHelper;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class VoucherStore {
  private final Vouchers plugin;
  private final Map<String, Voucher> vouchers = new HashMap<>();

  public VoucherStore(Vouchers plugin) {
    this.plugin = plugin;
  }

  public void reload(YamlConfiguration vouchersConfig) {
    vouchers.clear();
    for (String id : vouchersConfig.getKeys(false)) {
      VoucherConfig config = ConfigHelper.load(VoucherConfig.class, vouchersConfig.getConfigurationSection(id));
      VoucherBuilder voucherBuilder = new VoucherBuilder();
      voucherBuilder.icon(ObjectUtil.optional(config.icon, plugin.mainConfig.defaultVoucherIcon));
      voucherBuilder.name(config.name);
      voucherBuilder.description(config.description);
      voucherBuilder.physicalId(config.physicalId);
      if (config.customItem != null) {
        ItemBuilder itemBuilder = config.customItem;
        if (itemBuilder.material().isAir()) {
          itemBuilder.material(ObjectUtil.optional(config.icon, plugin.mainConfig.defaultVoucherIcon));
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
      voucherBuilder.doubleCheck(config.doubleCheck);
      voucherBuilder.useMessage(config.useMessage);
      vouchers.put(id, voucherBuilder.build());
    }
    plugin.getLogger().info("Loaded " + vouchers.size() + " vouchers");
  }

  public Map<String, Voucher> getVouchers() {
    return vouchers;
  }
}
