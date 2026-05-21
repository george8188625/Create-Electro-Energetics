<div align="center">
<img width="256" height="256" alt="rsz_cee-icon" src="https://github.com/user-attachments/assets/270cddf1-07b0-40b7-9ad0-d918386c4bdd" />
<h1>Create: Electro Energetics</h1>
</div>

<h3>Overview:</h3>
<p>Most implementations of electricity in Minecraft mods are really basic, like a one-way fluid simulation.</p>
<p>This implementation is different, it <b>realistically simulates</b> current flow. Sources now induce voltage instead of generating an abstract unit called 'fe', devices don't consume these units, instead they allow <b>current flow</b> and use that current flow to do work. This allows for short-circuits, voltage drops, energy losses etc.</p>

The name comes from polish "elektroenergetyka", which means electrical engineering, in the context of T&D.

<h3>Usage:</h3>
<p>Electricity provides a nice and elegant way of <b>moving energy</b>. Additionally, electricity can be used directly with electrical components like pumps, heaters etc. Recipes requiring electricity, Forge Energy powered blocks, through a converter, and electrified railways. Yes, real working electrified railways.</p>
<p>Since this implementation has nothing to do with FE, there is a special block to convert electricity - FE.</p>

## Overall direction of the mod:
The primary focus is to provide an elegant way to move energy. The other stuff too.

This mod aims to:
- Make electricity easy to use and forgiving enough, for people without any electrical background to be able to use the mod.
- Provide an elegant and polished implementation of electricity.
  
Complexity:
- Instead of teaching formulas, this mod will teach how things behave.
- While there is work done on AC, this mod will stay DC by default. Not because of technical challanges, but to make it easy to use.
- When the mod comes out, a separate addon for this mod will be made, which will add AC content and configure the simulation for it automatically.
- Other than that, the behavior of electricity will be realistic.

## Technical:
<p><b>Electricity stays functional on unloaded chunks</b>. Most electric components work on unloaded chunks, which may be really useful when making long transmission lines and on <b>multiplayer</b>.</p>
<p>The electrical simulation also runs on a separate thread.</p>

## Content:
- Transformers, alternators, motors, electrical grid equipment
- Energy meters, electrical pumps, bulbs
- Relays, diodes, capacitors, potentiometers
- Electric trains
- Electricity-related warning signs
- FE Converter

## Download:

This mod is currently in **open beta**, but it's quickly approaching a **release**.

**When testing please use the latest version:**
<br>
Download from [here](https://github.com/george8188625/Create-Electro-Energetics/releases)

**If you really need a stable version:**
<br>
Latest stable version: [Build 92](https://github.com/george8188625/Create-Electro-Energetics/releases/tag/autobuild-92)

Check out the [checklist](https://github.com/george8188625/Create-Electro-Energetics/blob/1.21.1/checklist.md).

**Join the discord server:**
<br>
<a href="https://discord.gg/crdN2xhQP2"><img src="https://img.shields.io/discord/1121792423836799128?color=5865f2&label=Discord"></a>
<br>
<b>Transmission lines:</b>
<img width="1883" height="958" alt="high voltage transmission towers" src="https://github.com/user-attachments/assets/c7e5d880-700a-4162-a627-b06d8a9c2b3f" />
<br>
<b>Electric train:</b>
<img width="1798" height="910" alt="obraz" src="https://github.com/user-attachments/assets/978bb75e-9965-4f48-a5dd-422d9bbff87c" />
Other mods used in this build:
- Create Deco
- Create: Copycats+
- Create Bits 'n' Bobs
- Create: Diesel Generators
- Create Design n' Decor
- Create: Steam 'n' Rails (unofficial neoforge port)
- Create Railways Navigator
