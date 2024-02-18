package quek.undergarden.data.provider;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;
import quek.undergarden.Undergarden;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class UGLangProvider extends LanguageProvider {

	private final PackOutput output;
	public final Map<String, String> upsideDownEntries = new HashMap<>();

	public UGLangProvider(PackOutput output) {
		super(output, Undergarden.MODID, "en_us");
		this.output = output;
	}

	@Override
	public void add(String key, String value) {
		super.add(key, value);
		List<LangFormatSplitter.Component> splitEnglish = LangFormatSplitter.split(value);
		this.upsideDownEntries.put(key, LangConversionHelper.convertComponents(splitEnglish));
	}

	protected void addAdvTitle(String advancementTitle, String name) {
		add("advancement.undergarden." + advancementTitle + ".title", name);
	}

	protected void addAdvDesc(String advancementTitle, String name) {
		add("advancement.undergarden." + advancementTitle + ".desc", name);
	}

	protected void addSubtitle(String category, String subtitleName, String name) {
		add("subtitles." + category + "." + subtitleName, name);
	}

	protected void addBiome(ResourceKey<Biome> biomeKey, String name) {
		add("biome.undergarden." + biomeKey.location().getPath(), name);
	}

	protected void addDeath(String deathName, String name) {
		add("death.attack." + deathName, name);
	}

	protected void addPotion(Supplier<? extends Potion> potion, String name) {
		add("item.minecraft.potion.effect." + ForgeRegistries.POTIONS.getKey(potion.get()).getPath(), "Potion of " + name);
		add("item.minecraft.splash_potion.effect." + ForgeRegistries.POTIONS.getKey(potion.get()).getPath(), "Splash Potion of " + name);
		add("item.minecraft.lingering_potion.effect." + ForgeRegistries.POTIONS.getKey(potion.get()).getPath(), "Lingering Potion of " + name);
		add("item.minecraft.tipped_arrow.effect." + ForgeRegistries.POTIONS.getKey(potion.get()).getPath(), "Arrow of " + name);
	}

	protected void addConfig(String configName, String name) {
		add("config.undergarden." + configName, name);
	}


	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		//generate normal lang file
		CompletableFuture<?> languageGen = super.run(cache);
		ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();
		futuresBuilder.add(languageGen);

		//generate en_ud file
		JsonObject upsideDownFile = new JsonObject();
		this.upsideDownEntries.forEach(upsideDownFile::addProperty);
		futuresBuilder.add(DataProvider.saveStable(cache, upsideDownFile, this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(Undergarden.MODID).resolve("lang").resolve("en_ud.json")));

		return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
	}
}
