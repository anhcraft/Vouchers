package dev.anhcraft.vouchers.config;

public class VoucherCodeConfig {
  public int length;
  public String chars;
  public AntiBruteForceConfig antiBruteForce;

  public static class AntiBruteForceConfig {
    public boolean enabled;
    public int maxFailedAttempts;
    public int blockRedemptionCooldown;
    public int resetCounterCooldown;
  }
}
