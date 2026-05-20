package com.leclowndu93150.animalweights.compat.jade;

import com.leclowndu93150.animalweights.Animalweights;
import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.inspect.MagnifyingGlassInspector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class AnimalWeightsJadePlugin implements IWailaPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(Animalweights.MOD_ID, "animal_condition");
    private static final String DATA_KEY = "animalweights_ticks_since_eval";
    private static final Provider PROVIDER = new Provider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(PROVIDER, Animal.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(PROVIDER, Animal.class);
    }

    private static final class Provider implements IComponentProvider<EntityAccessor>, IServerDataProvider<EntityAccessor> {
        @Override
        public void appendServerData(CompoundTag data, EntityAccessor accessor) {
            if (accessor.getEntity() instanceof Animal animal) {
                data.putInt(DATA_KEY, WeightAttachment.get(animal).getTicksSinceEvaluation());
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof Animal animal) {
                int serverTicks = accessor.getServerData().getIntOr(DATA_KEY, 0);
                for (Component line : MagnifyingGlassInspector.buildCompactLines(animal, serverTicks)) {
                    tooltip.add(line);
                }
            }
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
