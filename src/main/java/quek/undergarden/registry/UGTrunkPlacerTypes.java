package quek.undergarden.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import quek.undergarden.Undergarden;
import quek.undergarden.world.gen.trunkplacer.SmogstemTrunkPlacer;

public class UGTrunkPlacerTypes {

    public static final TrunkPlacerType<SmogstemTrunkPlacer> SMOGSTEM_TRUNK_PLACER = register("smogstem_trunk_placer", SmogstemTrunkPlacer.CODEC);

    private static <P extends TrunkPlacer> TrunkPlacerType<P> register(String name, Codec<P> codec) {
        return Registry.register(Registry.TRUNK_PLACER_TYPES, new ResourceLocation(Undergarden.MODID, name), new TrunkPlacerType<>(codec));
    }
}