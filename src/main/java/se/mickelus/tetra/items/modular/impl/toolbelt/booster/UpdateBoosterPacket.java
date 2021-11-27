package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.network.AbstractPacket;

public class UpdateBoosterPacket extends AbstractPacket {

    private boolean active;
    private boolean charged;

    public UpdateBoosterPacket() { }

    public UpdateBoosterPacket(boolean active) {
        this(active, false);
    }

    public UpdateBoosterPacket(boolean active, boolean charged) {
        this.active = active;
        this.charged = charged;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBoolean(active);
        buffer.writeBoolean(charged);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        active = buffer.readBoolean();
        charged = buffer.readBoolean();
    }

    @Override
    public void handle(PlayerEntity player) {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(player);

        if (!itemStack.isEmpty() && UtilBooster.canBoost(itemStack)) {
            UtilBooster.setActive(itemStack.getTag(), active, charged);

            // UtilToolbelt.updateBauble(player);
        }

    }

}
