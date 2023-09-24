package dev.anhcraft.vouchers.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.anhcraft.vouchers.Vouchers;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScopedLog {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private final PluginLogger logger;
    private final Map<String, Object> data;

    public ScopedLog(PluginLogger logger, String scope) {
        this.logger = logger;
        this.data = new LinkedHashMap<>(6);
        data.put("time", Vouchers.getInstance().mainConfig.dateTimeFormat.format(new Date()));
        data.put("scope", scope);
    }

    public ScopedLog add(String key, Object value) {
        data.put(key, normalize(value));
        return this;
    }

    private Object normalize(Object value) {
        if (value == null) {
            return "(null)";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value;
        } else if (value instanceof String) {
            return value;
        } else if (value instanceof OfflinePlayer) {
            return ((OfflinePlayer) value).getUniqueId().toString();
        } else if (value instanceof Collection || value.getClass().isArray()) {
            return value;
        }
        return String.valueOf(value);
    }

    public void flush() {
        logger.writeRaw(GSON.toJson(data));
    }
}
