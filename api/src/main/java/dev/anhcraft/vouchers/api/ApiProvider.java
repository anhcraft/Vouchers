package dev.anhcraft.vouchers.api;

import org.jetbrains.annotations.NotNull;

public class ApiProvider {
    private static VouchersApi api;

    @NotNull
    public static VouchersApi getApi() {
        if (api == null) {
            throw new IllegalStateException("Api is not initialized");
        }
        return api;
    }
}
