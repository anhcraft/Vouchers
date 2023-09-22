package dev.anhcraft.vouchers.util;

import dev.anhcraft.config.ConfigDeserializer;
import dev.anhcraft.config.ConfigSerializer;
import dev.anhcraft.config.bukkit.BukkitConfigDeserializer;
import dev.anhcraft.config.bukkit.BukkitConfigProvider;
import dev.anhcraft.config.bukkit.BukkitConfigSerializer;
import dev.anhcraft.config.bukkit.struct.YamlConfigSection;
import dev.anhcraft.config.schema.SchemaScanner;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConfigHelper {
    public static final ConfigSerializer SERIALIZER;
    public static final ConfigDeserializer DESERIALIZER;

    static {
        SERIALIZER = new BukkitConfigSerializer(BukkitConfigProvider.YAML);
        DESERIALIZER = new BukkitConfigDeserializer(BukkitConfigProvider.YAML);
    }

    @NotNull
    public static <T> T load(Class<T> clazz, ConfigurationSection section) {
        try {
            return DESERIALIZER.transformConfig(Objects.requireNonNull(SchemaScanner.scanConfig(clazz)), new YamlConfigSection(section));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T load(Class<T> clazz, ConfigurationSection section, T dest) {
        try {
            return DESERIALIZER.transformConfig(Objects.requireNonNull(SchemaScanner.scanConfig(clazz)), new YamlConfigSection(section), dest);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> void save(Class<T> clazz, ConfigurationSection section, T dest) {
        try {
            SERIALIZER.transformConfig(Objects.requireNonNull(SchemaScanner.scanConfig(clazz)), new YamlConfigSection(section), dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

