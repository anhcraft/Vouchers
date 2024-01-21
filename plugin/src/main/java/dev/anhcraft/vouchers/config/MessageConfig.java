package dev.anhcraft.vouchers.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Optional;
import dev.anhcraft.config.annotations.Validation;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class MessageConfig {
    @Validation(notNull = true)
    public String prefix;
    @Validation(notNull = true)
    public String[] defaultUseMessage;
    @Validation(notNull = true)
    public String noRewardsGiven;
    @Validation(notNull = true)
    public String invalidVoucher;
    @Validation(notNull = true)
    public String globalUsageLimit;
    @Validation(notNull = true)
    public String playerUsageLimit;
    @Validation(notNull = true)
    public String inCooldown;

    @Optional
    public String conditionNotSatisfied = "&cYou do not satisfy the condition";

    @Optional
    public String exclusivityNotSatisfied = "&cThis voucher is exclusive to someone else. You cannot use it!";

    @Optional
    public String doubleCheck = "&bRight-click the voucher again to confirm!";
}
