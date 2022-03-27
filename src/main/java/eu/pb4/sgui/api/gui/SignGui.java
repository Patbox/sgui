package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.mixin.SignBlockEntityAccessor;
import eu.pb4.sgui.virtual.FakeScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Sign Gui Implementation
 * <p>
 * The Vanilla sign GUI does not use a {@link net.minecraft.screen.ScreenHandler} and thus
 * it gets its data directly from a block in the world. Due to this, before opening the
 * screen the server must send a fake 'ghost' sign block to the player which contains the data
 * we want the sign to show. We send the block at the players location at max world height
 * so it hopefully goes unnoticed. The fake block is removed when the GUI is closed.
 * This also means in order to refresh the data on the sign, we must close and re-open the GUI,
 * as only handled screens have property support.
 * On the server side however, this sign GUI uses a custom {@link FakeScreenHandler} so the server
 * can manage and trigger methods like onTIck, onClose, ect.
 * <p>
 * SignGui has lots of deprecated methods which have no function, mainly due to the lack of
 * item slots and a client ScreenHandler.
 */
public class SignGui implements GuiInterface {

    protected final SignBlockEntity signEntity;
    protected BlockState type = Blocks.OAK_SIGN.getDefaultState();
    protected boolean autoUpdate = true;

    protected List<Integer> sendLineUpdate = new ArrayList<>(4);
    protected final ServerPlayerEntity player;
    protected boolean open = false;
    protected boolean reOpen = false;
    protected FakeScreenHandler screenHandler;

    /**
     * Constructs a new SignGui for the provided player
     *
     * @param player the player to serve this gui to
     */
    public SignGui(ServerPlayerEntity player)  {
        this.player = player;
        this.signEntity = new SignBlockEntity(new BlockPos(player.getBlockPos().getX(), player.world.getTopY() - 1, player.getBlockPos().getZ()), Blocks.OAK_SIGN.getDefaultState());
    }

    /**
     * Sets a line of {@link Text} on the sign.
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
     * Returns the {@link Text} from a line on the sign.
     *
     * @param line the line number
     * @return the text on the line
     */
    public Text getLine(int line) {
        return this.signEntity.getTextOnRow(line, false);
    }

    /**
     * Sets default color for the sign text.
     *
     * @param color the default sign color
     */
    public void setColor(DyeColor color) {
        ((SignBlockEntityAccessor) this.signEntity).setTextColorNoUpdate(color);

        if (this.open && this.autoUpdate) {
            this.updateSign();
        }
    }

    /**
     * Sets the block model used for the sign background.
     *
     * @param type a block in the {@link BlockTags#SIGNS} tag
     */
    public void setSignType(Block type) {
        if (!type.getRegistryEntry().isIn(BlockTags.SIGNS)) {
            throw new IllegalArgumentException("The type must be a sign");
        }

        this.type = type.getDefaultState();

        if (this.open && this.autoUpdate) {
            this.updateSign();
        }
    }

    /**
     * Sends sign updates to the player. <br>
     * This requires closing and reopening the gui, causing a flicker.
     */
    public void updateSign() {
        if (this.player.currentScreenHandler == this.screenHandler) {
            this.reOpen = true;
            this.player.networkHandler.sendPacket(new CloseScreenS2CPacket(this.screenHandler.syncId));
        } else {
            this.open();
        }
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }
    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public boolean open() {
        this.reOpen = true;

        if (this.player.currentScreenHandler != this.player.playerScreenHandler && this.player.currentScreenHandler != this.screenHandler) {
            this.player.closeHandledScreen();
        }
        if (screenHandler == null) {
            this.screenHandler = new FakeScreenHandler(this);
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
     * Used internally to receive input from the client
     */
    @ApiStatus.Internal
    public void setLineInternal(int line, Text text) {
        if (this.reOpen && this.sendLineUpdate.contains(line)) {
            this.sendLineUpdate.remove((Integer) line);
        } else {
            this.signEntity.setTextOnRow(line, text);
        }
    }

    @Deprecated
    @Override
    public void setTitle(Text title) {
    }

    @Deprecated
    @Override
    public Text getTitle() {
        return null;
    }

    @Deprecated
    @Override
    public ScreenHandlerType<?> getType() {
        return null;
    }

    @Deprecated
    @Override
    public int getSyncId() {
        return -1;
    }
}
