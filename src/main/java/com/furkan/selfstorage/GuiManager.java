package com.furkan.selfstorage;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiManager {

    public static final String STORAGE_TITLE_PREFIX = "§8Depo:";
    public static final String BUY_MENU_TITLE = "§6Depo Satın Al";
    private final SelfStorage plugin;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final VaultManager vaultManager;

    public GuiManager(SelfStorage plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.databaseManager = plugin.getDatabaseManager();
        this.vaultManager = plugin.getVaultManager();
    }

    public void openMainMenu(Player player) {
        String menuTitle = configManager.getMenuTitle();
        Inventory mainMenu = Bukkit.createInventory(null, 27, menuTitle);

        mainMenu.setItem(10, createGuiItem("satin_al"));
        mainMenu.setItem(12, createGuiItem("depo_gor"));
        mainMenu.setItem(14, createGuiItem("sure_uzat"));
        mainMenu.setItem(16, createGuiItem("depo_sil"));

        player.openInventory(mainMenu);
    }

    public void openBuyMenu(Player player) {
        Inventory buyMenu = Bukkit.createInventory(null, 27, BUY_MENU_TITLE);
        ConfigurationSection prices = plugin.getConfig().getConfigurationSection("depo.fiyatlar");
        if (prices != null) {
            int slot = 10;
            for (String tier : prices.getKeys(false)) {
                buyMenu.setItem(slot, createBuyMenuItem(tier));
                slot += 2;
            }
        }
        player.openInventory(buyMenu);
    }

    public void openStorageGUI(Player viewer, org.bukkit.OfflinePlayer owner) {
        if (!databaseManager.hasStorage(owner.getUniqueId())) {
            viewer.sendMessage("§cBu oyuncunun deposu bulunmuyor.");
            return;
        }

        int storageSize = configManager.getMaxCapacity();
        
        // Envanter başlığına sahibin UUID'sini gizli bir şekilde ekliyoruz.
        // Bu, daha sonra envanter kapatıldığında doğru kişiye kaydetmek için kullanılacak.
        String hiddenOwnerUUID = "§f§f" + owner.getUniqueId().toString();
        String storageTitle = STORAGE_TITLE_PREFIX + " " + owner.getName() + hiddenOwnerUUID;

        Inventory storageInv = Bukkit.createInventory(null, storageSize, storageTitle);

        ItemStack[] items = databaseManager.getStorageItems(owner.getUniqueId());
        if (items.length > 0) {
            storageInv.setContents(items);
        }

        viewer.openInventory(storageInv);
    }

    private ItemStack createGuiItem(String key) {
        String materialName = configManager.getGUItemMaterial(key).toUpperCase();
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            material = Material.STONE;
        }
        String name = configManager.getGUItemName(key);

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList("§7İşlem yapmak için tıkla!"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBuyMenuItem(String tier) {
        double price = configManager.getStoragePrice(tier);
        // Tier'e göre materyal belirleme (isteğe bağlı, görsel zenginlik için)
        Material material = Material.CHEST;
        if (tier.equals("3g")) material = Material.TRAPPED_CHEST;
        if (tier.equals("1w")) material = Material.ENDER_CHEST;

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a" + tier + " süreli depo");
            List<String> lore = new ArrayList<>();
            lore.add("§7Fiyat: §e" + vaultManager.format(price));
            // lore.add("§7Boyut: §b" + configManager.getStorageSize(tier) + "x" + configManager.getStorageSize(tier));
            lore.add(" ");
            lore.add("§6Satın almak için tıkla!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
