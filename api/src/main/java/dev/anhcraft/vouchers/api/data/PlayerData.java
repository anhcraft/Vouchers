package dev.anhcraft.vouchers.api.data;

public interface PlayerData extends Modifiable, Versioned {
    /**
     * Gets the last time the voucher was used.
     * @param voucherId the voucher ID
     * @return the last timestamp or {@code 0} if not used before
     */
    long getLastUsed(String voucherId);

    /**
     * Sets the last time the voucher was used.
     * @param voucherId the voucher ID
     * @param timestamp the timestamp
     */
    void setLastUsed(String voucherId, long timestamp);

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
     * @param delta the addition
     */
    default void increaseUsageLimitCount(String voucherId, int delta) {
        setUsageLimitCount(voucherId, getUsageLimitCount(voucherId) + delta);
    }
}
