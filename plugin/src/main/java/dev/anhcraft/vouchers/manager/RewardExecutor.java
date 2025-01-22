package dev.anhcraft.vouchers.manager;

import dev.anhcraft.jvmkit.utils.EnumUtil;
import dev.anhcraft.vouchers.Vouchers;
import dev.anhcraft.vouchers.api.entity.Voucher;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RewardExecutor {
  private static final Pattern CONDITION_TAG_PATTERN = Pattern.compile("\\[[a-z-]+(=[A-Za-z0-9-_.]*)?]");
  private final Vouchers plugin;

  public RewardExecutor(Vouchers plugin) {
    this.plugin = plugin;
  }

  public List<String> executeReward(Player player, Voucher voucher) {
    List<String> executedCommands = new ArrayList<>();

    plugin.debug("Executing %d rewards from '%s' for '%s'", voucher.getRewards().length, voucher.getName(), player.getName());

    outer:
    for (String reward : voucher.getRewards()) {
      plugin.debug(2, "- Before PlaceholderAPI-applied: %s", reward);
      reward = PlaceholderAPI.setPlaceholders(player, reward);
      plugin.debug(2, "- After PlaceholderAPI-applied: %s", reward);

      boolean runAsPlayer = false;
      double chance = 0.0;
      int delay = 0;
      String permission = null;

      int rewardType = 0; // 0: command, 1: message, 2: sound
      boolean broadcast = false;

      int endOfMatcher = 0;
      Matcher m = CONDITION_TAG_PATTERN.matcher(reward);
      while (m.find()) {
        endOfMatcher = Math.max(endOfMatcher, m.end());
        String tag = m.group();
        String[] args = tag.substring(1, tag.length() - 1).split("=");
        switch (args[0]) {
          case "player":
            plugin.debug(2, "- Run as player: %s", tag);
            runAsPlayer = args.length == 1 || args[1].equalsIgnoreCase("true");
            break;
          case "message":
            plugin.debug(2, "- Message: %s", tag);
            rewardType = (args.length == 1 || args[1].equalsIgnoreCase("true")) ? 1 : 0;
            break;
          case "sound":
            plugin.debug(2, "- Sound: %s", tag);
            rewardType = (args.length == 1 || args[1].equalsIgnoreCase("true")) ? 2 : 0;
            break;
          case "broadcast":
            plugin.debug(2, "- Broadcast: %s", tag);
            broadcast = args.length == 1 || args[1].equalsIgnoreCase("true");
            break;
          case "chance":
            if (args.length == 1) {
              throwError(player, voucher.getName(), reward, tag);
              continue outer;
            }
            try {
              chance = Double.parseDouble(args[1]);
              if (chance < 0 || chance > 1) {
                throwError(player, voucher.getName(), reward, tag);
                continue outer;
              }
              plugin.debug(2, "- Chance=%f: %s", chance, tag);
            } catch (NumberFormatException e) {
              throwError(player, voucher.getName(), reward, tag);
              continue outer;
            }
            break;
          case "delay":
            if (args.length == 1) {
              throwError(player, voucher.getName(), reward, tag);
              continue outer;
            }
            try {
              delay = Integer.parseInt(args[1]);
              if (delay < 0) {
                throwError(player, voucher.getName(), reward, tag);
                continue outer;
              }
              plugin.debug(2, "- Delay=%ds: %s", delay, tag);
            } catch (NumberFormatException e) {
              throwError(player, voucher.getName(), reward, tag);
              continue outer;
            }
            break;
          case "permission":
            if (args.length == 1) {
              throwError(player, voucher.getName(), reward, tag);
              continue outer;
            }
            permission = args[1];
            plugin.debug(2, "- Permission=%s: %s", args[1], tag);
            break;
        }
      }

      String ctn = reward.substring(endOfMatcher).trim();
      plugin.debug(2, "- Content: %s", ctn);

      if (ctn.isEmpty()) {
        plugin.debug(2, "=> FAILED: Skipped due to empty content given");
        continue;
      }
      if (permission != null && !player.hasPermission(permission)) {
        plugin.debug(2, "=> FAILED: Skipped due to no permission");
        continue;
      }
      if (chance > 0 && ThreadLocalRandom.current().nextDouble() > chance) {
        plugin.debug(2, "=> FAILED: Skipped due to unlucky");
        continue;
      }
      plugin.debug(2, "=> SUCCESS");

      if (rewardType == 1) {
        if (delay == 0) {
          sendMessage(player, broadcast, ctn);
        } else {
          boolean finalBroadcast = broadcast;
          // TODO localize schedule to the player's region (Folia)
          Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> sendMessage(player, finalBroadcast, ctn), delay * 20L);
        }
        executedCommands.add("[message] "+ctn);
      } else if (rewardType == 2) {
        if (delay == 0) {
          playSound(player, broadcast, ctn);
        } else {
          boolean finalBroadcast = broadcast;
          // TODO localize schedule to the player's region (Folia)
          Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> playSound(player, finalBroadcast, ctn), delay * 20L);
        }
        executedCommands.add("[sound] "+ctn);
      } else {
        CommandSender sender = runAsPlayer ? player : Bukkit.getConsoleSender();
        if (delay == 0) {
          Bukkit.dispatchCommand(sender, ctn);
        } else {
          Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(sender, ctn), delay * 20L);
        }
        executedCommands.add(ctn);
      }
    }

    return executedCommands;
  }

  private void sendMessage(Player player, boolean broadcast, String ctn) {
    ctn = PlaceholderAPI.setPlaceholders(player, ctn);
    if (broadcast) {
      for (Player p : Bukkit.getOnlinePlayers()) {
        plugin.rawMsg(p, ctn);
      }
    } else {
      plugin.rawMsg(player, ctn);
    }
  }

  private void playSound(Player player, boolean broadcast, String sound) {
    Sound enumSound = (Sound) EnumUtil.findEnum(Sound.class, sound.toUpperCase());
    if (enumSound != null) {
      if (broadcast) {
        for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), enumSound, 1.0f, 1.0f);
      } else {
        player.playSound(player.getLocation(), enumSound, 1.0f, 1.0f);
      }
    } else if (sound.matches("[a-z0-9/._-]+")) {
      if (broadcast) {
        for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
      } else {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
      }
    } else {
      plugin.debug(1, String.format("Invalid sound '%s'", sound));
    }
  }

  private void throwError(Player player, String voucher, String reward, String tag) {
    plugin.getLogger().warning(String.format(
      "Invalid tag '%s' in reward '%s' while '%s' is trying to claim '%s'",
      tag, reward, player.getName(), voucher
    ));
  }
}
