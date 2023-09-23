package dev.anhcraft.vouchers.api.data;

public interface ServerData extends Modifiable, Versioned {

    /**
     * Gets the usage limit count.
     * @param voucherId the voucher ID
     * @return the usage limit count (non-negative)
     */
    int getUsageLimitCount(String voucherId);

    /**
     * Sets the usage limit count.
     * @param voucherId the voucher ID
     * @param count the usage limit count
     */
    void setUsageLimitCount(String voucherId, int count);

    /**
     * Increases the usage limit count.
     * @param voucherId the voucher ID
     */
    default void increaseUsageLimitCount(String voucherId) {
        setUsageLimitCount(voucherId, getUsageLimitCount(voucherId) + 1);
    }

    /**
     * Gets the usage count.
     * @param voucherId the voucher ID
     * @return the usage count (non-negative)
     */
    int getUsageCount(String voucherId);

    /**
     * Sets the usage count.
     * @param voucherId the voucher ID
     * @param count the usage count
     */
    void setUsageCount(String voucherId, int count);

    /**
     * Increases the usage count.
     * @param voucherId the voucher ID
     */
    default void increaseUsageCount(String voucherId) {
        setUsageCount(voucherId, getUsageCount(voucherId) + 1);
    }
}
