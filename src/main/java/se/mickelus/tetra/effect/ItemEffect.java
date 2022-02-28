package se.mickelus.tetra.effect;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ParametersAreNonnullByDefault
public class ItemEffect {
    private static final Map<String, ItemEffect> effectMap = new ConcurrentHashMap<>();
    public static final String hauntedKey = "destabilized/haunted";
    public static final ItemEffect workable = get("workable");
    public static final ItemEffect unstable = get("unstable");

    /////////////////////////////////////////////////////////////
    // tools & weapons
    //////////////////////////////////////////////////////////////

    /**
     * Howling: Swinging the item grants a stacking potion effect that grants (per stack) 12.5% chance to deflect projectile, slow movement speed by -5%
     * and 2.5% chance to send the projectile directly back to where it came from. Effect level determines stacks gained per swing, efficiency
     * determines duration.
     */
    public static final ItemEffect howling = get("howling");

    /**
     * Bleeding: When hitting living entities there is a 30% chance to cause the entity to bleed for 2 seconds. Bleeding
     * entities take damage 2 times per second, the damage taken is equal to the level of the bleeding effect.
     * todo: make effect efficiency affect duration
     */
    public static final ItemEffect bleeding = get("bleeding");

    /**
     * Backstab: Hitting entities from behind cause critical hits, dealing percentage based bonus damage. Attacker has
     * to be within a 60 degree cone centered around the targets back to trigger the effect.
     * The bonus damage is 25% plus an additional 25% times the level of the effect. E.g. for backstab level 2 the bonus
     * damage would be 25% + 25% * 2 = 75% bonus damage.
     */
    public static final ItemEffect backstab = get("backstab");

    /**
     * Armor penetration: Attacks ignore 1% (per effect level) of the targets armor
     */
    public static final ItemEffect armorPenetration = get("armorPenetration");

    /**
     * Crushing: Hitting entities will always deal damage regardless of the targets armor. The minimum damage
     * dealt equals the level of the effect.
     */
    public static final ItemEffect crushing = get("crushing");

    /**
     * Skewering: Deals bonus damage when attacking entities with an armor value lower than the effect efficiency. Bonus damage dealt equals the
     * level of the effect.
     */
    public static final ItemEffect skewering = get("skewering");

    /**
     * Sweeping: Attacking an entity also hits all enemies adjacent to the target, similar to the vanilla sweeping edge
     * enchant.
     * - deals 12.5% of weapon damage per sweeping level to sweeped entities, minimum damage is 1 before reductions
     * - knocks sweeped entities back, if sweeping level is 4 or above knockback strength is affected by the knockback
     * effect level
     * - the sweeping effect only triggers if attack cooldown is 0.9 or above
     * - the effect efficiency affects the area in which entities are hit, ( 1 + efficiency ) blocks
     */
    public static final ItemEffect sweeping = get("sweeping");

    /**
     * Truesweep: Hitting entities with a sweep will also apply item & enchantment effects
     */
    public static final ItemEffect truesweep = get("truesweep");

    /**
     * Striking: Hitting a block instantly breaks it and triggers attack cooldown. Only applies if the corresponding tool level is high enough to
     * harvest the block and if attack cooldown is above 0.9. Different item effects for different harvesting tools.
     */
    public static final ItemEffect strikingAxe = get("strikingAxe");
    public static final ItemEffect strikingPickaxe = get("strikingPickaxe");
    public static final ItemEffect strikingCut = get("strikingCut");
    public static final ItemEffect strikingShovel = get("strikingShovel");
    public static final ItemEffect strikingHoe = get("strikingHoe");

    /**
     * Sweeping strike: Cause the striking effect to instantly break several blocks around the hit block.
     * todo: add more variation and increase max size
     */
    public static final ItemEffect sweepingStrike = get("sweepingStrike");

    /**
     * Extractor: Harvesting a block breaks several adjacent blocks, direction/shape based on the hit face. Breaks 4.5 block per effect level.
     */
    public static final ItemEffect extractor = get("extractor");

    /**
     * Unbreaking: Reduces the chance that the item will take durability damage. There is a 100 / ( level + 1 ) % chance
     * that the item will take damage. Uses the same mechanic as the vanilla does for the unbreaking enchantment.
     */
    public static final ItemEffect unbreaking = get("unbreaking");

    /**
     * Blocking: Allows the item to block incoming attacks and projectiles while rightclick is held down. The level defines how many seconds the block
     * can be held, the duration is infinite if the level is 16 or above.
     */
    public static final ItemEffect blocking = get("blocking");
    public static final ItemEffect blockingReflect = get("blockingReflect");

    /**
     * Bashing: Right-clicking an entity will knock it back, damage it, stun it and apply enchantment & item effects from the item. A stunned entity
     * cannot move, deals no damage if it attacks and has infinite swing cooldown.
     * The level of the effect + the level of the knockback enchantment on the item determines the knockback distance, using the same formulae as the
     * knockback enchantment usually would.
     * The efficiency of the effect determines the duration of the stun effect, 1.5 efficiency => 1.5 second duration.
     */
    public static final ItemEffect bashing = get("bashing");
    public static final ItemEffect ricochet = get("ricochet");
    public static final ItemEffect piercing = get("piercing");

    /**
     * Piercing harvest: Allows items with the piercing effect to break several blocks (in a line) at a time, thrown items that can break blocks
     * can break several blocks. The number of broken blocks depends on the level of the piercing effect.
     */
    public static final ItemEffect piercingHarvest = get("piercingHarvest");

    /**
     * Jab: When held in the offhand, allows use of right-click to attack an entity. Triggers cooldown.
     * The level of the effect is used as a multiplier for the damage, e.g. level 30 of the effect would result in a 0.3 damage multiplier.
     */
    public static final ItemEffect jab = get("jab");

    /**
     * Counterweight: Increases the attack speed based on how much integrity is used by all of the items modules.
     * Increases attack speed by 50% if the effect level is equal to the integrity usage, the AS bonus is reduced by 20%
     * for each point they differ. E.g. If the item has counterweight level 1 and uses 4 durability it's attack speed
     * will be changed by ( 50 - 3 * 20 ) = -10%, so the attack speed would actually be reduced by 10%.
     */
    public static final ItemEffect counterweight = get("counterweight");

    /**
     * Quick strike: Reduces the minimum damage dealt when hitting an entity while the attack cooldown is still active.
     * The minimum damage dealt is ( 20 + 5 * level )%.
     */
    public static final ItemEffect quickStrike = get("quickStrike");

    /**
     * Soft strike: Adds a durability buff improvement to major modules when the item providing the tool tools
     * has this effect.
     * todo: not yet implemented, rename to work for all tool classes
     */
    public static final ItemEffect softStrike = get("softStrike");

    /**
     * Gives attacks a chance to apply the severed effect to targets which reduces maximum health and attack damage.
     * Proc chance is equal to the effect level. Effect stacks up to effect efficiency.
     */
    public static final ItemEffect severing = get("severing");

    /**
     * Gives attacks a 1% (per effect level) chance to stun the target, effect efficiency determines the duration
     */
    public static final ItemEffect stun = get("stun");

    public static final ItemEffect abilityDefensive = get("abilityDefensive");
    public static final ItemEffect abilityOvercharge = get("abilityOvercharge");
    public static final ItemEffect abilityMomentum = get("abilityMomentum");
    public static final ItemEffect abilityCombo = get("abilityCombo");
    public static final ItemEffect abilityRevenge = get("abilityRevenge");
    public static final ItemEffect abilityOverextend = get("abilityOverextend");
    public static final ItemEffect abilityExhilaration = get("abilityExhilaration");
    public static final ItemEffect abilitySpeed = get("abilitySpeed");
    public static final ItemEffect abilityEcho = get("abilityEcho");

    /**
     * Charged right-click ability that deals damage based on targets missing health and the number of negative effects they're affected by.
     * Effect level determines bonus damage from missing health & efficiency determines bonus damage from negative effects
     */
    public static final ItemEffect execute = get("execute");

    /**
     * Charged right-click ability that launches the player forward. If the player lunges into another entity it deals a % (equal to the effect level)
     * of attack damage and reduces the cooldown by 20%
     */
    public static final ItemEffect lunge = get("lunge");

    /**
     * Charged right-click ability that can be used to either slam the ground to damage entities in a cone for 1% (per effect level) attack damage,
     * or to directly slam an entity to deal 1.5% (per effect level) attack damage and cause heavy knockback
     */
    public static final ItemEffect slam = get("slam");

    /**
     * Charged right-click ability that applies a 1 (per effect efficiency) second long 10% (per level of effect) armor reduction debuff if the targets
     * armor is above 5 or is not affected by the puncture effect already, otherwise inflict a 6 second long bleed.
     */
    public static final ItemEffect puncture = get("puncture");

    /**
     * Right-click ability that applies a 1 (per effect efficiency) second long 1 (per level of effect) armor reduction debuff to the target, the effect
     * stacks.
     */
    public static final ItemEffect pry = get("pry");

    /**
     * Charged right-click ability that deals 1% (per effect level) attack damage, applies almost no knockback, and exhausts (AS, MS, breakspeed reduction)
     * the target and the attacker for 1 (per effect efficiency) seconds.
     */
    public static final ItemEffect overpower = get("overpower");

    /**
     * Charged right-click ability that deals 1% (per effect level) attack damage to all entities in an area (based on effect efficiency). The attacker
     * gains a beneficial effect based on how many entities that are killed by the attack, the effect depends on the ability modifier.
     */
    public static final ItemEffect reap = get("reap");

    /**
     * De-nailing: Allows the player to instantly break plank based blocks by using right-click. Plank blocks include
     * plank slabs, bookshelves, doors etc.
     * todo: move to ability once implemented
     * todo: make possible blocks configurable
     */
    public static final ItemEffect denailing = get("denailing");

    /**
     * Fiery self: When the player uses the item there's a chance that the player is set on fire. The player burns for 1 second per level of the
     * effect. The chance for the player to catch fire is based on the effect efficiency, efficiency 1.0 would cause the player to always catch
     * fire and an efficiency of 0.0 would cause the player to never catch fire. The temperature of the biome affect the chance for the effect to
     * trigger, colder biomes reduce it while warmer biomes cause an increase.
     */
    public static final ItemEffect fierySelf = get("fierySelf");

    /**
     * Ender reverb: Using the item angers nearby endermen. When nearby endermen teleport the player sometimes suffers nausea and will be teleported
     * along with the enderman (also applies for thrown pearls). Breaking blocks or performing actions using the tool will sometimes anger nearby
     * entities from the end (endermen, endermites, shulkers & the dragon). The chance for the effects to occur is based on the effect efficiency,
     * efficiency 1.0 would cause them to always occur and an efficiency of 0.0 would cause them to never occur. Different actions have slightly
     * different chance to trigger effects.
     */
    public static final ItemEffect enderReverb = get("enderReverb");

    /**
     * Haunted: Using the item has a chance to spawn an invisible vex that holds a copy of the item, the vex will live for 1 second per effect level
     * and the probability for the effect to occur is equal to the effect efficiency.
     * <p>
     * todo: less hack, If the item has a module with the "destabilized/haunted" improvement it's level will be reduced by 1 or removed if its level
     * is 1.
     */
    public static final ItemEffect haunted = get("haunted");
    public static final ItemEffect stabilizing = get("stabilizing");

    /**
     * Critical strike: Hitting entities and destroying blocks has a chance to critically strike, the probability is equal to the level of the effect.
     * <p>
     * A critical strike on an entity deals damage multiplied by the effects efficiency, e.g. a 1.5 efficiency would cause a critical strike to deal
     * 150% damage.
     * <p>
     * A critical strike when mining blocks would cause the block to break instantly. Critical strikes can only occur when mining blocks
     * if the efficiency of the required tool is twice (or higher) as high as the blocks hardness.
     */
    public static final ItemEffect criticalStrike = get("criticalStrike");

    /**
     * Intuit: Experience gained from mining blocks or killing entities also yield honing progress equal to the xp gained multiplied by the effect
     * level.
     */
    public static final ItemEffect intuit = get("intuit");

    /**
     * Earthbind: Hitting an entity has a chance to bind them to the ground. Earthbound entities are slowed, have reduced knockback and cannot jump.
     * This effect is more likely to occur at lower y levels, starting at 50% at y level 0 and is reduced down to 0% at y level 128.
     */
    public static final ItemEffect earthbind = get("earthbind");

    /**
     * Throwable: Allows the item to be thrown by holding down right click.
     */
    public static final ItemEffect throwable = get("throwable");

    //////////////////////////////////////////////////////////////
    // toolbelt
    //////////////////////////////////////////////////////////////

    /**
     * Shieldbreaker: If an attack from this item is blocked there's a 25% chance for the blocker to have their shield disabled, like vanilla axes.
     * Mobs wielding this are guaranteed to disable shields.
     */
    public static final ItemEffect shieldbreaker = get("shieldbreaker");

    /**
     * Booster: Having a toolbelt with this effect and the correct fuel in their inventory allows the player to fly
     * short distances by jumping midair and to jump longer distances by holding shift when jumping. Pressing jump while
     * flying with an elytra also boosts the player similar to how using fireworks would. Effect level 1 barely allows
     * the player to hover, each level increases the strength of the boost.
     * todo: split behaviours into separate item effects
     */
    public static final ItemEffect booster = get("booster");

    /**
     * Quick slot: Adds quick access slots to a toolbelt, the effect level decides the number of slots at a 1:1 ratio.
     * Any item can be put into a quick access slot and can quickly be placed into the players hand using
     * the toolbelt quick access ui.
     */
    public static final ItemEffect quickSlot = get("quickSlot");

    /**
     * Storage slot: Adds storage slots to a toolbelt, the effect level decides the number of slots at a 1:1 ratio.
     * Any item can be put into a storage slot, players cannot quickly pull items from storage slots
     * but items can be quickly stored as with other slot types.
     */
    public static final ItemEffect storageSlot = get("storageSlot");

    /**
     * Potion slot: Adds potion slots to a toolbelt, the effect level decides the number of slots at a 1:1 ratio.
     * Potion items can be put into a potion slot, and can quickly be placed into the players hand using
     * the toolbelt quick access ui.
     */
    public static final ItemEffect potionSlot = get("potionSlot");

    /**
     * Quiver slot: Adds quiver slots to a toolbelt, the effect level decides the number of slots at a 1:1 ratio.
     * Different types of arrows can be put into a quiver slot, and can quickly be placed into the players hand using
     * the toolbelt quick access ui.
     */
    public static final ItemEffect quiverSlot = get("quiverSlot");

    /**
     * Quick access: Provides quick access to the inventory slots provided by the module, the effect level decides the
     * number of slots affected at a 1:1 ratio.
     */
    public static final ItemEffect quickAccess = get("quickAccess");

    /**
     * Cell socket: Cells (e.g. a magmatic cell) placed in slots with this effect can provide power to other
     * modules attached to the toolbelt. The effect level decides the number of slots affected at a 1:1 ratio.
     */
    public static final ItemEffect cellSocket = get("cellSocket");

    //////////////////////////////////////////////////////////////
    // bow
    //////////////////////////////////////////////////////////////

    /**
     * Suspend self: Pressing jump midair while within range of an active beacon will suspend the player in the air
     */
    public static final ItemEffect suspendSelf = get("suspendSelf");

    /**
     * Release latch: Causes the bow to automatically fire when fully drawn.
     */
    public static final ItemEffect releaseLatch = get("releaseLatch");

    /**
     * Flow: Firing an arrow within short succession of another increases damage by 1.
     * The effect level decides the damage cap and the effect efficiency decides the timeframe before the bonus is lost.
     */
    public static final ItemEffect flow = get("flow");
    public static final ItemEffect overbowed = get("overbowed");
    public static final ItemEffect multishot = get("multishot");
    public static final ItemEffect ammoCapacity = get("ammoCapacity");
    public static final ItemEffect zoom = get("zoom");
    public static final ItemEffect velocity = get("velocity");
    public static final ItemEffect suspend = get("suspend");
    public static final ItemEffect rangeCritical = get("rangeCritical");

    //////////////////////////////////////////////////////////////
    // holosphere
    //////////////////////////////////////////////////////////////

    /**
     * Scanner range: Defines the range of the scanner functionality, the level of the effect decides max distance (in blocks) for scanning. Enables
     * the scanning functionality if level > 0
     */
    public static final ItemEffect scannerRange = get("scannerRange");

    /**
     * Scanner horizontal spread: Defines the width of the scan, the number of hits per sweep will be four times the level of the effect.
     */
    public static final ItemEffect scannerHorizontalSpread = get("scannerHorizontalSpread");

    /**
     * Scanner vertical spread: Defines how many vertical raytraces are performed when scanning, each level of the effect adds two (up & down) more
     * vertical raytraces. The additional raytraces start at +-25 degrees and increments at 5 degrees per raytrace.
     */
    public static final ItemEffect scannerVerticalSpread = get("scannerVerticalSpread");
    private final String key;

    private ItemEffect(String key) {
        this.key = key;
    }

    public static ItemEffect get(String key) {
        return effectMap.computeIfAbsent(key, k -> new ItemEffect(key));
    }

    public String getKey() {
        return key;
    }
}
