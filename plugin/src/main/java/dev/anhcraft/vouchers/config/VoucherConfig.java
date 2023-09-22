package dev.anhcraft.vouchers.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import org.jetbrains.annotations.Nullable;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class VoucherConfig {
    @Validation(notNull = true)
    public String name;

    @Validation(notNull = true)
    public String[] description;

    @Validation(notNull = true)
    public String[] rewards;

    @Nullable
    public ItemBuilder customItem;
}
