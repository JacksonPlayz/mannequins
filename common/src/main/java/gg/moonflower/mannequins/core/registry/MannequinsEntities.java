package gg.moonflower.mannequins.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import gg.moonflower.mannequins.common.entity.Mannequin;
import gg.moonflower.mannequins.common.entity.Statue;
import gg.moonflower.mannequins.core.Mannequins;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class MannequinsEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Mannequins.MOD_ID, Registries.ENTITY_TYPE);

    public static final Supplier<EntityType<Mannequin>> MANNEQUIN = REGISTRY.register("mannequin", () -> EntityType.Builder.of(Mannequin::new, MobCategory.MISC).sized(0.5F, 2.0F).clientTrackingRange(10).build("mannequin"));
    public static final Supplier<EntityType<Statue>> STATUE = REGISTRY.register("statue", () -> EntityType.Builder.of(Statue::new, MobCategory.MISC).sized(0.5F, 2.0F).clientTrackingRange(10).build("statue"));
}
