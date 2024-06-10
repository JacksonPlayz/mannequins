package gg.moonflower.mannequins.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gg.moonflower.mannequins.client.screen.component.ScrollBar;
import gg.moonflower.mannequins.common.entity.AbstractMannequin;
import gg.moonflower.mannequins.common.menu.MannequinInventoryMenu;
import gg.moonflower.mannequins.common.network.MannequinsMessages;
import gg.moonflower.mannequins.common.network.play.ServerboundSetMannequinPose;
import gg.moonflower.pollen.api.render.util.v1.ScissorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Jackson, Ocelot
 */
public abstract class AbstractMannequinScreen extends AbstractContainerScreen<MannequinInventoryMenu> {
    private static MannequinPart selectedPart = MannequinPart.HEAD;
    private final AbstractMannequin mannequin;

    private ScrollBar xScroll;
    private ScrollBar yScroll;
    private ScrollBar zScroll;

    public AbstractMannequinScreen(MannequinInventoryMenu menu, Inventory inventory, AbstractMannequin mannequin) {
        super(menu, inventory, mannequin.getDisplayName());
        this.mannequin = mannequin;
        //this.passEvents = false;
        this.imageHeight = 185;
        this.inventoryLabelY += 20;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(this.xScroll = new ScrollBar(this.leftPos + 136, this.topPos + 20, 8, 65, 360, Component.literal("X")));
        this.addRenderableWidget(this.yScroll = new ScrollBar(this.leftPos + 147, this.topPos + 20, 8, 65, 360, Component.literal("Y")));
        this.addRenderableWidget(this.zScroll = new ScrollBar(this.leftPos + 158, this.topPos + 20, 8, 65, 360, Component.literal("Z")));
        this.xScroll.setScrollSpeed(1);
        this.yScroll.setScrollSpeed(1);
        this.zScroll.setScrollSpeed(1);
        this.updateSliders();
    }

    private void updateSliders() {
        Rotations rotations = selectedPart.getRotation(this.mannequin);
        float rotationX = Mth.wrapDegrees(rotations.getX()) + 180;
        float rotationY = Mth.wrapDegrees(rotations.getY()) + 180;
        float rotationZ = Mth.wrapDegrees(rotations.getZ()) + 180;
        if (selectedPart == MannequinPart.LEFT_ARM)
            rotationZ = 360 - rotationZ;
        this.xScroll.setScroll(rotationX % 360 / 360.0F * this.xScroll.getMaxScroll());
        this.yScroll.setScroll(rotationY % 360 / 360.0F * this.yScroll.getMaxScroll());
        this.zScroll.setScroll(rotationZ % 360 / 360.0F * this.zScroll.getMaxScroll());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (MannequinPart part : MannequinPart.values()) {
            if (selectedPart != part && part.isHovered(mouseX - this.leftPos, mouseY - this.topPos)) {
                selectedPart = part;
                this.updateSliders();
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return this.xScroll.mouseReleased(mouseX, mouseY, mouseButton) || this.yScroll.mouseReleased(mouseX, mouseY, mouseButton) || this.zScroll.mouseReleased(mouseX, mouseY, mouseButton) || super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void containerTick() {
        this.xScroll.tick();
        this.yScroll.tick();
        this.zScroll.tick();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(this.getTexture(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        for (MannequinPart part : MannequinPart.values())
            if (selectedPart != part)
                graphics.blit(this.getTexture(), this.leftPos + part.xOffset, this.topPos + part.yOffset, part.isHovered(mouseX - this.leftPos, mouseY - this.topPos) ? part.buttonU + part.buttonWidth : part.buttonU, part.buttonV, part.buttonWidth, part.buttonHeight);

        ScissorHelper.push(this.leftPos + 26, this.topPos + 18, 49, 70);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, this.leftPos + 51, this.topPos + 80, 28, 0, -30, this.mannequin);
        ScissorHelper.pop();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);

        Rotations rotations = selectedPart.getRotation(this.mannequin);
        float x = this.xScroll.getInterpolatedScrollPercentage(partialTicks) * 360.0F - 180.0F;
        float y = this.yScroll.getInterpolatedScrollPercentage(partialTicks) * 360.0F - 180.0F;
        float z = this.zScroll.getInterpolatedScrollPercentage(partialTicks) * 360.0F - 180.0F;
        if (selectedPart == MannequinPart.LEFT_ARM)
            z *= -1;
        if (rotations.getX() != x || rotations.getY() != y || rotations.getZ() != z)
            selectedPart.setRotation(this.mannequin, new Rotations(x, y, z));
    }

    @Override
    public void onClose() {
        MannequinsMessages.PLAY.sendToServer(new ServerboundSetMannequinPose(this.menu.containerId, this.mannequin.getHeadPose(), this.mannequin.getBodyPose(), this.mannequin.getLeftArmPose(), this.mannequin.getRightArmPose()));
        super.onClose();
    }

    public abstract ResourceLocation getTexture();

    enum MannequinPart {
        HEAD(AbstractMannequin::setHeadPose, AbstractMannequin::getHeadPose, 98, 21, 176, 59, 16, 16),
        CHEST(AbstractMannequin::setBodyPose, AbstractMannequin::getBodyPose, 98, 37, 176, 39, 16, 20),
        LEFT_ARM(AbstractMannequin::setLeftArmPose, AbstractMannequin::getLeftArmPose, 114, 37, 176, 15, 8, 24),
        RIGHT_ARM(AbstractMannequin::setRightArmPose, AbstractMannequin::getRightArmPose, 90, 37, 176, 15, 8, 24);

        private final BiConsumer<AbstractMannequin, Rotations> rotationSetter;
        private final Function<AbstractMannequin, Rotations> rotationGetter;
        private final int xOffset;
        private final int yOffset;
        private final int buttonU;
        private final int buttonV;
        private final int buttonWidth;
        private final int buttonHeight;

        MannequinPart(BiConsumer<AbstractMannequin, Rotations> rotationSetter, Function<AbstractMannequin, Rotations> rotationGetter, int xOffset, int yOffset, int buttonU, int buttonV, int buttonWidth, int buttonHeight) {
            this.rotationSetter = rotationSetter;
            this.rotationGetter = rotationGetter;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.buttonU = buttonU;
            this.buttonV = buttonV;
            this.buttonWidth = buttonWidth;
            this.buttonHeight = buttonHeight;
        }

        public boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= this.xOffset && mouseX < this.xOffset + this.buttonWidth && mouseY >= this.yOffset && mouseY < this.yOffset + this.buttonHeight;
        }

        public Rotations getRotation(AbstractMannequin mannequin) {
            return this.rotationGetter.apply(mannequin);
        }

        public void setRotation(AbstractMannequin mannequin, Rotations rot) {
            this.rotationSetter.accept(mannequin, rot);
        }
    }
}