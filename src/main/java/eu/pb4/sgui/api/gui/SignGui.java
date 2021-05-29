package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.mixin.SignBlockEntityAccessor;
import eu.pb4.sgui.virtual.sign.SignScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public class SignGui implements GuiInterface {

    protected final SignBlockEntity signEntity = new SignBlockEntity();
    protected BlockState type = Blocks.OAK_SIGN.getDefaultState();
    protected boolean autoUpdate = true;

    protected List<Integer> sendLineUpdate = new ArrayList<>(4);
    protected final ServerPlayerEntity player;
    protected boolean open = false;
    protected boolean reOpen = false;
    protected SignScreenHandler screenHandler;

    public SignGui(ServerPlayerEntity playerEntity)  {
        this.player = playerEntity;
        this.signEntity.setPos(new BlockPos(player.getBlockPos().getX(), 255, player.getBlockPos().getZ()));
    }

    /**
     * Sets a line of {@link Text} on the sign
     *
     * @param line the line index, from 0
     * @param text the Text for the line, note that all formatting is stripped when the player closes the sign
     */
    public void setLine(int line, Text text) {
        this.signEntity.setTextOnRow(line, text);
        this.sendLineUpdate.add(line);

        if (this.open & this.autoUpdate) {
            this.updateSign();
        }
    }

    /**
     * Gets the {@link Text} from a line on the sign
     *
     * @param line the line number
     * @return the text on the line
     */
    public Text getLine(int line) {
        return ((SignBlockEntityAccessor) this.signEntity).getText()[line];
    }

    /**
     * Sets default color for the sign text.
     *
     * @param color the default sign color
     */
    public void setColor(DyeColor color) {
        ((SignBlockEntityAccessor) signEntity).setTextColorNoUpdate(color);

        if (this.open && this.autoUpdate) {
            this.updateSign();
        }
    }

    /**
     * Sets the block model used for the sign background
     *
     * @param type a block in the {@link BlockTags#SIGNS} tag
     */
    public void setSignType(Block type) {
        if (!type.isIn(BlockTags.SIGNS)) {
            throw new IllegalArgumentException("The type must be a sign");
        }

        this.type = type.getDefaultState();

        if (this.open && this.autoUpdate) {
            this.updateSign();
        }
    }

    /**
     * Send sign updates to the player.
     * This requires closing and reopening the gui, causing a flicker.
     */
    public void updateSign() {
        if (this.player.currentScreenHandler == this.screenHandler) {
            this.reOpen = true;
            this.player.networkHandler.sendPacket(new CloseScreenS2CPacket());
        } else {
            this.open();
        }
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean open() {
        this.reOpen = true;

        if (this.player.currentScreenHandler != this.player.playerScreenHandler && this.player.currentScreenHandler != this.screenHandler) {
            this.player.closeHandledScreen();
        }
        if (screenHandler == null) {
            this.screenHandler = new SignScreenHandler(this);
        }
        this.player.currentScreenHandler = this.screenHandler;

        this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.signEntity.getPos(), this.type));
        this.player.networkHandler.sendPacket(this.signEntity.toUpdatePacket());
        this.player.networkHandler.sendPacket(new SignEditorOpenS2CPacket(this.signEntity.getPos()));

        this.reOpen = false;
        this.open = true;

        return true;
    }

    @Override
    public void close(boolean alreadyClosed) {
        if (this.open && !this.reOpen) {
            this.open = false;
            this.reOpen = false;

            this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(player.world, signEntity.getPos()));

            if (alreadyClosed && this.player.currentScreenHandler == this.screenHandler) {
                this.player.closeScreenHandler();
            } else {
                this.player.closeHandledScreen();
            }

            this.onClose();
        } else {
            this.reOpen = false;
            this.open();
        }
    }

    @Override
    public boolean getAutoUpdate() {
        return this.autoUpdate;
    }

    @Override
    public void setAutoUpdate(boolean value) {
        this.autoUpdate = value;
    }

    /**
     * Used internally to receive input from the sign closing
     */
    @ApiStatus.Internal
    public void setLineInternal(int line, Text text) {
        if (this.reOpen && this.sendLineUpdate.contains(line)) {
            this.sendLineUpdate.remove((Integer) line);
        } else {
            this.signEntity.setTextOnRow(line, text);
        }
    }

    @Override
    public void onOpen() {
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onUpdate(boolean firstUpdate) {
    }

    @Override
    public void setTitle(Text title) {
    }

    @Override
    public Text getTitle() {
        return null;
    }

    @Override
    public ScreenHandlerType<?> getType() {
        return null;
    }

    @Override
    public int getSyncId() {
        return -1;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean getLockPlayerInventory() {
        return false;
    }

    @Override
    public void setLockPlayerInventory(boolean value) {
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        return false;
    }
}
