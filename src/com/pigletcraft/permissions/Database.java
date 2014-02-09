package com.pigletcraft.permissions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class Database {

    public ChatColor getChatColor(String playerName) {

        Properties prop = new Properties();

        try {
            InputStream input = new FileInputStream("permissions.properties");
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ChatColor chatColor = ChatColor.WHITE;

        try {
            String connectionString = "jdbc:mysql://localhost/piglet";
            Connection connection = DriverManager.getConnection(connectionString, prop.getProperty("username"), prop.getProperty("password"));
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
