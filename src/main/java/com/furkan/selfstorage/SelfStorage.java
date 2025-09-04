package com.furkan.selfstorage;

import org.bukkit.plugin.java.JavaPlugin;

public class SelfStorage extends JavaPlugin {

    private static SelfStorage instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private GuiManager guiManager;
    private VaultManager vaultManager;

    @Override
    public void onEnable() {
        instance = this;

        // Önce Vault'u ve konfigürasyonu yükle
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        vaultManager = new VaultManager(this);

        // Veritabanını başlat
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        
        // GUI Yöneticisini başlat
        guiManager = new GuiManager(this);

        // Komutları ve Listener'ları kaydet
        this.getCommand("storage").setExecutor(new StorageCommand(this));
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        getLogger().info("SelfStorage plugin'i aktif edildi!");

        // Süre kontrol görevini başlat
        startStorageCheckerTask();
    }

    @Override
    public void onDisable() {
        // Bukkit görevlerini iptal et
        getServer().getScheduler().cancelTasks(this);

        // Veritabanı bağlantısını kapat
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("SelfStorage plugin'i devre dışı bırakıldı!");
    }

    private void startStorageCheckerTask() {
        long intervalTicks = configManager.getCheckIntervalSeconds() * 20L; // Saniyeyi tick'e çevir
        new StorageChecker(this).runTaskTimerAsynchronously(this, intervalTicks, intervalTicks);
        getLogger().info("Depo süre kontrolcüsü her " + configManager.getCheckIntervalSeconds() + " saniyede bir çalışacak.");
    }

    public static SelfStorage getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public GuiManager getGuiManager() {
        return guiManager;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }
}
