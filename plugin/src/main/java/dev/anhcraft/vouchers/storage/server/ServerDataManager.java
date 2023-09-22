package dev.anhcraft.vouchers.storage.server;

import dev.anhcraft.vouchers.Vouchers;
import dev.anhcraft.vouchers.util.CompressUtils;
import dev.anhcraft.vouchers.util.ConfigHelper;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public class ServerDataManager {
    private final Vouchers plugin;
    private final File file;
    private ServerDataImpl serverData;

    public ServerDataManager(Vouchers plugin) {
        this.plugin = plugin;
        File folder = new File(plugin.getDataFolder(), "data");
        folder.mkdir();
        file = new File(folder, "server.gz");
    }

    public ServerDataImpl getData() {
        return serverData;
    }

    public void loadData() {
        if (file.exists()) {
            YamlConfiguration conf = null;
            try {
                conf = YamlConfiguration.loadConfiguration(new StringReader(CompressUtils.readAndDecompressString(file)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (conf == null)
                serverData = new ServerDataImpl(new ServerDataConfig());

            serverData = new ServerDataImpl(ConfigHelper.load(ServerDataConfig.class, conf));
        } else {
            serverData = new ServerDataImpl(new ServerDataConfig());
        }

        plugin.debug("Server data loaded!");
    }

    private void saveDataIfDirty() {
        if (serverData.internal().dirty.compareAndSet(true, false)) {
            plugin.debug("Saving server data...");
            YamlConfiguration conf = new YamlConfiguration();
            ConfigHelper.save(ServerDataConfig.class, conf, serverData.internal());
            try {
                CompressUtils.compressAndWriteString(conf.saveToString(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void reload() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::saveDataIfDirty, 20, 200);
    }

    public void terminate() {
        serverData.internal().dirty.set(true);
        saveDataIfDirty();
    }
}
