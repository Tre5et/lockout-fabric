package me.marin.lockout;

import me.marin.lockout.lockout.DefaultGoalRegister;
import me.marin.lockout.lockout.goal.config.GoalPoolConfig;
import me.marin.lockout.network.Networking;
import me.marin.lockout.server.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.Objects;

import static me.marin.lockout.Constants.*;

public class LockoutInitializer implements ModInitializer {
    public static Version MOD_VERSION;

    @Override
    public void onInitialize() {
        MOD_VERSION = FabricLoader.getInstance().getModContainer(NAMESPACE).get().getMetadata().getVersion();

        LockoutConfig.load();
        Networking.registerPayloads();
        DefaultGoalRegister.registerGoals();
        GoalPoolConfig.load();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.getRoot().addChild(Command.SERVER_COMMAND);
        });

        LootTableEvents.REPLACE.register(((key, original, source, registries) -> {
            if (Objects.equals(key, BuiltInLootTables.PIGLIN_BARTERING)) {
                Holder<ContextIntProvider> ironNuggetsCount = ContextIntProviders.between(9, 36);
                Holder<ContextIntProvider> quartzCount = ContextIntProviders.between(8, 16);
                Holder<ContextIntProvider> glowstoneDustCount = ContextIntProviders.between(5, 12);
                Holder<ContextIntProvider> magmaCreamCount = ContextIntProviders.between(2, 6);
                Holder<ContextIntProvider> enderPearlCount = ContextIntProviders.between(4, 8);
                Holder<ContextIntProvider> stringCount = ContextIntProviders.between(8, 24);
                Holder<ContextIntProvider> fireChargeCount = ContextIntProviders.between(1, 5);
                Holder<ContextIntProvider> gravelCount = ContextIntProviders.between(8, 16);
                Holder<ContextIntProvider> leatherCount = ContextIntProviders.between(4, 10);
                Holder<ContextIntProvider> netherBrickCount = ContextIntProviders.between(4, 16);
                Holder<ContextIntProvider> cryingObsidianCount = ContextIntProviders.between(1, 3);
                Holder<ContextIntProvider> soulSandCount = ContextIntProviders.between(4, 16);

                LootPool pool = LootPool.lootPool()
                        .add(LootItem.lootTableItem(Items.BOOK).apply(EnchantRandomlyFunction.randomEnchantment().withEnchantment(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SOUL_SPEED))).setWeight(5))
                        .add(LootItem.lootTableItem(Items.IRON_BOOTS).apply(EnchantRandomlyFunction.randomEnchantment().withEnchantment(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SOUL_SPEED))).setWeight(8))
                        .add(LootItem.lootTableItem(Items.POTION).apply(SetPotionFunction.setPotion(Potions.FIRE_RESISTANCE)).setWeight(10))
                        .add(LootItem.lootTableItem(Items.SPLASH_POTION).apply(SetPotionFunction.setPotion(Potions.FIRE_RESISTANCE)).setWeight(10))
                        .add(LootItem.lootTableItem(Items.IRON_NUGGET).apply(SetItemCountFunction.setCount(ironNuggetsCount)).setWeight(10))
                        .add(LootItem.lootTableItem(Items.QUARTZ).apply(SetItemCountFunction.setCount(quartzCount)).setWeight(20))
                        .add(LootItem.lootTableItem(Items.GLOWSTONE_DUST).apply(SetItemCountFunction.setCount(glowstoneDustCount)).setWeight(20))
                        .add(LootItem.lootTableItem(Items.MAGMA_CREAM).apply(SetItemCountFunction.setCount(magmaCreamCount)).setWeight(20))
                        .add(LootItem.lootTableItem(Items.ENDER_PEARL).apply(SetItemCountFunction.setCount(enderPearlCount)).setWeight(20))
                        .add(LootItem.lootTableItem(Items.STRING).apply(SetItemCountFunction.setCount(stringCount)).setWeight(20))
                        .add(LootItem.lootTableItem(Items.FIRE_CHARGE).apply(SetItemCountFunction.setCount(fireChargeCount)).setWeight(40))
                        .add(LootItem.lootTableItem(Items.GRAVEL).apply(SetItemCountFunction.setCount(gravelCount)).setWeight(40))
                        .add(LootItem.lootTableItem(Items.LEATHER).apply(SetItemCountFunction.setCount(leatherCount)).setWeight(40))
                        .add(LootItem.lootTableItem(Items.NETHER_BRICK).apply(SetItemCountFunction.setCount(netherBrickCount)).setWeight(40))
                        .add(LootItem.lootTableItem(Items.OBSIDIAN).setWeight(40))
                        .add(LootItem.lootTableItem(Items.CRYING_OBSIDIAN).apply(SetItemCountFunction.setCount(cryingObsidianCount)).setWeight(40))
                        .add(LootItem.lootTableItem(Items.SOUL_SAND).apply(SetItemCountFunction.setCount(soulSandCount)).setWeight(40))
                        .build();
                return LootTable.lootTable().pool(pool).build();
            }
            return null;
        }));

    }

}
