package se.mickelus.tetra.items.journal.gui.craft;

import se.mickelus.tetra.blocks.workbench.gui.GuiSchemaListItem;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.SchemaRarity;
import se.mickelus.tetra.module.schema.SchemaType;
import se.mickelus.tetra.module.schema.UpgradeSchema;
import se.mickelus.tetra.util.Filter;

import java.util.Arrays;
import java.util.function.Consumer;

public class GuiJournalSchemas extends GuiElement {

    private Consumer<UpgradeSchema> onSchemaSelect;

    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    public GuiJournalSchemas(int x, int y, int width, int height, Consumer<UpgradeSchema> onSchemaSelect) {
        super(x, y, width, height);

        this.onSchemaSelect = onSchemaSelect;

        showAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(y));

        hideAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(y - 5))
                .onStop(complete -> this.isVisible = false);
    }

    public void update(ItemModular item, String slot) {
        int offset = 0;
        int pageLines = 8;
        UpgradeSchema[] schemas = Arrays.stream(ItemUpgradeRegistry.instance.getSchemas(slot))
                .filter(schema -> !schema.isHoning())
                .filter(schema -> !schema.getRarity().equals(SchemaRarity.temporary))
                .filter(schema -> !schema.getType().equals(SchemaType.improvement))
                .filter(Filter.distinct(UpgradeSchema::getName))
                .toArray(UpgradeSchema[]::new);

        int count = schemas.length;

        clearChildren();

        for (int i = 0; i < count; i++) {
            UpgradeSchema schema = schemas[i + offset];
            addChild(new GuiSchemaListItem(
                    i / pageLines * 106,
                    i % pageLines * 14,
                    103,
                    schema, () -> onSchemaSelect.accept(schema)));
        }
    }

    @Override
    protected void onShow() {
        super.onShow();
        hideAnimation.stop();
        showAnimation.start();
    }

    @Override
    protected boolean onHide() {
        showAnimation.stop();
        hideAnimation.start();

        return false;
    }
}
