package se.mickelus.tetra.items.duplex_tool;

import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.data.HandheldModuleData;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.MultiSlotModule;
import se.mickelus.tetra.module.data.ImprovementData;

public class DuplexHeadModule extends MultiSlotModule<HandheldModuleData> {
    public DuplexHeadModule(String slotKey, String moduleKey, String slotSuffix) {
        super(slotKey, "duplex/" + moduleKey, slotSuffix);

        // this uses the unsuffixed module key, to use the same data for both sides
        data = DataHandler.instance.getModuleData("duplex/" + moduleKey, HandheldModuleData[].class);

        improvements = DataHandler.instance.getModuleData("duplex/improvements/" + moduleKey, ImprovementData[].class);

        // this uses the suffixed module key, to avoid passing the slot key to every method that makes use of data
        ItemUpgradeRegistry.instance.registerModule(this.moduleKey, this);
    }
}
