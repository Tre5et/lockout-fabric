package me.marin.lockout.mixin.server;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerTrades.class)
public class TradeOffersMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void lockout$forceTrialChamberMap(CallbackInfo ci) {
        Int2ObjectMap<VillagerTrades.ItemListing[]> cartographerTrades = VillagerTrades.TRADES.get(VillagerProfession.CARTOGRAPHER);
        if (cartographerTrades != null) {
            cartographerTrades.put(2, new VillagerTrades.ItemListing[]{
                new VillagerTrades.EmeraldForItems(Items.GLASS_PANE, 11, 12, 10),
                new VillagerTrades.TreasureMapForEmeralds(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10)
            });
            cartographerTrades.put(3, new VillagerTrades.ItemListing[]{
                new VillagerTrades.EmeraldForItems(Items.COMPASS, 1, 12, 20),
                new VillagerTrades.TreasureMapForEmeralds(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.OCEAN_MONUMENT, 12, 10)
            });
        }
    }
}
