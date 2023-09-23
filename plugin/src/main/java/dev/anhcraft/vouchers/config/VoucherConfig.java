package dev.anhcraft.vouchers.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Optional;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class VoucherConfig {
    @Validation(notNull = true)
    public String name;

    @Optional
    public String[] description = ArrayUtils.EMPTY_STRING_ARRAY;

    @Nullable
    public Material icon;

    @Validation(notNull = true)
    public String[] rewards;

    @Nullable
    public ItemBuilder customItem;

    @Nullable
    public Map<String, Integer> usageLimit;

    @Nullable
    public Map<String, Integer> cooldown;
}
