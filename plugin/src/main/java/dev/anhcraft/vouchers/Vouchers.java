package dev.anhcraft.vouchers;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Preconditions;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import dev.anhcraft.config.bukkit.utils.ColorUtil;
import dev.anhcraft.jvmkit.utils.FileUtil;
import dev.anhcraft.jvmkit.utils.IOUtil;
import dev.anhcraft.jvmkit.utils.ReflectionUtil;
import dev.anhcraft.vouchers.api.ApiProvider;
import dev.anhcraft.vouchers.api.VouchersApi;
import dev.anhcraft.vouchers.cmd.MainCommand;
import dev.anhcraft.vouchers.config.MainConfig;
import dev.anhcraft.vouchers.config.MessageConfig;
import dev.anhcraft.vouchers.listener.PlayerListener;
import dev.anhcraft.vouchers.manager.VouchersManager;
import dev.anhcraft.vouchers.storage.player.PlayerDataManager;
import dev.anhcraft.vouchers.storage.server.ServerDataManager;
import dev.anhcraft.vouchers.util.ConfigHelper;
import dev.anhcraft.vouchers.util.PluginLogger;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.cloud.CloudExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class Vouchers extends JavaPlugin {
    public static final int LATEST_PLAYER_DATA_VERSION = 1;
    public static final int LATEST_SERVER_DATA_VERSION = 1;
    private static Vouchers INSTANCE;
    private static VouchersApiImpl API;
    private PlaceholderAPIPlugin placeholderApi;
    public VouchersManager vouchersManager;
    public PlayerDataManager playerDataManager;
    public ServerDataManager serverDataManager;
    public PluginLogger pluginLogger;
    public MessageConfig messageConfig;
    public MainConfig mainConfig;

    @NotNull
    public static Vouchers getInstance() {
        return INSTANCE;
    }

    public static VouchersApi getApi() {
        return API;
    }

    public void debug(@NotNull String format, @NotNull Object... args) {
        debug(1, format, args);
    }

    public void debug(int level, @NotNull String format, @NotNull Object... args) {
        if (mainConfig != null && mainConfig.debugLevel >= level) {
            getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[Vouchers#DEBUG] " + String.format(format, args));
        }
    }

    public void msg(CommandSender sender, String str) {
        if (str == null) {
            sender.sendMessage(ColorUtil.colorize(messageConfig.prefix + "&c<Empty message>"));
            return;
        }
        sender.sendMessage(ColorUtil.colorize(messageConfig.prefix + str));
    }

    public void rawMsg(CommandSender sender, String str) {
        if (str == null) {
            sender.sendMessage(ColorUtil.colorize("&c<Empty message>"));
            return;
        }
        sender.sendMessage(ColorUtil.colorize(str));
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        API = new VouchersApiImpl(this);
        vouchersManager = new VouchersManager(this);
        serverDataManager = new ServerDataManager(this);
        playerDataManager = new PlayerDataManager(this);
        pluginLogger = new PluginLogger(new File(getDataFolder(), "logs"));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        reload();
        serverDataManager.loadData();

        PaperCommandManager pcm = new PaperCommandManager(this);
        pcm.enableUnstableAPI("help");
        pcm.registerCommand(new MainCommand(this));
        CommandCompletions<BukkitCommandCompletionContext> cmpl = pcm.getCommandCompletions();
        cmpl.registerAsyncCompletion("vouchers", context -> API.getVoucherIds());

        ReflectionUtil.setDeclaredStaticField(ApiProvider.class, "api", API);

        placeholderApi = PlaceholderAPIPlugin.getInstance();

        new UpdateChecker(this, UpdateCheckSource.SPIGOT, "112837")
                .setDonationLink("https://paypal.me/lycheene")
                .checkEveryXHours(12)
                .checkNow();

        new BukkitRunnable() {
            @Override
            public void run() {
                installExpansions();
            }
        }.runTaskLaterAsynchronously(this, 60);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        playerDataManager.terminate();
        serverDataManager.terminate();
        pluginLogger.flush();
    }

    public void reload() {
        getServer().getScheduler().cancelTasks(this);

        getDataFolder().mkdir();
        mainConfig = ConfigHelper.load(MainConfig.class, requestConfig("config.yml"));
        messageConfig = ConfigHelper.load(MessageConfig.class, requestConfig("messages.yml"));

        vouchersManager.reload(requestConfig("vouchers.yml"));
        serverDataManager.reload();
        playerDataManager.reload();

        getServer().getScheduler().runTaskTimerAsynchronously(this, pluginLogger::flush, 60L, 100L);
    }

    private YamlConfiguration requestConfig(String path) {
        File f = new File(getDataFolder(), path);
        Preconditions.checkArgument(f.getParentFile().exists());

        if (!f.exists()) {
            try {
                FileUtil.write(f, IOUtil.readResource(Vouchers.class, "/config/" + path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return YamlConfiguration.loadConfiguration(f);
    }

    private void installExpansions() {
        List<CompletableFuture<File>> completableFutures = new ArrayList<>();

        for (String expansion : new String[]{"player"}) {
            if (placeholderApi.getCloudExpansionManager().getCloudExpansionsInstalled().containsKey(expansion))
                continue;
            completableFutures.add(installExpansion(expansion));
        }

        if (completableFutures.isEmpty()) return;

        getLogger().info("Installing required PlaceholderAPI expansions...");

        CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new))
          .whenComplete((unused, throwable) -> {
              if (throwable != null) {
                  try {
                      throw throwable;
                  } catch (Throwable e) {
                      throw new RuntimeException(e);
                  }
              }

              getLogger().info("Expansions are installed. Trying reloading PlaceholderAPI...");

              new BukkitRunnable() {
                  @Override
                  public void run() {
                      placeholderApi.reloadConf(Bukkit.getConsoleSender());
                  }
              }.runTask(this);
          });
    }

    private CompletableFuture<File> installExpansion(String name) {
        return placeholderApi.getCloudExpansionManager()
          .findCloudExpansionByName(name).map((Function<CloudExpansion, CompletableFuture<File>>) expansion -> {
              if (!expansion.isVerified())
                  return CompletableFuture.failedFuture(new RuntimeException("Expansion is unverified"));
              CloudExpansion.Version version = expansion.getVersion(expansion.getLatestVersion());
              return placeholderApi.getCloudExpansionManager().downloadExpansion(expansion, version);
          }).orElseGet(() -> CompletableFuture.failedFuture(new RuntimeException("Expansion not exists")));
    }
}
