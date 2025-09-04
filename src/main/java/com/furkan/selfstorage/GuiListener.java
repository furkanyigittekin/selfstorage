package com.furkan.selfstorage;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GuiListener implements Listener {

    // ... (fields and constructor are the same)
    private final SelfStorage plugin;
    private final ConfigManager configManager;
    private final GuiManager guiManager;
    private final DatabaseManager dbManager;
    private final VaultManager vaultManager;

    public GuiListener(SelfStorage plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.guiManager = plugin.getGuiManager();
        this.dbManager = plugin.getDatabaseManager();
        this.vaultManager = plugin.getVaultManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String viewTitle = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();

        // Ana Menü
        if (viewTitle.equals(configManager.getMenuTitle())) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;
            String itemName = clickedItem.getItemMeta().getDisplayName();

            if (itemName.equals(configManager.getGUItemName("depo_gor"))) {
                player.closeInventory();
                guiManager.openStorageGUI(player, player); // Kendisini sahip olarak belirt
            } else if (itemName.equals(configManager.getGUItemName("satin_al"))) {
                guiManager.openBuyMenu(player);
            } else if (itemName.equals(configManager.getGUItemName("depo_sil"))) {
                player.closeInventory();
                if (!dbManager.hasStorage(player.getUniqueId())) {
                    player.sendMessage("§cSilinecek bir deponuz bulunmuyor.");
                    return;
                }
                dbManager.deleteStorage(player.getUniqueId());
                player.sendMessage("§aDeponuz ve içindeki tüm eşyalar başarıyla silindi.");
            }
        // Satın Alma Menüsü
        } else if (viewTitle.equals(GuiManager.BUY_MENU_TITLE)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String tier = clickedItem.getItemMeta().getDisplayName().replaceAll("§[a-z0-9]", "").split(" ")[0];
            player.closeInventory();
            purchaseStorage(player, tier);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String viewTitle = event.getView().getTitle();
        if (viewTitle.startsWith(GuiManager.STORAGE_TITLE_PREFIX)) {
            // Başlıktan gizlenmiş UUID'yi çıkar
            String rawTitle = viewTitle.replaceAll("§[a-z0-9]", "");
            String[] parts = rawTitle.split(" ");
            String ownerUUIDString = viewTitle.substring(viewTitle.lastIndexOf("§f§f") + 4);
            
            try {
                UUID ownerUUID = UUID.fromString(ownerUUIDString);
                dbManager.saveStorageItems(ownerUUID, event.getInventory().getContents());

                Player closer = (Player) event.getPlayer();
                // Sadece sahibi kendi envanterini kapattığında mesaj gönder
                if (closer.getUniqueId().equals(ownerUUID)) {
                    closer.sendMessage("§aDepo içeriğiniz kaydedildi.");
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Depo kaydederken başlıkta geçerli bir UUID bulunamadı: " + viewTitle);
            }
        }
    }

    // ... (purchaseStorage and parseDuration methods are the same)
    private void purchaseStorage(Player player, String tier) {
        if (dbManager.hasStorage(player.getUniqueId())) {
            player.sendMessage("§cZaten bir deponuz var!");
            return;
        }
        double price = configManager.getStoragePrice(tier);
        if (price <= 0) {
            player.sendMessage("§cGeçersiz depo süresi!");
            return;
        }
        if (!vaultManager.hasEnoughMoney(player, price)) {
            player.sendMessage("§cYeterli paranız yok! Gereken: " + vaultManager.format(price));
            return;
        }
        if (vaultManager.withdrawMoney(player, price)) {
            long expireTime = System.currentTimeMillis() + parseDuration(tier);
            dbManager.createStorage(player.getUniqueId(), tier, expireTime);
            player.sendMessage("§aBaşarıyla " + vaultManager.format(price) + " karşılığında bir depo kiraladınız!");
        } else {
            player.sendMessage("§cÖdeme sırasında bir hata oluştu.");
        }
    }

    private long parseDuration(String duration) {
        switch (duration) {
            case "1g": return TimeUnit.DAYS.toMillis(1);
            case "3g": return TimeUnit.DAYS.toMillis(3);
            case "1w": return TimeUnit.DAYS.toMillis(7);
            default: return 0;
        }
    }
}
