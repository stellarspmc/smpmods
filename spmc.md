# SpaceMC Server Mechanics Guide

## Player Economy System (6.4)

### Ore Baseline Valuation & Gem Deposits (6.6)
The SpaceMC economy functions like a real-world stock exchange.

Diamonds serve as the server’s permanent financial baseline ($100 per diamond). 

All other trade minerals float dynamically based on player supply, demand, and organic market drift.

To simulate a realistic market, exact algorithmic formulas are **not given** to players.

| Ore / Gem Item       | Base Deposit Value |
|----------------------|--------------------|
| **Heart of the Sea** | $2,000             |
| **Nether Star**      | $1,250             |
| **Netherite Ingot**  | $750               |
| **Diamond**          | **$100**           |
| **Echo Shard**       | $50                |
| **Gold Ingot**       | $10                |
| **Emerald**          | $5                 |
| **Iron Ingot**       | $2                 |
| **Lapis Lazuli**     | $1                 |
| **Redstone Dust**    | $0.5               |
| **Copper Ingot**     | $0.2               |
| **Coal**             | $0.1               |
| **Amethyst Shard**   | $0.05              |

#### Commands & Controls
* `/balance [player]` — Check your current cash balance or another player's balance.
* `/deposit [all]` — Convert ores/gems from inventory into bank account funds.
* `/withdraw <amount>` — Convert bank balance back into physical currency items.
* `/send <player> <amount>` — Transfer money directly to another player.
* `/baltop [page]` — View the server wealth leaderboard.
* `/market` — View current dynamic market prices for fluctuating trade ores.

#### Market Behaviour
1. Supply & Demand Impact
    * Mass depositing minerals increases market supply and depresses buy prices.
    * Purchasing or withdrawing minerals increases scarcity and drives prices up.
2. Automated Market Decay
    * If a mineral receives no buy/sell transactions for 2.5 minutes (150 seconds), its price enters an automated drift cycle.
    * Inactive minerals have a **60% chance** to decay back toward their base equilibrium price, and a **40% chance** to experience natural market drift.
3. Dynamic Server Scaling
    * Market evaluation cycles adjust dynamically depending on how many players are online, ensuring the economy stays active during peak hours while conserving server resources during quiet periods.
4. Laziness Prevention
    * To punish laziness, deposits could be compressed to blocks, but giving a 7% decrease rate, encouraging players to deposit slowly.

### Player Chest Shops (6.6)
The Shop System allows players and admins to set up automated physical stores using Barrels and Signs. Displays (holograms and 3D item floating models) automatically render above the shop barrel to showcase items, batch sizes, live stock, and pricing.

Full cross-platform support is included for both Java and Bedrock (Geyser/Floodgate) players with custom-tailored user interfaces.

#### How to Create a Shop
1. Place a **Barrel** on the ground.
2. Place a **Sign** directly on top of the barrel.
3. Hold the item you want to sell in your main hand.
4. Edit the sign text:
    - Line 1: Type `[shop]`.
    - Line 2: Type the price (e.g. `10.50` or `$10.50`).
5. Upon saving, the sign will automatically be destroyed and converted into **a floating 3D Item & Text Hologram**.
* **TODO: include photo / video support**

#### Shop Types
* Player Shops: Stock is drawn directly from the barrel below. Earned cash goes straight to the owner's bank balance upon purchase.
* Creative Shops: Infinite stock. Sales revenue is voided rather than credited to a player account.

#### Shop Interaction Controls
**Shop Owners:**
* `Shift + Right Click` — Open shop owner management menu.
* `Shift + Left Click` — Destroy the shop.

**Customers:**
* `Right Click` — Purchase item.
* `Left Click` — View item details and stock information.

**TODO: include photo**

### Income Streams
* **Playtime Allowance:** Earn **$70 / hour** passively while logged in (tracked and updated once every minute).
* **Treasure Drops:** Base payout of **$3 per treasure**. Includes an anti-exploit decay rate for high-speed farming:
* **Cooldown Buffer:** 10 minutes between full-value drops.
* **Gem Deposits:** Deposit mined ores directly via `/deposit` or sell via `/market`.

## Industrial Crafting (7.3)

### Compressor Recipes

Refine raw carbon and nether materials into high-tier industrial components inside the Compressor.

| Input Material    | Quantity | Output Material         | Quantity |
|-------------------|----------|-------------------------|----------|
| Charcoal          | 16x      | Carbon                  | 1x       |
| Coal              | 48x      | Carbon                  | 1x       |
| Carbon            | 12x      | Compressed Carbon       | 1x       |
| Compressed Carbon | 4x       | Carbon Chunk            | 1x       |
| Nether Brick      | 32x      | Compressed Nether Brick | 1x       |

## Community Systems & Activities

### Vault System (6.7)
The Vault tracks community-wide donation goals and grants server-wide perks upon milestone completion.

* **Hub Interaction:** Punch the `vault master` in the server hub.
* **Left Click:** Check current contribution progress and tier milestones.
* **Right Click:** Donate items or funds directly into the Vault.

* **Command Access:** Run `/vault` anywhere in the world to view info and progress.

### Better Fishing System

Fishing includes custom mechanics, rod progression, badges, and quests.

* **Minigame Hook Mechanics:** When a fish bites, a timing bar appears. Press **SPACE** when the indicator aligns with the **green sweet spot** to successfully land the catch.
* **Progression Unlocks:** Earn money to purchase custom upgraded fishing rods with higher catch probabilities and unique loot tables.

### Lodestone Chunk Loading & Automation

* **Chunk Loading:** Placing a Lodestone force-loads its surrounding chunk area.
* **Entity Persistence:** Keeps entities inside loaded chunks persistent to prevent despawning.
* **Fakeplayer Integration:** Automation designs utilizing fakeplayers require a Lodestone to keep chunks active and functional.

### Quality of Life & Server Mechanics

* **Mob Variants:** Wild mobs occasionally spawn with custom variants (such as `nickwong` and `eye` variants).
* **Happy Ghasts:** Ghast flying speed is fixed at 3x default speed to match Elytra travel rates.
* **Death Messages:** Death coordinates log cleanly as `at (x, y, z)`. Bedrock crossplay players trigger a Bedrock death grave upon dying.
* **Core Commands:** `/home` (2 home limit), `/surface` (teleport to world surface), `/mapart` (render canvas maps).

---

### Treasure System (6.35)

Treasures grant dynamic rewards while preventing automated or high-speed farming.

* **Base Payout:** $3 per treasure.
* **Cooldown Buffer:** 10-minute default cooldown timer between full-value drops.
* **Diminishing Returns Formula:** Consecutive triggers within the cooldown window apply an exponential probability drop:

$$\text{Drop Chance} = 0.85^x$$ (wip, changed)

*(where $x$ is the number of consecutive triggers without waiting for the cooldown).*