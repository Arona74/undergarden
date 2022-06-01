package quek.undergarden.world.gen.tree;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import quek.undergarden.registry.UGConfiguredFeatures;

import javax.annotation.Nullable;
import java.util.Random;

public class WigglewoodTree extends AbstractTreeGrower {

    @Nullable
    @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random random, boolean hive) {
        return random.nextInt(10) == 0 ? UGConfiguredFeatures.TALL_WIGGLEWOOD_TREE.getHolder().get() : UGConfiguredFeatures.WIGGLEWOOD_TREE.getHolder().get();
    }
}