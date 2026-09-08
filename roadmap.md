# SpaceMC Feature Proposals & Roadmap

## Active Concepts

### 1. Rotating Market Pool (Priority)
* **Target Release:** v7.3-pre4
* **Core Idea:** Secondary rotation pool for `/market` containing consumable and high-value utility items (Totems, Gaps, Shulker Boxes, Netherite Templates).
* **Technical Requirements:**
  * task schedule (2-4hrs)
  * new cmd for rotation pool

### 2. Stock Market-Like Screen Monitor
* **Target Release:** v7.3-pre4
* **Core Idea:** Display of the `/market` command as a way to show market data into the world, integrating the economy system to fit more into the vanilla game.
* **Technical Requirements:**
  * display via text / item display
  * npc to instant trade without the need of commands
  * gui for the market (easier management)

### 3. Hypixel Bazaar-Like System
* **Target Release:** v7.3
* **Core Idea:** To remove the necessary 1.1x / 0.9x multiplier for instant selling, creating a complete loop of supply and demand.
* **Technical Requirements:**
  * wip...

### 4. ATM
* **Target Release:** v7.3
* **Core Idea:** ATM for players to control balance and deposits, decreasing the capabilities to understand commands.
* **Technical Requirements:**
  * wip...

### 5. Treasure Rework (Priority)
* **Target Release:** v7.3-pre4
* **Core Idea:** Align the Treasure Module with the Fishing Module update to feature 8 rarity tiers, a custom pickaxe progression tree, vanilla tool integration, and a reworked chunk-based exhaustion system.
* **Technical Requirements:**
  * new rarity tiers

### 6. Fishing Re-Rework
* **Target Release:** v7.3-pre5
* **Core Idea:** Fish mobs to show that fishes aren't just a static item, increasing fun.
* **Technical Requirements:**
  * fish mob abstraction
  * fish mob spawn mechanic (3+ fish streak is the plan rn)

### 7. Dynamic NPC System (finished)
* **Target Release:** v7.3-pre4
* **Core Idea:** NPC system to make creating NPCs not a hassle.
* **Technical Requirements:**
  * npc command abstraction
  * npc functionality abstraction (using class extensions? probs lol) (nah too complicated)

### 8. Quest System (Priority)
* **Target Release:** v7.3-pre4
* **Core Idea:** Fully finish quest system.
* **Technical Requirements:**
  * idk
  
### 9. Cosmetics
* **Target Release:** v7.3
* **Core Idea:** Cosmetics, wip
* **Technical Requirements:**
  * wip

### 10. Home Re-write
* **Target Release:** v7.3-pre5
* **Core Idea:** The /home provided by essentialcommands is too prone to break with mixins, implementing my own command is better
* **Technical Requirements:**
  * /home (port old homes to new home system)
  * /back 
  * /spawn
  * /rtp (cost money, maybe $300 - $3000?)

### 11. Combat Logging
* **Target Release:** v7.3-pre6
* **Core Idea:** not elaborating
* **Technical Requirements:**
  * probably find a mod to do

### 12. 26.3 Port
* **Target Release:** when 26.3 drops
* **Core Idea:** not elaborating
* **Technical Requirements:**
  * wait for mods to catch up / port