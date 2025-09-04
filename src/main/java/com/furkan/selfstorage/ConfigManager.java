package com.furkan.selfstorage;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final SelfStorage plugin;
    private FileConfiguration config;

    public ConfigManager(SelfStorage plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        // Bu metod, pluginin ana sınıfından çağrılacak
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        plugin.reloadConfig(); // Değişikliklerin anında yansıması için
    }

    public String getMenuTitle() {
        return config.getString("gui.menutitle", "§6Depo Menüsü");
    }

    public int getStorageSize(String duration) {
        return config.getInt("depo.boyutlar." + duration, 5);
    }

    public double getStoragePrice(String duration) {
        return config.getDouble("depo.fiyatlar." + duration, 100.0);
    }
    
    public int getMaxCapacity() {
        return config.getInt("depo.max_kapasite", 54);
    }

    public int getCheckIntervalSeconds() {
        return config.getInt("zaman.kontrol_periyodu_saniye", 600);
    }

    public String getGUItemMaterial(String itemKey) {
        return config.getString("gui.itemlar." + itemKey + ".malzeme", "CHEST");
    }

    public String getGUItemName(String itemKey) {
        return config.getString("gui.itemlar." + itemKey + ".isim", "İsimsiz Buton");
    }
}
