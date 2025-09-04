package com.furkan.selfstorage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class StorageCommand implements CommandExecutor {

    private final SelfStorage plugin;
    private final DatabaseManager dbManager;
    private final ConfigManager configManager;
    private final VaultManager vaultManager;

    public StorageCommand(SelfStorage plugin) {
        this.plugin = plugin;
        this.dbManager = plugin.getDatabaseManager();
        this.configManager = plugin.getConfigManager();
        this.vaultManager = plugin.getVaultManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("selfstorage.use")) {
            player.sendMessage("§cBu komutu kullanma yetkiniz yok.");
            return true;
        }

        if (args.length == 0) {
            plugin.getGuiManager().openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "buy":
                handleBuyCommand(player, args);
                break;
            case "admin":
                handleAdminCommand(player, args);
                break;
            case "remove":
                handleRemoveCommand(player);
                break;
            default:
                player.sendMessage("§cGeçersiz komut. Kullanım: /storage [buy|remove|admin]");
                break;
        }

        return true;
    }

    private void handleAdminCommand(Player admin, String[] args) {
        if (!admin.hasPermission("selfstorage.admin")) {
            admin.sendMessage("§cBu komutu kullanma yetkiniz yok.");
            return;
        }
        if (args.length < 3 || !args[1].equalsIgnoreCase("view")) {
            admin.sendMessage("§cKullanım: /storage admin view <oyuncu>");
            return;
        }

        String targetPlayerName = args[2];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);

        if (targetPlayer == null || !dbManager.hasStorage(targetPlayer.getUniqueId())) {
            admin.sendMessage("§cBelirtilen oyuncu bulunamadı veya deposu yok.");
            return;
        }

        plugin.getGuiManager().openStorageGUI(admin, targetPlayer);
    }

    private void handleBuyCommand(Player player, String[] args) {
        // ... (code is the same)
        if (args.length < 2) {
            player.sendMessage("§cKullanım: /storage buy <1g|3g|1w>");
            return;
        }
        if (dbManager.hasStorage(player.getUniqueId())) {
            player.sendMessage("§cZaten bir deponuz var!");
            return;
        }
        String tier = args[1].toLowerCase();
        double price = configManager.getStoragePrice(tier);
        if (price <= 0) {
            player.sendMessage("§cGeçersiz depo süresi! Mevcut süreler: 1g, 3g, 1w");
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

    private void handleRemoveCommand(Player player) {
        if (!dbManager.hasStorage(player.getUniqueId())) {
            player.sendMessage("§cSilinecek bir deponuz bulunmuyor.");
            return;
        }

        dbManager.deleteStorage(player.getUniqueId());
        player.sendMessage("§aDeponuz ve içindeki tüm eşyalar başarıyla silindi.");
    }
}
