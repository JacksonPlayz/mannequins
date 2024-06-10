package gg.moonflower.mannequins.core;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import gg.moonflower.mannequins.client.render.entity.MannequinRenderer;
import gg.moonflower.mannequins.client.render.entity.StatueRenderer;
import gg.moonflower.mannequins.client.render.model.MannequinModel;
import gg.moonflower.mannequins.client.render.model.MannequinsModelLayers;
import gg.moonflower.mannequins.client.render.model.StatueModel;
import gg.moonflower.mannequins.common.network.MannequinsMessages;
import gg.moonflower.mannequins.common.network.play.handler.MannequinsClientPlayPacketHandlerImpl;
import gg.moonflower.mannequins.core.registry.MannequinsEntities;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class MannequinsClient {

    public static void init() {
        //RegisterAtlasSpriteEvent.event(InventoryMenu.BLOCK_ATLAS).register((atlas, registry) -> registry.accept(new ResourceLocation(Mannequins.MOD_ID, "item/empty_mannequin_slot_mainhand")));
        EntityModelLayerRegistry.register(MannequinsModelLayers.MANNEQUIN, MannequinModel::createLayerDefinition);
        EntityModelLayerRegistry.register(MannequinsModelLayers.MANNEQUIN_INNER_ARMOR, () -> MannequinModel.createLayerDefinition(new CubeDeformation(0.5F)));
        EntityModelLayerRegistry.register(MannequinsModelLayers.MANNEQUIN_OUTER_ARMOR, () -> MannequinModel.createLayerDefinition(new CubeDeformation(1.0F)));

        EntityModelLayerRegistry.register(MannequinsModelLayers.STATUE, StatueModel::createLayerDefinition);
        EntityModelLayerRegistry.register(MannequinsModelLayers.STATUE_INNER_ARMOR, () -> StatueModel.createLayerDefinition(new CubeDeformation(0.5F)));
        EntityModelLayerRegistry.register(MannequinsModelLayers.STATUE_OUTER_ARMOR, () -> StatueModel.createLayerDefinition(new CubeDeformation(1.0F)));

        EntityRendererRegistry.register(MannequinsEntities.MANNEQUIN, MannequinRenderer::new);
        EntityRendererRegistry.register(MannequinsEntities.STATUE, StatueRenderer::new);
    }

    public static void postInit() {
        MannequinsMessages.PLAY.setClientHandler(new MannequinsClientPlayPacketHandlerImpl());
    }
}
