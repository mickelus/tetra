package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.network.AbstractPacket;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.util.ItemStackTagHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class UpdateBoosterPacket extends AbstractPacket {

    private boolean active;
    private boolean charged;

    public UpdateBoosterPacket() {
    }

    public UpdateBoosterPacket(boolean active) {
        this(active, false);
    }

    public UpdateBoosterPacket(boolean active, boolean charged) {
        this.active = active;
        this.charged = charged;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(active);
        buffer.writeBoolean(charged);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        active = buffer.readBoolean();
        charged = buffer.readBoolean();
    }

    @Override
    public void handle(Player player) {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(player);

        if (!itemStack.isEmpty() && UtilBooster.canBoost(itemStack)) {
            ItemStackTagHelper.mutate(itemStack, tag -> UtilBooster.setActive(tag, active, charged));

            // UtilToolbelt.updateBauble(player);
        }

    }

}
