package dev.anhcraft.vouchers.api.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GroupSettings {
    public static final String COOLDOWN_PERM = "vouchers.cooldown.";
    public static final String USAGE_LIMIT_PERM = "vouchers.usage-limit.";

    private final String permissionPrefix;
    private final int global;
    private final int fallback;
    private final Map<String, Integer> groups;

    public static GroupSettings empty(String permissionPrefix) {
        return new GroupSettings(permissionPrefix, 0, 0, Collections.emptyMap());
    }

    public static GroupSettings of(String permissionPrefix, Map<String, Integer> settings, boolean reversed) {
        int global = 0;
        int fallback = 0;
        Map<String, Integer> groups = new LinkedHashMap<>();
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(settings.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));
        if (reversed) Collections.reverse(entries);

        for (Map.Entry<String, Integer> e : entries) {
            if (e.getKey().equalsIgnoreCase("global")) {
                global = e.getValue();
            } else if (e.getKey().equalsIgnoreCase("default")) {
                fallback = e.getValue();
            } else {
                groups.put(e.getKey(), e.getValue());
            }
        }

        return new GroupSettings(permissionPrefix, global, fallback, groups);
    }

    private GroupSettings(String permissionPrefix, int global, int fallback, Map<String, Integer> groups) {
        this.permissionPrefix = permissionPrefix;
        this.global = global;
        this.fallback = fallback;
        this.groups = groups;
    }

    public int getGlobal() {
        return global;
    }

    public int getFallback() {
        return fallback;
    }

    public int evaluate(@NotNull Player player) {
        for (Map.Entry<String, Integer> entry : groups.entrySet()) {
            if (player.hasPermission(permissionPrefix + entry.getKey())) {
                return entry.getValue();
            }
        }
        return fallback;
    }
}
