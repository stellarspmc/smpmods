# SpaceMC Roadmap

## v7.3-pre4

### Treasure System Rework (Priority)
* **Core Idea:** Align the Treasure Module with the Fishing update, featuring 8 rarity tiers, a custom pickaxe progression tree, vanilla tool integration, and a reworked chunk-based exhaustion system.
* **Technical Requirements:**
* 8 rarity loot tier tables
* Pickaxe progression tree (cross emoji) & chunk digging limit logic (tick emoji)

### Stock Market Screen Monitors
* **Core Idea:** In-world physical display screens at spawn to display live market trends, integrated with quick-trade market NPCs for instant command-free transactions.
* **Technical Requirements:**
* Text/Item display entity rendering system
* Quick-trade NPC interaction system
* In-world market management UI

### Planting Rework
* **Core Idea:** wip
* **Technical Requirements:**
* wip

### Shop Rework
* **Core Idea:** Adding NPCs as a shop alternative would make the shops more engaging than barrels.
* **Technical Requirements:**
* Hooking up shop system with NPCs

## v7.3-pre5

### Fishing Re-Rework
* **Core Idea:** Interactive fishing mechanics where catches are spawned as active living mobs instead of static item drops, featuring streak-based mob events.
* **Technical Requirements:**
* Fish mob entity abstraction layer
* Streak-based spawn mechanics (triggered on 3+ catch streaks)

### Custom Home & Navigation System
* **Core Idea:** Custom navigation suite featuring upgradable home limits, `/home`, `/back`, `/spawn`, and an economy-integrated random teleport system (`/rtp`).
* **Technical Requirements:**
* Custom home data storage & migration handler
* Core navigation commands (`/home`, `/back`, `/spawn`)
* Economy-integrated `/rtp` ($300–$3000 scaling cost)

## v7.3-pre6

### Combat Logging Protection
* **Core Idea:** Anti-combat-logging protection to maintain fair PvP gameplay and prevent players from disconnecting mid-combat to escape death.
* **Technical Requirements:**
* Combat tagged state timer & event listener
* Disconnect penalty & combat-pawn handler

### Display Cases
* **Core Idea:** display cases
* **Technical Requirements:**
* item/block displays for different types of cases (jar? / glass? / stuff like that)

## v7.3-rc1

### Bazaar Economy System
* **Core Idea:** Player-driven market where real supply and demand directly set item values, replacing static price multipliers with dynamic buy/sell orders.
* **Technical Requirements:**
* Dynamic order book system for buy/sell matching
* Automated supply and demand price scaling engine

### ATM & Banking Kiosks
* **Core Idea:** Interactive physical ATM kiosks placed in the world, allowing players to manage balances, make cash deposits, and transfer funds without relying on commands.
* **Technical Requirements:**
* Physical ATM block/entity UI interface
* Currency item deposit and withdrawal transaction handler

# SpaceMC Future Proposals
### Cosmetics & Customization
* **Core Idea:** Unlockable player cosmetics, custom particle trails, and visual cosmetic wearables.
* **Technical Requirements:**
* Cosmetic wardrobe GUI & unlock state storage
* Particle effect renderer & visibility toggles

### Pets
* **Core Idea:** Unlockable pets with 4 main features: Index, Inventory system, RNG mechanic and buffs.
* https://discord.com/channels/964789575669137470/1536957411708108923/1536957411708108923
* **Technical Requirements:**
* wip