package com.furkan.selfstorage;

import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private final SelfStorage plugin;
    private Connection connection;

    public DatabaseManager(SelfStorage plugin) {
        this.plugin = plugin;
    }

    public void connect() { 
        // ... (existing connect code) ...
        try {
            File dbFile = new File(plugin.getDataFolder(), "storage.db");
            if (!dbFile.exists()) {
                dbFile.getParentFile().mkdirs();
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            plugin.getLogger().info("SQLite veritabanı bağlantısı başarılı.");
            createTables();
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite veritabanı bağlantısı kurulamadı!");
            e.printStackTrace();
        }
    }

    private void createTables() {
        // ... (existing createTables code) ...
        String sql = "CREATE TABLE IF NOT EXISTS storages (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "player_uuid TEXT NOT NULL UNIQUE," +
                     "storage_tier TEXT NOT NULL," +
                     "expire_time LONG NOT NULL," +
                     "items TEXT" + // Serialized items
                     ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Veritabanı tabloları oluşturulamadı!");
            e.printStackTrace();
        }
    }

    public boolean hasStorage(UUID playerUUID) {
        String sql = "SELECT id FROM storages WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createStorage(UUID playerUUID, String tier, long expireTime) {
        String sql = "INSERT INTO storages(player_uuid, storage_tier, expire_time, items) VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, tier);
            pstmt.setLong(3, expireTime);
            pstmt.setString(4, ""); // Initially empty
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ItemStack[] getStorageItems(UUID playerUUID) {
        String sql = "SELECT items FROM storages WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String base64Items = rs.getString("items");
                if (base64Items != null && !base64Items.isEmpty()) {
                    return ItemSerializer.fromBase64(base64Items);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return new ItemStack[0]; // Return empty array if no items or error
    }

    public void saveStorageItems(UUID playerUUID, ItemStack[] items) {
        String sql = "UPDATE storages SET items = ? WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String base64Items = ItemSerializer.toBase64(items);
            pstmt.setString(1, base64Items);
            pstmt.setString(2, playerUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteStorage(UUID playerUUID) {
        String sql = "DELETE FROM storages WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public String getStorageTier(UUID playerUUID) {
        String sql = "SELECT storage_tier FROM storages WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("storage_tier");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.List<StorageInfo> getAllStorages() {
        java.util.List<StorageInfo> storages = new java.util.ArrayList<>();
        String sql = "SELECT player_uuid, expire_time FROM storages";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                long expireTime = rs.getLong("expire_time");
                storages.add(new StorageInfo(uuid, expireTime));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return storages;
    }

    public void close() {
        // ... (existing close code) ...
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
