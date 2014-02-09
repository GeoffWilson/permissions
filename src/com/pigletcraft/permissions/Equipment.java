package com.pigletcraft.permissions;

import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Benshiro on 08/02/14.
 */
public class Equipment {
    private ItemStack[] items;
    private ItemStack[] armour;

    public Equipment(){}

    public Equipment(String playerName, Map<String, String> items, Map<String, String> armour) {

        String playerItemKey = String.format("%s.items", playerName);
        String playerArmourKey = String.format("%s.armour", playerName);

        ArrayList<ItemStack> itemList = new ArrayList<>();
        ArrayList<ItemStack> armourList = new ArrayList<> ();

        Jedis jedis = new Jedis("127.0.0.1");
        for (String key : items.keySet()) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(jedis.hget(playerItemKey.getBytes(), key.getBytes()));
                ObjectInput input = new ObjectInputStream(inputStream);
                ItemStack item = ItemStack.deserialize((Map<String, Object>) input.readObject());
                itemList.add(item);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (armour != null) {
            for (String key : armour.keySet()) {
                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(jedis.hget(playerArmourKey.getBytes(), key.getBytes()));
                    ObjectInput input = new ObjectInputStream(inputStream);
                    ItemStack item = ItemStack.deserialize((Map<String, Object>) input.readObject());
                    armourList.add(item);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

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

    public void save(String playerName) {

        Jedis jedis = new Jedis("127.0.0.1");
        jedis.del(playerName + ".items", playerName + ".armour");

        for (int i = 0; i < items.length; i++) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream);
                objectOutput.writeObject(items[i].serialize());
                jedis.hset((playerName + ".items").getBytes(),String.valueOf(i).getBytes(),byteArrayOutputStream.toByteArray());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        i
        for (int i = 0; i < armour.length; i++) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream);
                objectOutput.writeObject(armour[i].serialize());
                jedis.hset((playerName + ".armour").getBytes(),String.valueOf(i).getBytes(),byteArrayOutputStream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}


