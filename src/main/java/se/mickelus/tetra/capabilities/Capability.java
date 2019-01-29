package se.mickelus.tetra.capabilities;

/**
 * Capabilities are provided by item modules and describe what an item can be used for. Capabilities have a level, and
 * most also makes use of an efficiency value. Several capabilities map to vanilla harvest tools, e.g. a module with the
 * pickaxe capability makes it possible to use the item to break stone and ore. The level of the capability maps to the
 * harvest level, note that capability levels start at 1 while the harvest tool levels start at 0, e.g pickaxe
 * capability level 4 is required to harvest obsidian. The harvest/mining speed is based on the capability efficiency
 * multiplied by the item attack speed.
 *
 * Capabilities from different modules do not stack, major modules have capability levels and efficiencies added to its
 * own
 *
 * In json config files the provided capabilities are expressed as an object, example with hammer level 2 and
 * axe level 1 with efficiency 0.93:
 * {
 *     "hammer": 2,
 *     "axe": [1, 0.93]
 * }
 */
public enum Capability {

    /**
     * Hammer: The hammer capability is used for crafting modules, and for some block interactions. A higher
     * level allows for more complex modules to be crafted and more difficult materials to be used.
     * Efficiency is currently unused, but may come to affect block interaction success rates.
     */
    hammer,

    /**
     * Axe: The axe capability maps to the axe harvest tool, it allows players to harvest blocks which require an
     * axe to harvest such as logs or planks. It is also used for crafting modules from wooden materials.
     */
    axe,

    /**
     * Pickaxe: The pickaxe capability maps to the pickaxe harvest tool, it allows players to harvest blocks which
     * require a pickaxe to harvest such as stone or ore.
     */
    pickaxe,

    /**
     * Shovel: The shovel capability maps to the shovel harvest tool, it allows players to harvest blocks which
     * require a shovel to harvest such as dirt or soul sand.
     */
    shovel,

    /**
     * Cut: The cut capability makes it possible to harvest blocks that are usually harvested using swords or shears,
     * but does not map to any vanilla harvest tool.
     * todo: make harvest level affect what can be harvested
     * todo: drop leaf blocks if harvest level is high enough
     */
    cut,

    /**
     * Pry: Used for salvaging blocks.
     */
    pry
}
