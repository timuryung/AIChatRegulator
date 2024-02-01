package org.timurarahost.aichatregulator;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final AIChatRegulator plugin;

    public ChatListener(AIChatRegulator plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (plugin.isModelLoaded()) {
            String message = event.getMessage();
            Player player = event.getPlayer();
            plugin.enqueueChatMessage(player, message);
        } else {
            event.getPlayer().sendMessage(plugin.getConfig().getString("model_not_loaded_message", "Default"));

        }
    }
}
