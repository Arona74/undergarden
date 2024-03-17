package quek.undergarden.mixin;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quek.undergarden.registry.UGBlocks;

@Mixin(BubbleColumnBlock.class)
public class BubbleColumnBlockMixin {

	@Inject(method = "getColumnState", at = @At(value = "RETURN", ordinal = 2), cancellable = true, remap = false)
	private static void undergarden$smogVentsCreateColumns(BlockState state, CallbackInfoReturnable<BlockState> cir) {
		if (state.is(UGBlocks.SMOG_VENT.get())) {
			cir.setReturnValue(Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(BubbleColumnBlock.DRAG_DOWN, false));
		}
	}
}
