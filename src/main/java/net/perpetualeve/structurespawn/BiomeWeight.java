package net.perpetualeve.structurespawn;

import java.util.ArrayList;
import java.util.List;

import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.ParsedCollections;
import carbonconfiglib.utils.ParsedCollections.ParsedMap;
import carbonconfiglib.utils.structure.IStructuredData.EntryDataType;
import carbonconfiglib.utils.structure.StructureCompound.CompoundBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeWeight {
    ResourceLocation biome;
    double weight;

    public BiomeWeight() {
	this.biome = Biomes.PLAINS.location();
	this.weight = 1.0d;
    }

    public BiomeWeight(ResourceLocation biome, double weight) {
	super();
	this.biome = biome;
	this.weight = weight;
    }

    public ResourceLocation getBiome() {
	return biome;
    }

    public static List<BiomeWeight> createDefault() {
	List<BiomeWeight> biomes = new ArrayList<>();
	for (ResourceLocation biome : ForgeRegistries.BIOMES.getKeys()) {
	    if(biome == null) throw new NullPointerException(); 
	    biomes.add(new BiomeWeight(biome, 1));
	}
	return biomes;
    }

    public static IConfigSerializer<BiomeWeight> createParser() {
	CompoundBuilder builder = new CompoundBuilder();

	builder.variants("Biome", EntryDataType.STRING, ResourceLocation.class, T -> {
	    try {
		return ParseResult.success(new ResourceLocation(T));
	    } catch (Exception e) {
		return ParseResult.error(T, e, "Couldn't parse ");
	    }
	}, T -> {
	    return T.toString();
	}).finish();
	builder.setNewLined(false);

	builder.simple("Weight", EntryDataType.DOUBLE).finish();

	return IConfigSerializer.noSync(builder.build(), new BiomeWeight(), BiomeWeight::parse, BiomeWeight::serialize);
    }

    public static ParseResult<BiomeWeight> parse(ParsedCollections.ParsedMap values) {

	ParseResult<ResourceLocation> biome = values.getOrError("Biome", ResourceLocation.class);
	if (biome.hasError())
	    return biome.onlyError();
	ParseResult<Double> weight = values.getOrError("Weight", Double.class);
	if (weight.hasError())
	    return weight.onlyError();

	return ParseResult.success(new BiomeWeight(biome.getValue(), weight.getValue()));
    }

    public ParsedCollections.ParsedMap serialize() {

	ParsedMap map = new ParsedMap();

	map.put("Biome", biome);
	map.put("Weight", weight);

	return map;
    }
}