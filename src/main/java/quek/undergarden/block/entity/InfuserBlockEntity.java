package quek.undergarden.block.entity;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import quek.undergarden.inventory.InfuserMenu;
import quek.undergarden.recipe.InfusingRecipe;
import quek.undergarden.registry.UGBlockEntities;
import quek.undergarden.registry.UGItems;
import quek.undergarden.registry.UGRecipeTypes;

public class InfuserBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible {

	private static final int[] SLOTS_FOR_UP = new int[]{0};
	private static final int[] SLOTS_FOR_DOWN = new int[]{3};
	private static final int[] SLOTS_FOR_SIDES = new int[]{1, 2};

	private NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
	int infusingProgress;
	int infusingTotalTime;
	private final ContainerData containerData = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> InfuserBlockEntity.this.infusingProgress;
				case 1 -> InfuserBlockEntity.this.infusingTotalTime;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0:
					InfuserBlockEntity.this.infusingProgress = value;
					break;
				case 1:
					InfuserBlockEntity.this.infusingTotalTime = value;
					//break;
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
	};

	private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
	private final RecipeManager.CachedCheck<SingleRecipeInput, InfusingRecipe> quickCheck;

	public InfuserBlockEntity(BlockPos pos, BlockState blockState) {
		super(UGBlockEntities.INFUSER.get(), pos, blockState);
		this.quickCheck = RecipeManager.createCheck(UGRecipeTypes.INFUSING.get());
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.undergarden.infuser");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		ItemStack itemstack = this.items.get(index);
		boolean flag = !stack.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, stack);
		this.items.set(index, stack);
		stack.limitSize(this.getMaxStackSize(stack));
		if (index == 0 && !flag) {
			this.infusingTotalTime = getTotalInfusingTime(this.level, this);
			this.infusingProgress = 0;
			this.setChanged();
		}
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new InfuserMenu(containerId, inventory, this, this.containerData);
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		if (side == Direction.DOWN) {
			return SLOTS_FOR_DOWN;
		} else {
			return side == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
		}
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
		return switch (index) {
			case 0 -> true;
			case 1 -> itemStack.is(UGItems.UTHERIUM_CRYSTAL);
			case 2 -> itemStack.is(UGItems.ROGDORIUM_CRYSTAL);
			default -> false;
		};
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return direction == Direction.DOWN && index == 3;
	}

	@Override
	public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
		if (recipe != null) {
			ResourceLocation resourcelocation = recipe.id();
			this.recipesUsed.addTo(resourcelocation, 1);
		}
	}

	@Nullable
	@Override
	public RecipeHolder<?> getRecipeUsed() {
		return null;
	}

	@Override
	public void fillStackedContents(StackedContents contents) {
		for (ItemStack stack : this.items) {
			contents.accountStack(stack);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, this.items, registries);
		this.infusingProgress = tag.getInt("InfusingTime");
		this.infusingTotalTime = tag.getInt("InfusingTimeTotal");
		CompoundTag compoundtag = tag.getCompound("RecipesUsed");

		for (String s : compoundtag.getAllKeys()) {
			this.recipesUsed.put(ResourceLocation.parse(s), compoundtag.getInt(s));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putInt("InfusingTime", this.infusingProgress);
		tag.putInt("InfusingTimeTotal", this.infusingTotalTime);
		ContainerHelper.saveAllItems(tag, this.items, registries);
		CompoundTag compoundtag = new CompoundTag();
		this.recipesUsed.forEach((p_187449_, p_187450_) -> compoundtag.putInt(p_187449_.toString(), p_187450_));
		tag.put("RecipesUsed", compoundtag);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, InfuserBlockEntity blockEntity) {
		boolean changed = false;
		//Undergarden.LOGGER.debug("time {}", blockEntity.infusingProgress);
		//Undergarden.LOGGER.debug("totalTime {}", blockEntity.infusingTotalTime);

		ItemStack utheriumFuel = blockEntity.items.get(1);
		ItemStack rogdoriumFuel = blockEntity.items.get(2);
		ItemStack input = blockEntity.items.get(0);
		boolean inputFull = !input.isEmpty();
		boolean utheriumFuelFull = !utheriumFuel.isEmpty();
		boolean rogdoriumFuelFull = !rogdoriumFuel.isEmpty();

		RecipeHolder<InfusingRecipe> recipe = blockEntity.quickCheck.getRecipeFor(new SingleRecipeInput(input), level).orElse(null);
		int i = blockEntity.getMaxStackSize();

		if (inputFull) {
			if (blockEntity.canInfuse(level.registryAccess(), recipe, blockEntity.items, i, blockEntity)) {
				blockEntity.infusingProgress++;
				if (blockEntity.infusingProgress == blockEntity.infusingTotalTime) {
					if (utheriumFuelFull && recipe != null && recipe.value().isUtheriumFuel()) {
						utheriumFuel.shrink(1);
					}
					if (rogdoriumFuelFull && recipe != null && !recipe.value().isUtheriumFuel()) {
						rogdoriumFuel.shrink(1);
					}

					blockEntity.infusingProgress = 0;
					blockEntity.infusingTotalTime = getTotalInfusingTime(level, blockEntity);
					if (blockEntity.infuse(level.registryAccess(), recipe, blockEntity.items, i, blockEntity)) {
						blockEntity.setRecipeUsed(recipe);
					}

					changed = true;
				}
			} else {
				blockEntity.infusingProgress = 0;
			}
		} else if (blockEntity.infusingProgress > 0) {
			blockEntity.infusingProgress = Mth.clamp(blockEntity.infusingProgress - 2, 0, blockEntity.infusingTotalTime);
		}

		if (changed) {
			setChanged(level, pos, state);
		}
	}

	private boolean canInfuse(RegistryAccess registryAccess, @javax.annotation.Nullable RecipeHolder<InfusingRecipe> recipe, NonNullList<ItemStack> inventory, int maxStackSize, InfuserBlockEntity infuser) {
		if (!inventory.get(0).isEmpty() && recipe != null) {
			ItemStack result = recipe.value().assemble(new SingleRecipeInput(infuser.getItem(0)), registryAccess);

			if (inventory.get(1).isEmpty() && recipe.value().isUtheriumFuel()) {
				return false;
			}
			if (inventory.get(2).isEmpty() && !recipe.value().isUtheriumFuel()) {
				return false;
			}

			if (result.isEmpty()) {
				return false;
			} else {
				ItemStack resultSlot = inventory.get(3);
				if (resultSlot.isEmpty()) {
					return true;
				} else if (!ItemStack.isSameItem(resultSlot, result)) {
					return false;
				} else {
					// Neo fix: make furnace respect stack sizes in furnace recipes
					return resultSlot.getCount() + result.getCount() <= maxStackSize && resultSlot.getCount() + result.getCount() <= resultSlot.getMaxStackSize() || resultSlot.getCount() + result.getCount() <= result.getMaxStackSize(); // Neo fix: make furnace respect stack sizes in furnace recipes
				}
			}
		} else {
			return false;
		}
	}

	private boolean infuse(RegistryAccess registryAccess, @javax.annotation.Nullable RecipeHolder<InfusingRecipe> recipe, NonNullList<ItemStack> inventory, int maxStackSize, InfuserBlockEntity infuser) {
		if (recipe != null && canInfuse(registryAccess, recipe, inventory, maxStackSize, infuser)) {
			ItemStack input = inventory.get(0);
			ItemStack result = recipe.value().assemble(new SingleRecipeInput(this.getItem(0)), registryAccess);
			ItemStack output = inventory.get(3);
			if (output.isEmpty()) {
				inventory.set(3, result.copy());
			} else if (output.is(result.getItem())) {
				output.grow(result.getCount());
			}

			input.shrink(1);
			return true;
		} else {
			return false;
		}
	}

	private static int getTotalInfusingTime(Level level, InfuserBlockEntity blockEntity) {
		SingleRecipeInput singlerecipeinput = new SingleRecipeInput(blockEntity.getItem(0));
		return blockEntity.quickCheck.getRecipeFor(singlerecipeinput, level).map(p_300840_ -> p_300840_.value().getInfusingTime()).orElse(200);
	}
}