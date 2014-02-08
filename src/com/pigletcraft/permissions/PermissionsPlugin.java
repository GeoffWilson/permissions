package com.pigletcraft.permissions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsPlugin extends JavaPlugin implements Listener {

    private ConcurrentHashMap<String, PlayerData> activePlayers;

    private class PlayerData {
        public PermissionAttachment permissions;
        public ChatColor chatColor;
    }

    @Override
    public void onEnable() {

        // Create the necessary objects for the plugin
        activePlayers = new ConcurrentHashMap<String, PlayerData>();

        // Register as an event handler
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        ChatColor c = ChatColor.WHITE;
        if (activePlayers.containsKey(event.getPlayer().getName())) {
            c = activePlayers.get(event.getPlayer().getName()).chatColor;
        }
        event.setFormat("<" + c + "%s" + ChatColor.WHITE + "> %s");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (activePlayers.containsKey(player.getName())) {
            activePlayers.remove(player.getName());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        if (!activePlayers.containsKey(player.getName())) {

            Database db = new Database();
            PlayerData newPlayerData = new PlayerData();
            newPlayerData.chatColor = db.getChatColor(player.getName());
            newPlayerData.permissions = player.addAttachment(this);

            // Load the players permissions from the database
            newPlayerData.permissions.setPermission("commandbook.home.set", true);
            newPlayerData.permissions.setPermission("commandbook.home.teleport", true);
            newPlayerData.permissions.setPermission("commandbook.spawn", true);
            newPlayerData.permissions.setPermission("validation.basic", true);
            newPlayerData.permissions.setPermission("bank.bank", true);

            if (player.getName().equals("GeoffWilson") || player.getName().equals("Benshiro")) {
                newPlayerData.permissions.setPermission("validation.rain", true);
                newPlayerData.permissions.setPermission("validation.fireworks", true);
            }

            activePlayers.put(player.getName(), newPlayerData);
        }
    }
}
