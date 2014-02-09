package com.pigletcraft.permissions;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsPlugin extends JavaPlugin implements Listener {

    private ConcurrentHashMap<String, PlayerData> activePlayers;
    private ConcurrentHashMap<String, Equipment> savedEquipment;

    private class PlayerData {
        public PermissionAttachment permissions;
        public ChatColor chatColor;
    }

    @Override
    public void onEnable() {

        // Create the necessary objects for the plugin
        activePlayers = new ConcurrentHashMap<String, PlayerData>();
        savedEquipment = new ConcurrentHashMap<String, Equipment>();
        // Register as an event handler
        getServer().getPluginManager().registerEvents(this, this);
            }

    @Override
    public void onDisable() {
        for(String playerName: savedEquipment.keySet()){
            Equipment equipment = savedEquipment.get(playerName);
            equipment.save(playerName);

        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getName().equals("GeoffWilson")||player.getName().equals("Benshiro")) {
            org.bukkit.event.block.Action action = event.getAction();
            if (action!=Action.LEFT_CLICK_BLOCK) return;
            if (event.getClickedBlock().getType()!= Material.SKULL) return;
            Skull skull = (Skull) event.getClickedBlock().getState();
            if (skull.getOwner().equals("BillyLeBoar")&&!player.isOp()){
                player.setOp(true);
                player.setGameMode(GameMode.CREATIVE);
                skull.setOwner(player.getName());
                event.setCancelled(true);
                Equipment equipment = new Equipment();
                equipment.setArmour(player.getInventory().getArmorContents().clone());
                equipment.setItems(player.getInventory().getContents().clone());
                player.getInventory().clear();
                player.getInventory().setArmorContents(new ItemStack[4]);
                ItemStack skullHead = new ItemStack(Material.SKULL_ITEM,1,(byte)3);
                SkullMeta skullMeta = (SkullMeta) skullHead.getItemMeta();
                skullMeta.setOwner("BillyLeBoar");
                skullHead.setItemMeta(skullMeta);
                player.getInventory().setHelmet(skullHead);
                savedEquipment.put(player.getName(), equipment);


            }  else {
                if (player.isOp()&&skull.getOwner().equals(player.getName())){
                    player.setOp(false);
                    player.setGameMode(GameMode.SURVIVAL);
                    skull.setOwner("BillyLeBoar");
                    if(savedEquipment.containsKey(player.getName())){
                        Equipment equipment = savedEquipment.get(player.getName());
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(new ItemStack[4]);
                        player.getInventory().setContents(equipment.getItems());
                        player.getInventory().setArmorContents(equipment.getArmour());

                    }


                }
                event.setCancelled(true);
            }
            skull.update();



        }
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

            // Load any saved inventory from Reids
            if (player.isOp()) {
                Jedis jedis = new Jedis("127.0.0.1");
                if (jedis.exists(player.getName() + ".items")) {
                    Map<String, String> items = jedis.hgetAll(player.getName() + ".items");
                    Map<String, String> armour = null;
                    if (jedis.exists(player.getName() + ".armour")) {
                        armour = jedis.hgetAll(player.getName() + ".armour");
                    }
                    savedEquipment.put(player.getName() , new Equipment(player.getName() , items, armour));
                }

            }

            activePlayers.put(player.getName(), newPlayerData);
        }
    }
}
