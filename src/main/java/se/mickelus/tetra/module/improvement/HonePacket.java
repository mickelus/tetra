package se.mickelus.tetra.module.improvement;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.network.AbstractPacket;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HonePacket extends AbstractPacket {

    ItemStack itemStack;

    public HonePacket() {
    }

    public HonePacket(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        ItemStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, itemStack);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        itemStack = ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
    }

    @Override
    public void handle(Player player) {
        ProgressionHelper.showHoneToastClient(itemStack);
    }
}
