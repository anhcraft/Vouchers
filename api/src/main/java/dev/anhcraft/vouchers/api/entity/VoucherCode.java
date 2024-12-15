package dev.anhcraft.vouchers.api.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public record VoucherCode(@NotNull String code, @NotNull Voucher voucher, @NotNull UUID issuer, @Nullable UUID user,
                          @NotNull Date issueDate, @Nullable Date redeemDate) {
  public boolean isUsed() {
    return user != null;
  }
}
