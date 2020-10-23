package se.mickelus.tetra.items.loot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;

public class DragonSinewItem extends TetraItem {
    private static final String unlocalizedName = "dragon_sinew";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static DragonSinewItem instance;

    private static final ResourceLocation dragonLootTable = new ResourceLocation("entities/ender_dragon");
    private static final ResourceLocation sinewLootTable = new ResourceLocation(TetraMod.MOD_ID, "entities/ender_dragon_extended");

    public DragonSinewItem() {
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);

        MinecraftForge.EVENT_BUS.register(new LootTableHandler());
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(World world, Entity entity, ItemStack itemstack) {
        entity.setNoGravity(true);

        return null;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        entity.setMotion(entity.getMotion().scale(0.8f));
        if (entity.world.isRemote && entity.getAge() % 20 == 0) {
            entity.world.addParticle(ParticleTypes.DRAGON_BREATH, entity.getPosXRandom(.2d), entity.getPosYRandom() + 0.2, entity.getPosZRandom(0.2),
                    entity.world.getRandom().nextFloat() * 0.02f - 0.01f, -0.01f -entity.world.getRandom().nextFloat() * 0.01f, entity.world.getRandom().nextFloat() * 0.02f - 0.01f);
        }
        return false;
    }

    public static class LootTableHandler {
        @SubscribeEvent
        public void onLootTableLoad(final LootTableLoadEvent event) {
            if (event.getName().equals(dragonLootTable)) {
                event.getTable().addPool(LootPool.builder()
                        .name(TetraMod.MOD_ID + ":" + unlocalizedName)
                        .addEntry(TableLootEntry.builder(sinewLootTable)).build());
            }
        }
    }
}
