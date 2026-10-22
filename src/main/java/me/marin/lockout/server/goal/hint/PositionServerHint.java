package me.marin.lockout.server.goal.hint;

import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import me.marin.lockout.lockout.goal.hint.PositionHint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import oshi.util.tuples.Pair;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public abstract class PositionServerHint extends PositionHint implements ServerHint<Pair<BlockPos,Identifier>> {
    private final String name;
    private final Holder<MapDecorationType> decoration;
    private final List<ResourceKey<Level>> applicableLevels;

    public PositionServerHint(String name, Holder<MapDecorationType> decoration, List<ResourceKey<Level>> applicableLevels) {
        this.name = name;
        this.decoration = decoration;
        this.applicableLevels = applicableLevels;
    }

    protected abstract GoalHintResult<Pair<BlockPos,Identifier>> resolveWithLevel(MinecraftServer server, ServerPlayer player, ServerLevel level);

    @Override
    public GoalHintResult<Pair<BlockPos,Identifier>> resolve(MinecraftServer server, ServerPlayer player) {
        Optional<ResourceKey<Level>> level = applicableLevels.stream().filter(player.level().dimension()::equals).findAny();
        if(level.isEmpty()) {
            return GoalHintResult.error("You are not in the correct dimension.");
        }

        ServerLevel serverLevel = server.getLevel(level.get());
        if(serverLevel == null) {
            return GoalHintResult.error("The current player dimension " + level.get() + " does not exist.");
        }
        return resolveWithLevel(server, player, serverLevel);
    }

    @Override
    public void onPropagateToPlayer(Pair<BlockPos,Identifier> result, MinecraftServer server, ServerPlayer player) {
        Component itemName = Component.literal(name + " Hint Map");
        if(player.getInventory().contains(s -> s.getItem() == Items.FILLED_MAP && s.getItemName().getString().equals(itemName.getString()))) {
            return;
        }

        player.addItem(createExplorerMap(result, decoration, itemName, server));
    }

    public static ItemStack createExplorerMap(Pair<BlockPos,Identifier> result, Holder<MapDecorationType> decoration, Component name, MinecraftServer server) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, result.getB()));

        BlockPos position = result.getA();
        MapItem.applyNewSavedData(level, itemStack, position.getX(), position.getZ(), (byte)2, true, true);
        MapItem.renderBiomePreviewMap(level, itemStack);
        MapItemSavedData.addTargetDecoration(itemStack, position, "hint_target", decoration);
        itemStack.set(DataComponents.ITEM_NAME, name);
        return itemStack;
    }
}
