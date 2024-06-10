package gg.moonflower.mannequins.core;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import gg.moonflower.mannequins.common.entity.AbstractMannequin;
import gg.moonflower.mannequins.common.network.MannequinsMessages;
import gg.moonflower.mannequins.common.network.play.ClientboundAttackMannequin;
import gg.moonflower.mannequins.core.registry.MannequinsEntities;
import gg.moonflower.mannequins.core.registry.MannequinsItems;
import gg.moonflower.mannequins.core.registry.MannequinsSounds;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;

/**
 * @author Jackson, Ocelot
 */
public class Mannequins {

    public static final String MOD_ID = "mannequins";

    public static void init() {
        MannequinsSounds.REGISTRY.register();
        MannequinsItems.REGISTRY.register();
        MannequinsEntities.REGISTRY.register();


        EntityAttributeRegistry.register(MannequinsEntities.MANNEQUIN, () -> AbstractMannequin.createLivingAttributes().add(Attributes.KNOCKBACK_RESISTANCE, 1.0));
        EntityAttributeRegistry.register(MannequinsEntities.STATUE, () -> AbstractMannequin.createLivingAttributes().add(Attributes.KNOCKBACK_RESISTANCE, 1.0));
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (player.level().isClientSide())
                return CompoundEventResult.pass();

            if (player.fishing == null || !(stack.getItem() instanceof FishingRodItem))
                return CompoundEventResult.pass();

            Entity hooked = player.fishing.getHookedIn();
            if (hooked instanceof AbstractMannequin) {
                MannequinsMessages.PLAY.sendToTracking(hooked, new ClientboundAttackMannequin(hooked.getId(), (float) (Math.PI - Mth.atan2(player.getX() - hooked.getX(), player.getZ() - hooked.getZ()))));
            }

            return CompoundEventResult.pass();
        });
    }

    public static void postInit() {
        MannequinsMessages.init();
    }
}
