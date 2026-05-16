package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.command.AnimalWeightsCommand;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod("animalweights")
public class AnimalweightsNeoForge {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "animalweights");

    public static final Supplier<AttachmentType<WeightData>> WEIGHT_ATTACHMENT = ATTACHMENTS.register(
        "weight",
        () -> AttachmentType.builder(WeightData::new)
            .serialize(WeightData.MAP_CODEC)
            .sync(WeightData.STREAM_CODEC.cast())
            .build()
    );

    public AnimalweightsNeoForge(IEventBus modBus) {
        Animalweights.init(FMLPaths.CONFIGDIR.get());
        ATTACHMENTS.register(modBus);
        WeightAttachment.install(new WeightAttachment.Bridge() {
            @Override
            public WeightData get(Animal animal) {
                return animal.getData(WEIGHT_ATTACHMENT);
            }

            @Override
            public void set(Animal animal, WeightData data) {
                animal.setData(WEIGHT_ATTACHMENT, data);
            }
        });
        NeoForge.EVENT_BUS.addListener(AnimalweightsNeoForge::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        AnimalWeightsCommand.register(event.getDispatcher());
    }
}
