package me.marin.lockout;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class CompassItemHandler {

    public static boolean isCompass(ItemStack item) {
        return item != null &&
                item.getItem() == Items.COMPASS &&
                Optional.ofNullable(item.get(DataComponents.CUSTOM_DATA)).filter(customData -> customData.copyTag().contains("PlayerTracker")).isPresent();
    }

    public final List<UUID> players = new ArrayList<>();
    public final Map<UUID, String> playerNames = new HashMap<>();
    public final Map<UUID, Integer> currentSelection = new HashMap<>();
    public final Map<UUID, Integer> compassSlots = new HashMap<>();

    public CompassItemHandler(List<UUID> players, PlayerList playerManager) {
        for (int i = 0; i < players.size(); i++) {
            UUID playerId = players.get(i);
            this.players.add(playerId);
            this.playerNames.put(playerId, playerManager.getPlayer(playerId).getName().getString());

            this.currentSelection.put(playerId, i == 0 ? 1 : 0);
        }
    }

    public void cycle(Player player) {
        if (!currentSelection.containsKey(player.getUUID())) return;
        int cur = currentSelection.get(player.getUUID());
        int next = (cur + 1) % players.size();
        if (players.get(next).equals(player.getUUID())) {
            next = (next + 1) % players.size();
        }
        currentSelection.put(player.getUUID(), next);
    }

    public void removePlayer(UUID player) {
        if (!players.contains(player)) return;
        int index = players.indexOf(player);
        players.remove(player);
        playerNames.remove(player);

        // Update selections for other players
        for (UUID uuid : currentSelection.keySet()) {
            int selected = currentSelection.get(uuid);
            if (selected == index) {
                // If they were selecting the removed player, cycle to next
                if (players.isEmpty()) {
                    currentSelection.put(uuid, 0); // Should handle empty list gracefully elsewhere if needed
                } else {
                    int next = selected % players.size(); // Wrap around just in case
                    if (players.get(next).equals(uuid)) {
                         next = (next + 1) % players.size();
                    }
                    currentSelection.put(uuid, next);
                }
            } else if (selected > index) {
                // Shift down if above
                currentSelection.put(uuid, selected - 1);
            }
        }
        currentSelection.remove(player);
    }

    public ItemStack newCompass() {
        ItemStack compass = Items.COMPASS.getDefaultInstance();
        CompoundTag compound = new CompoundTag();
        compound.putString("PlayerTracker", UUID.randomUUID().toString());
        compass.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
        return compass;
    }

}
