package se.mickelus.tetra.compat.curios;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.SlotTypeMessage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CuriosCompat {
    public static final String modId = "curios";
    public static final Boolean isLoaded = ModList.get().isLoaded(modId);

    public static void enqueueIMC(InterModEnqueueEvent event) {
        if (CuriosCompat.isLoaded) {
            InterModComms.sendTo(modId, SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("belt").size(1).build());
        }
    }
}
