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
     * @param delta the addition
     */
    default void increaseUsageLimitCount(String voucherId, int delta) {
        setUsageLimitCount(voucherId, getUsageLimitCount(voucherId) + delta);
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

    /**
     * Marks a physical id as used.
     * @param id physical id
     */
    void usePhysicalId(String id);

    /**
     * Checks whether the given physical id has been used.
     * @param id physical id
     * @return whether the given physical id has been used
     */
    boolean isPhysicalIdUsed(String id);
}
