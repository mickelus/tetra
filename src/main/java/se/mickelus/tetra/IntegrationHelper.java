package se.mickelus.tetra;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.SlotTypeMessage;

public class IntegrationHelper {
    public static final String curiosModId = "curios";
    public static final Boolean isCuriosLoaded = ModList.get().isLoaded(curiosModId);

    public static void enqueueIMC(InterModEnqueueEvent event) {
        if(isCuriosLoaded) {
            InterModComms.sendTo(curiosModId, SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("belt").size(1).build());
        }
    }
}
