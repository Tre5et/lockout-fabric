package me.marin.lockout.server.goal.hint;

import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import me.marin.lockout.lockout.goal.hint.PositionHint;
import me.marin.lockout.server.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import oshi.util.tuples.Pair;

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
        ItemStack hintItem = null;
        if(ServerConfig.getInstance().getHintMode() == ServerConfig.HintMode.COMPASS) hintItem = createCompass(result, Component.literal(name), server);
        if(ServerConfig.getInstance().getHintMode() == ServerConfig.HintMode.MAP) hintItem = createExplorerMap(result, decoration, Component.literal(name), server);
        if(hintItem == null) return;

        CompoundTag identifierTag = new CompoundTag();
        identifierTag.put("hint_name", StringTag.valueOf(name));
        identifierTag.put("hint_result", StringTag.valueOf(getHintResultId(result)));
        hintItem.set(DataComponents.CUSTOM_DATA, CustomData.of(identifierTag));

        replaceHintItem(result, player, hintItem);
    }

    public void replaceHintItem(Pair<BlockPos,Identifier> result, ServerPlayer player, ItemStack newItem) {
        Optional<Boolean> offhandMatch = matchItemToHint(player.getOffhandItem(), result);
        if(offhandMatch.isPresent()) {
            if(!offhandMatch.get()) player.setItemInHand(InteractionHand.OFF_HAND, newItem);
            return;
        }

        Inventory inventory = player.getInventory();
        for(int i = 0; i < 36; i++) {
            Optional<Boolean> match = matchItemToHint(inventory.getItem(i), result);
            if(match.isPresent()) {
                if(!match.get()) inventory.setItem(i, newItem);
                return;
            }
        }

        player.addItem(newItem);
    }

    private Optional<Boolean> matchItemToHint(ItemStack stack, Pair<BlockPos,Identifier> result) {
        if(stack == null || !stack.has(DataComponents.CUSTOM_DATA)) return Optional.empty();
        CompoundTag data = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        if(!data.getStringOr("hint_name", "").equals(name)) return Optional.empty();
        if(!stack.getItem().equals(ServerConfig.getInstance().getHintMode().getItemType())) return Optional.of(false);
        return Optional.of(data.getStringOr("hint_result", "").equals(getHintResultId(result)));
    }

    private String getHintResultId(Pair<BlockPos,Identifier> result) {
        return result.getA().getX() + ";" + result.getA().getY() + ";" + result.getA().getZ() + ";" + result.getB().toString();
    }

    public static ItemStack createExplorerMap(Pair<BlockPos,Identifier> result, Holder<MapDecorationType> decoration, Component name, MinecraftServer server) {
        ItemStack itemStack = Items.FILLED_MAP.getDefaultInstance();
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, result.getB()));

        BlockPos position = result.getA();
        MapItem.applyNewSavedData(level, itemStack, position.getX(), position.getZ(), (byte)2, true, true);
        MapItem.renderBiomePreviewMap(level, itemStack);
        MapItemSavedData.addTargetDecoration(itemStack, position, "hint_target", decoration);
        itemStack.set(DataComponents.ITEM_NAME, Component.empty().append(name).append(" Hint Map"));
        return itemStack;
    }

    public static ItemStack createCompass(Pair<BlockPos,Identifier> result, Component name, MinecraftServer server) {
        ItemStack itemStack = Items.COMPASS.getDefaultInstance();
        ResourceKey<Level> level = server.getLevel(ResourceKey.create(Registries.DIMENSION, result.getB())).dimension();
        LodestoneTracker target = new LodestoneTracker(Optional.of(new GlobalPos(level, result.getA())), false);

        itemStack.set(DataComponents.LODESTONE_TRACKER, target);
        itemStack.set(DataComponents.ITEM_NAME, Component.empty().append(name).append(" Hint Compass"));
        return itemStack;
    }
}
