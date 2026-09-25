package com.leclowndu93150.animalweights.compat.jade;

import com.leclowndu93150.animalweights.AnimalWeightsRules;
import com.leclowndu93150.animalweights.Animalweights;
import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.habitat.HabitatScanner;
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
    private static final String TROUGH_KEY = "animalweights_trough_food";
    private static final String WILD_KEY = "animalweights_wild";
    private static final DataProvider DATA_PROVIDER = new DataProvider();
    private static final ComponentProvider COMPONENT_PROVIDER = new ComponentProvider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(DATA_PROVIDER, Animal.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(COMPONENT_PROVIDER, Animal.class);
    }

    private static final class DataProvider implements IServerDataProvider<EntityAccessor> {
        @Override
        public void appendServerData(CompoundTag data, EntityAccessor accessor) {
            if (accessor.getEntity() instanceof Animal animal) {
                data.putInt(DATA_KEY, WeightAttachment.get(animal).getTicksSinceEvaluation());
                data.putBoolean(TROUGH_KEY, HabitatScanner.hasUsableFeedingTrough(animal));
                data.putBoolean(WILD_KEY, !AnimalWeightsRules.isActive(animal));
            }
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }

    private static final class ComponentProvider implements IComponentProvider<EntityAccessor> {
        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof Animal animal) {
                int serverTicks = accessor.getServerData().getIntOr(DATA_KEY, 0);
                boolean troughFood = accessor.getServerData().getBooleanOr(TROUGH_KEY, false);
                boolean wild = accessor.getServerData().getBooleanOr(WILD_KEY, false);
                for (Component line : MagnifyingGlassInspector.buildCompactLines(animal, serverTicks, wild, troughFood)) {
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
