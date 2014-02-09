package com.pigletcraft.permissions;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsPlugin extends JavaPlugin implements Listener {

    private final String ADMIN_HEAD_SKIN = "BillyLeBoar";
    private ConcurrentHashMap<String, PlayerData> activePlayers;
    private ConcurrentHashMap<String, Equipment> savedEquipment;
    private ArrayList<String> serverOps;

    @Override
    public void onEnable() {

        // Create the necessary objects for the plugin
        activePlayers = new ConcurrentHashMap<>();
        savedEquipment = new ConcurrentHashMap<>();

        // This is the list of users allow to become op
        serverOps = new ArrayList<>();
        serverOps.add("GeoffWilson");
        serverOps.add("Benshiro");
        serverOps.add("BillyLeBoar");

        // if this has been caused by /reload then load we fake a PlayerJoinEvent
        for (Player player : getServer().getOnlinePlayers()) {
            PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(player, "Welcome");
            onPlayerJoin(playerJoinEvent);
        }

        // Register as an event handler
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

        // Save all the op inventories to disk
        for (String playerName : savedEquipment.keySet()) {
            Equipment equipment = savedEquipment.get(playerName);
            equipment.save(playerName, this.getConfig());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (serverOps.contains(player.getName())) {

            Action action = event.getAction();
            Block clickedBlock = event.getClickedBlock();

            if (player.getItemInHand().getType() == Material.BLAZE_ROD && player.isOp()) {

                switch (action) {
                    case LEFT_CLICK_AIR:
                    case LEFT_CLICK_BLOCK:
                        player.performCommand("j");
                        break;

                    case RIGHT_CLICK_AIR:
                    case RIGHT_CLICK_BLOCK:
                        player.performCommand("thru");

                        break;
                }
                event.setCancelled(true);

            } else {

                // Break if either of these conditions are true
                if (action != Action.LEFT_CLICK_BLOCK || clickedBlock.getType() != Material.SKULL) return;

                // We know this can be cast to skull from the above check
                Skull skull = (Skull) event.getClickedBlock().getState();

                if (skull.getOwner().equals(ADMIN_HEAD_SKIN) && !player.isOp()) {

                    // Configure the player for op
                    player.setOp(true);
                    player.setGameMode(GameMode.CREATIVE);

                    player.sendMessage(ChatColor.RED + "You are now op!");

                    // Set the admin head to the players skin
                    skull.setOwner(player.getName());

                    // Save the players inventory
                    Equipment equipment = new Equipment();
                    equipment.setArmour(player.getInventory().getArmorContents().clone());
                    equipment.setItems(player.getInventory().getContents().clone());
                    savedEquipment.put(player.getName(), equipment);

                    // Clear the players current inventory
                    player.getInventory().clear();
                    player.getInventory().setArmorContents(new ItemStack[4]);

                    // Put the admin skull on the player and add Op tools
                    ItemStack skullHead = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                    ItemStack wand = new ItemStack(Material.WOOD_AXE);
                    ItemStack teleporter = new ItemStack(Material.BLAZE_ROD);

                    SkullMeta skullMeta = (SkullMeta) skullHead.getItemMeta();
                    skullMeta.setOwner(ADMIN_HEAD_SKIN);
                    skullHead.setItemMeta(skullMeta);
                    ItemMeta teleMeta = teleporter.getItemMeta();
                    ItemMeta wandMeta = wand.getItemMeta();
                    teleMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Teleporter");
                    wandMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Wand");
                    teleporter.setItemMeta(teleMeta);
                    wand.setItemMeta(wandMeta);

                    player.getInventory().setHelmet(skullHead);
                    player.getInventory().setItem(0, teleporter);
                    player.getInventory().setItem(1, wand);

                    // Cancel any block break event
                    event.setCancelled(true);

                } else {

                    if (player.isOp() && skull.getOwner().equals(player.getName())) {

                        // Configure the player for standard game play
                        player.setOp(false);
                        player.setGameMode(GameMode.SURVIVAL);

                        player.sendMessage(ChatColor.RED + "You are no longer op!");

                        // Set the admin head back to the admin skin
                        skull.setOwner(ADMIN_HEAD_SKIN);

                        // Clear the players current inventory
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(new ItemStack[4]);

                        // if we have saved inventory for the player then restore
                        if (savedEquipment.containsKey(player.getName())) {

                            Equipment equipment = savedEquipment.get(player.getName());
                            player.getInventory().setContents(equipment.getItems());
                            player.getInventory().setArmorContents(equipment.getArmour());
                        }
                    }

                    // Cancel any block break event
                    if(serverOps.contains(skull.getOwner())){
                    event.setCancelled(true);
                    }
                }

                // Send the update for the skull block in the world
                skull.update();

            }
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

            if (player.isOp()) {
                savedEquipment.put(player.getName(), new Equipment(player.getName(), this.getConfig()));
            }

            activePlayers.put(player.getName(), newPlayerData);
        }
    }

    private class PlayerData {
        public PermissionAttachment permissions;
        public ChatColor chatColor;
    }
}
