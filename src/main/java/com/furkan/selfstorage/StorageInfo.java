package com.furkan.selfstorage;

import java.util.UUID;

public class StorageInfo {
    private final UUID playerUUID;
    private final long expireTime;

    public StorageInfo(UUID playerUUID, long expireTime) {
        this.playerUUID = playerUUID;
        this.expireTime = expireTime;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public long getExpireTime() {
        return expireTime;
    }
}
