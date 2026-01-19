## Create: Electro - Energetics
Most implementations of electricity in Minecraft mods are really basic ( like a one-way fluid simulation ). This implementation is different, it **realistically simulates** current flow. Sources now induce voltage instead of generating an abstract unit called 'fe', devices don't consume these units, they allow **current flow** and use it to do things. This allows for short-circuits, voltage drops, energy losses etc.

Electricity provides a nice and elegant way of **transferring energy**. Instead of making a super long underground shaft (Obviously shafts are way more efficient but that's not the point), players can build transmission lines. Losses are as low as possible to encourage the use of electricity, even if that may not be as realistic. Additionally, electricity can be used directly with electrical components like pumps, heaters etc. Recipes requiring electricity, Forge Energy powered blocks, through a converter, and electrified railways. Yes, real working electrified railways.

The simulation isn't based on BEs (block-entities) and stays functional **outside loaded chunks**, Most devices ( except alternators, motors, FE converters and devices requiring block interaction ) work on unloaded chunks, which may be really useful when making long transmission lines and on **multiplayer** (now that I mention that there also will be energy meters).

Since it has nothing to do with FE, there is a special block to convert electricity - FE.

The electrical simulation also runs on a separate thread.

## Download:

This mod is currently in **open beta**, but it's quickly approaching a **release**.

**When testing please use the latest version:**
<br>
Download from [here](https://github.com/george8188625/Create-Electro-Energetics/releases)

**If you really need a stable version:**
<br>
Latest stable version: [Build 35](https://github.com/george8188625/Create-Electro-Energetics/releases/tag/autobuild-35)

The mod will **release** after the latest version is confirmed to be stable, and the following features are implemented:
- All ponders
- Placement helpers
- Advancements
- More info for the user for electric trains, accumulators etc.

Then, after the first release, all features from the [checklist](https://github.com/george8188625/Create-Electro-Energetics/blob/1.21.1/checklist.md) are gonna be added.

**Join the discord server:**
<br>
<a href="https://discord.gg/crdN2xhQP2"><img src="https://img.shields.io/discord/1121792423836799128?color=5865f2&label=Discord"></a>
<br>
<img width="1883" height="958" alt="high voltage transmission towers" src="https://github.com/user-attachments/assets/c7e5d880-700a-4162-a627-b06d8a9c2b3f" />
