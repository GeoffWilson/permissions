package com.pigletcraft.permissions;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

/**
 * This class will store the players inventory while they are op, this includes server reloads
 *
 * @author Ben Carvell
 * @author Geoff Wilson
 */
public class Equipment {
    private ItemStack[] items;
    private ItemStack[] armour;

    public Equipment() {}

    public Equipment(String playerName, FileConfiguration config) {

        try {
            config.load("op-inv.yml");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        String playerItemKey = String.format("%s.items", playerName);
        String playerArmourKey = String.format("%s.armour", playerName);

        List<ItemStack> itemList = (List<ItemStack>) config.get(playerItemKey);
        List<ItemStack> armourList = (List<ItemStack>) config.get(playerArmourKey);

        this.items = itemList.toArray(new ItemStack[itemList.size()]);
        this.armour = armourList.toArray(new ItemStack[armourList.size()]);
    }

    public ItemStack[] getItems() {
        return items;
    }

    public void setItems(ItemStack[] items) {
        this.items = items;
    }

    public ItemStack[] getArmour() {
        return armour;
    }

    public void setArmour(ItemStack[] armour) {
        this.armour = armour;
    }

    public void save(String playerName, FileConfiguration config) {

        String itemKey = String.format("%s.items", playerName);
        String armourKey = String.format("%s.armour", playerName);

        if (items != null) config.set(itemKey, items);
        if (armour != null) config.set(armourKey, armour);

        try {
            config.save("op-inv.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


