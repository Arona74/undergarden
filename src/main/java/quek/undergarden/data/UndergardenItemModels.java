package quek.undergarden.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import quek.undergarden.data.provider.UndergardenItemModelProvider;
import quek.undergarden.registry.UndergardenBlocks;
import quek.undergarden.registry.UndergardenItems;

public class UndergardenItemModels extends UndergardenItemModelProvider {

    public UndergardenItemModels(DataGenerator generator, ExistingFileHelper fileHelper) {
        super(generator, fileHelper);
    }

    @Override
    public String getName() {
        return "Undergarden Item Models";
    }

    @Override
    protected void registerModels() {
        itemBlock(UndergardenBlocks.depthrock);
        itemBlock(UndergardenBlocks.coal_ore);
        itemBlock(UndergardenBlocks.cloggrum_ore);
        itemBlock(UndergardenBlocks.utherium_ore);
        itemBlock(UndergardenBlocks.cobbled_depthrock);
        //itemBlock(UndergardenBlocks.deepturf);
        itemBlock(UndergardenBlocks.deepsoil);
        itemBlock(UndergardenBlocks.smogstem_log);
        itemBlock(UndergardenBlocks.wigglewood_log);
        itemBlock(UndergardenBlocks.smogstem_planks);
        itemBlock(UndergardenBlocks.wigglewood_planks);
        itemBlockFlat(UndergardenBlocks.tall_deepturf);

        normalItem(UndergardenItems.smogstem_stick);
        normalItem(UndergardenItems.cloggrum_ingot);
        //normalItem(UndergardenItems.cloggrum_nugget);
        normalItem(UndergardenItems.utheric_shard);
        normalItem(UndergardenItems.utherium_ingot);
        normalItem(UndergardenItems.utherium_chunk);

        toolItem(UndergardenItems.smogstem_sword);
        toolItem(UndergardenItems.smogstem_pickaxe);
        toolItem(UndergardenItems.smogstem_axe);
        toolItem(UndergardenItems.smogstem_shovel);

        toolItem(UndergardenItems.cloggrum_sword);
        toolItem(UndergardenItems.cloggrum_pickaxe);
        toolItem(UndergardenItems.cloggrum_axe);
        toolItem(UndergardenItems.cloggrum_shovel);

        toolItem(UndergardenItems.utheric_sword);
        toolItem(UndergardenItems.utheric_pickaxe);
        toolItem(UndergardenItems.utheric_axe);
        toolItem(UndergardenItems.utheric_shovel);

        //normalItem(UndergardenItems.underbeans);
        //normalItem(UndergardenItems.raw_dweller_meat);
        //normalItem(UndergardenItems.dweller_steak);
    }


}
