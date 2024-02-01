package org.timurarahost.aichatregulator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PluginCommandExecutor implements CommandExecutor {
    private final AIChatRegulator plugin;

    public PluginCommandExecutor(AIChatRegulator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Command without arguments - Display plugin info
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "AIChatRegulator Plugin v1.0 by Timur Yun - Monitoring chat behavior.");
            return true;
        }
        // Command with a player name - Display the count of inappropriate messages
        else if (args.length == 1) {
            String playerName = args[0];
            int count = plugin.getInappropriateMessageCount(playerName);

            // Retrieve the template from config
            String messageTemplate = plugin.getConfig().getString("messages.inappropriateMessageCount", "%playerName% has %count% inappropriate messages.");

            // Replace placeholders with actual values
            String message = messageTemplate.replace("%playerName%", playerName).replace("%count%", String.valueOf(count));

            sender.sendMessage(message);
            return true;
        }
        // Incorrect usage
        else {
            sender.sendMessage("Usage: /aichat [playerName]");
            return false;
        }
    }
}
