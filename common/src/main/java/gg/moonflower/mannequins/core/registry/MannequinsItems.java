package gg.moonflower.mannequins.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import gg.moonflower.mannequins.common.item.MannequinItem;
import gg.moonflower.mannequins.core.Mannequins;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class MannequinsItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(Mannequins.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> MANNEQUIN = REGISTRY.register("mannequin", () -> new MannequinItem<>(MannequinsEntities.MANNEQUIN, new Item.Properties().stacksTo(16).arch$tab(CreativeModeTabs.FUNCTIONAL_BLOCKS)));
    public static final RegistrySupplier<Item> STATUE = REGISTRY.register("statue", () -> new MannequinItem<>(MannequinsEntities.STATUE, new Item.Properties().stacksTo(16).arch$tab(CreativeModeTabs.FUNCTIONAL_BLOCKS)));

}
