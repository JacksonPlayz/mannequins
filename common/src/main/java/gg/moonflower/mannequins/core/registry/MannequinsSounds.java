package gg.moonflower.mannequins.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import gg.moonflower.mannequins.core.Mannequins;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class MannequinsSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Mannequins.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> ENTITY_MANNEQUIN_BREAK = registerSound("entity.mannequin.break");
    public static final RegistrySupplier<SoundEvent> ENTITY_MANNEQUIN_FALL = registerSound("entity.mannequin.fall");
    public static final RegistrySupplier<SoundEvent> ENTITY_MANNEQUIN_HIT = registerSound("entity.mannequin.hit");
    public static final RegistrySupplier<SoundEvent> ENTITY_MANNEQUIN_PLACE = registerSound("entity.mannequin.place");

    public static final RegistrySupplier<SoundEvent> ENTITY_STATUE_BREAK = registerSound("entity.statue.break");
    public static final RegistrySupplier<SoundEvent> ENTITY_STATUE_FALL = registerSound("entity.statue.fall");
    public static final RegistrySupplier<SoundEvent> ENTITY_STATUE_HIT = registerSound("entity.statue.hit");
    public static final RegistrySupplier<SoundEvent> ENTITY_STATUE_PLACE = registerSound("entity.statue.place");

    private static RegistrySupplier<SoundEvent> registerSound(String id) {
        return REGISTRY.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Mannequins.MOD_ID, id)));
    }
}
