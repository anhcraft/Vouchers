package dev.anhcraft.vouchers.config;

import dev.anhcraft.config.annotations.*;
import org.bukkit.Sound;

import java.text.SimpleDateFormat;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class MainConfig {
    public boolean devMode;

    public int debugLevel;

    @Optional
    public Sound defaultSound = Sound.ENTITY_PLAYER_LEVELUP;

    public String[] defaultVoucherFooter;

    public boolean preventNoRewards;

    @Validation(notNull = true, silent = true)
    private String dateFormat = "dd/MM/yyyy HH:mm:ss";

    @Exclude
    public SimpleDateFormat dateTimeFormat;

    @PostHandler
    private void handle() {
        dateTimeFormat = new SimpleDateFormat(dateFormat);
    }
}
