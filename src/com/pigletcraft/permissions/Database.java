package com.pigletcraft.permissions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Database {

    private final String connectionString = "jdbc:mysql://localhost/piglet";
    private final String username = "";
    private final String password = "";

    public ChatColor getChatColor(String playerName) {

        ChatColor chatColor = ChatColor.WHITE;

        try {
            Connection connection = DriverManager.getConnection(connectionString, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT chat_color FROM minecraft_user WHERE name = ?");
            statement.setString(1, playerName);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                chatColor = ChatColor.valueOf(result.getString("chat_color"));
            }

            connection.close();
            return chatColor;

        } catch (Exception e) {
            Bukkit.getLogger().info("exception in getChatColor() " + e.getLocalizedMessage());
        }

        return ChatColor.WHITE;
    }
}
