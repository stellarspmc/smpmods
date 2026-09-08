# SpaceMC Server Mechanics Guide

## Player Economy System (6.4)

### Ore Baseline Valuation & Gem Deposits (6.6)
The SpaceMC economy functions like a real-world stock exchange.

Diamonds serve as the server’s permanent financial baseline ($100 per diamond).

All other trade minerals and rotating market items float dynamically based on player supply, demand, and organic market drift.

To simulate a realistic market, exact algorithmic formulas are **not given** to players.

#### Permanent Mineral Baseline
This is the main mineral pool, which will not be changed.

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

#### Rotating Market Pool
A secondary pool of high-value consumables and utility items temporarily enters the `/market` on an automated schedule (refreshing every 2–4 hours).

| Rotating Item                           | Base Price | Fluctuation Rate |
|-----------------------------------------|------------|------------------|
| **Enchanted Golden Apple**              | $1,500     | High (4.5x)      |
| **Shulker Shell**                       | $1,200     | High (4.0x)      |
| **Netherite Upgrade Smithing Template** | $750       | Medium (2.5x)    |
| **Totem of Undying**                    | $350       | Moderate (1.5x)  |

#### Commands & Controls

* `/balance [player]` — Check your current cash balance or another player's balance.
* `/deposit [all]` — Convert ores, gems, or active rotation items from inventory into bank account funds.
* `/withdraw <amount>` — Convert bank balance back into physical currency items.
* `/send <player> <amount>` — Transfer money directly to another player.
* `/baltop [page]` — View the server wealth leaderboard.
* `/market` — View current dynamic market prices for all trade items (including active rotation items).

#### Market Behaviour
1. Supply & Demand Impact
   * Mass depositing minerals increases market supply and depresses buy/sell prices.
   * Purchasing or withdrawing minerals increases scarcity and drives prices up.
2. Automated Market Decay & Drift
   * Permanent minerals decay toward equilibrium or drift naturally if inactive.
   * Active temporary rotation items undergo price decay and expires automatically.
3. Automated Rotation Scheduler
   * Every 2 – 4 hours, the server selects a temporary utility item from the rotation pool to inject into `/market`.
4. Dynamic Server Scaling
   * Market evaluation cycles adjust dynamically depending on how many players are online, ensuring the economy stays active during peak hours while conserving server resources during quiet periods.
5. Block Compression Fee (Laziness Penalty)
   * Depositing items in block form (e.g. Gold Blocks, Netherite Blocks) yields a 7% penalty compared to depositing individual ingots/gems, encouraging manual uncrafting or rewarding uncompressed deposits.

### Player Chest Shops (6.6)
The Shop System allows players and admins to set up automated physical stores using Barrels and Signs. Displays (holograms and 3D item floating models) automatically render above the shop barrel to showcase items, batch sizes, live stock, and pricing.

Full cross-platform support is included for both Java and Bedrock (Geyser/Floodgate) players with UIs.

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
* `Shift + Left Click` — Open shop owner management menu.
* `Shift + Right Click` — Destroy the shop.

**Customers:**
* `Left Click` — Purchase item.
* `Right Click` — View item details and stock information.
**TODO: include photo**

### Income Streams
* **Playtime Allowance:** Earn **$70 / hour** passively while logged in (tracked and updated once every minute).
* **Treasure Drops:** Base payout of **$3 per treasure**. Includes an anti-exploit decay rate for high-speed farming:
* **Cooldown Buffer:** 10 minutes between full-value drops.
* **Gem Deposits:** Deposit mined ores directly via `/deposit` or sell via `/market`.

## Industrial Crafting (7.3)

### Compressor Recipes

Refine raw carbon and nether materials into high-tier industrial components inside the Compressor.

| Input Material     | Quantity | Output Material         | Quantity | Note                                       |
|--------------------|----------|-------------------------|----------|--------------------------------------------|
| Charcoal           | 16x      | Carbon                  | 1x       | Coal is not perferred because it costs 48x |
| Carbon             | 12x      | Compressed Carbon       | 1x       |                                            |
| Compressed Carbon  | 4x       | Carbon Chunk            | 1x       |                                            |
| Nether Brick       | 32x      | Compressed Nether Brick | 1x       |                                            |
| Different Raw Ores | Varying  | Compressed Ore          | Varying  | Raw Gold is the best ore, 1x -> 2x ore     |
| Gold Dust          | 16x      | 4-Karat Gold Ingot      | 1x       | Karat Gold could scale up to 24K           |

### Smeltery Recipes

The smeltery is used to create alloy materials.

| Input Material       | Quantity | Output Material         | Quantity |
|----------------------|----------|-------------------------|----------|
| Iron Ingot + Silicon | 1x       | Ferrosilicon            | 1x       |

### Other Recipes

These are other recipes that should be noted by players.

| Input Material                                                  | Output Material  | Crafting Station |
|-----------------------------------------------------------------|------------------|------------------|
| Compressed Nether Brick (x5), Nether Star (x3), Netherite Block | Nether Core      | Crafting Table   |
| Sculk Catalyst (x5), Piston (x3), Redstone Dust                 | Sculk Compressor | Crafting Table   |

**TODO: write the crafting recipes**

## Vault System (6.7)
The Vault tracks community-wide donation goals and grants server-wide perks upon milestone completion.

* **Hub Interaction:** Punch the `Vault Master` in the server hub.
* **Left Click:** Check current contribution progress and tier milestones.
* **Right Click:** Donate items or funds directly into the Vault.

* **Command Access:** Run `/vault` anywhere in the world to view info and progress.

## Better Fishing System (7.0)
Fishing includes custom mechanics, rod progression, badges, and quests.

To know more, please refer to: **TODO: write this part**

* **Minigame Hook Mechanics:** When a fish bites, a timing bar appears. Press **SPACE** when the indicator aligns with the **green sweet spot** to successfully land the catch.
* **Progression Unlocks:** Earn money to purchase custom upgraded fishing rods with higher catch probabilities and unique loot tables.

## Better Planting System (7.3)
stub

## Lodestone Chunk Loading & Automation (6.6)
* **Chunk Loading:** Placing a Lodestone force-loads its surrounding chunk area.
* **Entity Persistence:** Keeps entities inside loaded chunks persistent to prevent despawning.

## Quality of Life & Server Mechanics (6.4 - 7.3)
* **Mob Variants:** Wild mobs occasionally spawn with custom variants (such as `nickwong` and `eye` variants). **TODO: expand**
* **Happy Ghasts:** Ghast flying speed is fixed at 3x default speed to match Elytra travel rates. 
* **Core Commands:** `/home` (7 home limit), `/surface` (teleport to world surface), `/mapart` (render canvas maps). **TODO: expand**

## Treasure System (6.35)
Treasures grant dynamic rewards while preventing automated or high-speed farming. **TODO: rewrite + expand**

* **Base Payout:** $3 per treasure.
* **Cooldown Buffer:** 10-minute default cooldown timer between full-value drops.
* **Diminishing Returns Formula:** Consecutive triggers within the cooldown window apply an exponential probability drop:

$$\text{Drop Chance} = 0.85^x$$ (wip, changed)

*(where $x$ is the number of consecutive triggers without waiting for the cooldown).*

## Questing System (7.3)
* stub