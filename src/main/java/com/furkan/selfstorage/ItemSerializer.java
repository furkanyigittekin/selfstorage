package com.furkan.selfstorage;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemSerializer {

    /**
     * Bir envanterin içeriğini Base64 string'ine çevirir.
     * @param items Kaydedilecek ItemStack dizisi
     * @return Base64 formatında şifrelenmiş metin
     * @throws IllegalStateException Veri yazılamazsa
     */
    public static String toBase64(ItemStack[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Eşyalar serileştirilemedi!", e);
        }
    }

    /**
     * Base64 string'ini bir envanter içeriğine (ItemStack dizisi) çevirir.
     * @param data Çözülecek Base64 metni
     * @return ItemStack dizisi
     * @throws IOException Veri okunamzsa
     */
    public static ItemStack[] fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Eşyalar serileştirilemedi!", e);
        }
    }
}
