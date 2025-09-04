package com.furkan.selfstorage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class StorageChecker extends BukkitRunnable {

    private final SelfStorage plugin;
    private final DatabaseManager dbManager;

    public StorageChecker(SelfStorage plugin) {
        this.plugin = plugin;
        this.dbManager = plugin.getDatabaseManager();
    }

    @Override
    public void run() {
        List<StorageInfo> storages = dbManager.getAllStorages();
        long currentTime = System.currentTimeMillis();
        int expiredCount = 0;

        for (StorageInfo storage : storages) {
            if (storage.getExpireTime() < currentTime) {
                // Depo süresi dolmuş
                UUID playerUUID = storage.getPlayerUUID();
                dbManager.deleteStorage(playerUUID);
                expiredCount++;

                // Oyuncu online ise bilgilendir
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    player.sendMessage("§cDeponuzun süresi dolduğu için silinmiştir!");
                }
            }
        }

        if (expiredCount > 0) {
            plugin.getLogger().info(expiredCount + " adet süresi dolan depo silindi.");
        }
    }
}
