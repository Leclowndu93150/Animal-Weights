package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.command.AnimalWeightsCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;

public class AnimalweightsFabric implements ModInitializer {
    public static final AttachmentType<WeightData> WEIGHT_ATTACHMENT = AttachmentRegistry
        .<WeightData>builder()
        .initializer(WeightData::new)
        .persistent(WeightData.CODEC)
        .syncWith(WeightData.STREAM_CODEC.cast(), AttachmentSyncPredicate.all())
        .buildAndRegister(ResourceLocation.parse("animalweights:weight"));

    @Override
    public void onInitialize() {
        Animalweights.init(FabricLoader.getInstance().getConfigDir());
        WeightAttachment.install(new WeightAttachment.Bridge() {
            @Override
            public WeightData get(Animal animal) {
                return animal.getAttachedOrCreate(WEIGHT_ATTACHMENT);
            }

            @Override
            public void set(Animal animal, WeightData data) {
                animal.setAttached(WEIGHT_ATTACHMENT, data);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
            AnimalWeightsCommand.register(dispatcher));
    }
}
