package se.mickelus.tetra.module;

/**
 * Item effects are used by modules to apply various effects when the item is used in different ways, or to alter
 * how the item can be used. Item effects have a level, and some also makes use of an efficiency value.
 *
 * In json config files the effects are expressed as an object. Example with bleeding level 2 and
 * sweeping level 1 with efficiency 3.4:
 * {
 *     "bleeding": 2,
 *     "sweeping": [1, 3.4]
 * }
 */
public enum ItemEffect {

    /////////////////////////////////////////////////////////////
    // tools & weapons
    //////////////////////////////////////////////////////////////

    /**
     * Bleeding: When hitting living entities there is a 30% chance to cause the entity to bleed for 2 seconds. Bleeding
     * entities take damage 2 times per second, the damage taken is equal to the level of the bleeding effect.
     * todo: make effect efficiency affect duration
     */
    bleeding,

    /**
     * Backstab: Hitting entities from behind cause critical hits, dealing percentage based bonus damage. Attacker has
     * to be within a 60 degree cone centered around the targets back to trigger the effect.
     * The bonus damage is 25% plus an additional 25% times the level of the effect. E.g. for backstab level 2 the bonus
     * damage would be 25% + 25% * 2 = 75% bonus damage.
     */
    backstab,

    /**
     * Armor penetration: Hitting entities will always deal damage regardless of the targets armor. The minimum damage
     * dealt equals the level of the effect.
     * todo: make the minimum damage not able to exceed the items actual damage
     */
    armorPenetration,

    /**
     * Unarmored bonus damage: Deals bonus damage when attacking entities with 0 armor. Bonus damage dealt equals the
     * level of the effect.
     * todo: use effect efficiency for armor threshold
     */
    unarmoredDamage,

    /**
     * Sweeping: Attacking an entity also hits all enemies adjacent to the target, similar to the vanilla sweeping edge
     * enchant.
     * - deals 12.5% of weapon damage per sweeping level to sweeped entities, minimum damage is 1 before reductions
     * - knocks sweeped entities back, if sweeping level is 4 or above knockback strength is affected by the knockback
     *   effect level
     * - the sweeping effect only triggers if attack cooldown is 0.9 or above
     * - the effect efficiency affects the area in which entities are hit, ( 1 + efficiency ) blocks
     * todo: apply additional effects to sweeped targets at high levels
     */
    sweeping,

    /**
     * Striking: Hitting a block instantly breaks it and triggers attack cooldown. Only applies if the corresponding
     * harvest capability is high enough to harvest the block and if attack cooldown is above 0.9. Different item
     * effects for different harvesting tools.
     * todo: stop really durable blocks from being instantly broken
     */
    strikingAxe,
    strikingPickaxe,
    strikingCut,
    strikingShovel,

    /**
     * Sweeping strike: Cause the striking effect to instantly break several blocks around the hit block.
     * todo: add more variation and make break count depend on sweeping & capability efficiency + block hardness
     */
    sweepingStrike,

    /**
     * Unbreaking: Reduces the chance that the item will take durability damage. There is a 100 / ( level + 1 ) % chance
     * that the item will take damage. Uses the same mechanic as the vanilla does for the unbreaking enchantment.
     */
    unbreaking,

    /**
     * Flattening: Rightclicking a grass block converts it to a path block, the same behaviour as vanilla shovels.
     */
    flattening,

    /**
     * Tilling: Rightclicking a grass block converts it to a farmland block, the same behaviour as vanilla hoes.
     */
    tilling,

    /**
     * Stripping: Rightclicking a log converts it to a stripped log of the same sort of wood, the same behaviour as vanilla axes.
     */
    stripping,

    /**
     * Blocking: Allows the item to block incoming attacks and projectiles while rightclick is held down. Has infinite duration if no
     * efficiency value is given, otherwise the level defines how many seconds the block be held and the efficiency defines the cooldown
     * period.
     */
    blocking,

    /**
     * Bashing: Right-clicking an entity will knock it back, damage it, stun it and apply enchantment & item effects from the item. A stunned entity
     * cannot move, deals no damage if it attacks and has infinite swing cooldown.
     * The level of the effect + the level of the knockback enchantment on the item determines the knockback distance, using the same formulae as the
     * knockback enchantment usually would.
     * The efficiency of the effect determines the duration of the stun effect, 1.5 efficiency => 1.5 second duration.
     */
    bashing,

    /**
     * Armor: Provides armor when held in the mainhand or the offhand. Armor value provided equals the effect level.
     */
    armor,

    /**
     * Toughness: Provides armor toughness when held in the mainhand or the offhand. Toughness value provided equals the effect level.
     */
    toughness,

    /**
     * Counterweight: Increases the attack speed based on how much integrity is used by all of the items modules.
     * Increases attack speed by 50% if the effect level is equal to the integrity usage, the AS bonus is reduced by 20%
     * for each point they differ. E.g. If the item has counterweight level 1 and uses 4 durability it's attack speed
     * will be changed by ( 50 - 3 * 20 ) = -10%, so the attack speed would actually be reduced by 10%.
     */
    counterweight,

    /**
     * Quick strike: Reduces the minimum damage dealt when hitting an entity while the attack cooldown is still active.
     * The minimum damage dealt is ( 20 + 5 * level )%.
     */
    quickStrike,

    /**
     * Soft strike: Adds a durability buff improvement to major modules when the item providing the tool capabilities
     * has this effect.
     * todo: not yet implemented, rename to work for all tool classes
     */
    softStrike,

    /**
     * De-nailing: Allows the player to instantly break plank based blocks by using right-click. Plank blocks include
     * plank slabs, bookshelves, doors etc.
     * todo: move to ability once implemented
     * todo: make possible blocks configurable
     */
    denailing,

    /**
     * Fiery self: When the player uses the item there's a chance that the player is set on fire. The player burns for 1 second per level of the
     * effect. The chance for the player to catch fire is based on the effect efficiency, efficiency 1.0 would cause the player to always catch
     * fire and an efficiency of 0.0 would cause the player to never catch fire. The temperature of the biome affect the chance for the effect to
     * trigger, colder biomes reduce it while warmer biomes cause an increase.
     */
    fierySelf,

    /**
     * Ender reverb: Using the item angers nearby endermen. When nearby endermen teleport the player sometimes suffers nausea and will be teleported
     * along with the enderman (also applies for thrown pearls). Breaking blocks or performing actions using the tool will sometimes anger nearby
     * entities from the end (endermen, endermites, shulkers & the dragon). The chance for the effects to occur is based on the effect efficiency,
     * efficiency 1.0 would cause them to always occur and an efficiency of 0.0 would cause them to never occur. Different actions have slightly
     * different chance to trigger effects.
     */
    enderReverb,

    /**
     * Haunted: Using the item has a chance to spawn an invisible vex that holds a copy of the item, the vex will live for 1 second per effect level
     * and the probability for the effect to occur is equal to the effect efficiency.
     *
     * todo: less hack, If the item has a module with the "destabilized/haunted" improvement it's level will be reduced by 1 or removed if its level
     * is 1.
     */
    haunted,

    /**
     * Critical strike: Hitting entities and destroying blocks has a chance to critically strike, the probability is equal to the level of the effect.
     *
     * A critical strike on an entity deals damage multiplied by the effects efficiency, e.g. a 1.5 efficiency would cause a critical strike to deal
     * 150% damage.
     *
     * A critical strike when mining blocks would cause the block to break instantly. Critical strikes can only occur when mining blocks
     * if the efficiency of the required tool is twice (or higher) as high as the blocks hardness.
     */
    criticalStrike,

    /**
     * Intuit: Experience gained from mining blocks or killing entities also yield honing progress equal to the xp gained multiplied by the effect
     * level.
     */
    intuit,

    /**
     * Earthbind: Hitting an entity has a chance to bind them to the ground. Earthbound entities are slowed, have reduced knockback and cannot jump.
     * This effect is more likely to occur at lower y levels, starting at 50% at y level 0 and is reduced down to 0% at y level 128.
     */
    earthbind,

    /**
     * Throwable: Allows the item to be thrown by holding down right click.
     */
    throwable,

    //////////////////////////////////////////////////////////////
    // toolbelt
    //////////////////////////////////////////////////////////////

    /**
     * Booster: Having a toolbelt with this effect and the correct fuel in their inventory allows the player to fly
     * short distances by jumping midair and to jump longer distances by holding shift when jumping. Pressing jump while
     * flying with an elytra also boosts the player similar to how using fireworks would. Effect level 1 barely allows
     * the player to hover, each level increases the strength of the boost.
     * todo: split behaviours into separate item effects
     */
    booster,

    /**
     * Quick slot: Adds quick access slots to a toolbelt, the effect level decides the number of slots at a 1:1 ratio.
     * Any item can be put into a quick access slot and can quickly be placed into the players hand using
     * the toolbelt quick access ui.
     */
    quickSlot,

    /**
     * Storage slot: Adds storage slots to a toolbelt, the effect level decides the number of slots at a 1:1 ratio.
     * Any item can be put into a storage slot, players cannot quickly pull items from storage slots
     * but items can be quickly stored as with other slot types.
     */
    storageSlot,

    /**
     * Potion slot: Adds potion slots to a toolbelt, the effect level decides the number of slots at a 1:1 ratio.
     * Potion items can be put into a potion slot, and can quickly be placed into the players hand using
     * the toolbelt quick access ui.
     */
    potionSlot,

    /**
     * Quiver slot: Adds quiver slots to a toolbelt, the effect level decides the number of slots at a 1:1 ratio.
     * Different types of arrows can be put into a quiver slot, and can quickly be placed into the players hand using
     * the toolbelt quick access ui.
     */
    quiverSlot,

    /**
     * Quick access: Provides quick access to the inventory slots provided by the module, the effect level decides the
     * number of slots affected at a 1:1 ratio.
     */
    quickAccess,

    /**
     * Cell socket: Cells (e.g. a magmatic cell) placed in slots with this effect can provide power to other
     * modules attached to the toolbelt. The effect level decides the number of slots affected at a 1:1 ratio.
     */
    cellSocket,

    //////////////////////////////////////////////////////////////
    // bow
    //////////////////////////////////////////////////////////////

    /**
     * Release latch: Causes the bow to automatically fire when fully drawn.
     */
    releaseLatch,

    /**
     *  Flow: Firing an arrow within short succession of another increases damage by 1.
     *  The effect level decides the damage cap and the effect efficiency decides the timeframe before the bonus is lost.
     */
    flow,

    overbowed;

    public static final String hauntedKey = "destabilized/haunted";
}
