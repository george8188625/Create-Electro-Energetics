<div align="center">
<img width="256" height="256" alt="rsz_cee-icon" src="https://github.com/user-attachments/assets/270cddf1-07b0-40b7-9ad0-d918386c4bdd" />
<h1>Create: Electro Energetics</h1>
</div>

<h3>Overview:</h3>
<p>Most implementations of electricity in Minecraft mods are really basic, like a one-way fluid simulation.</p>
<p>This implementation is different, it <b>realistically simulates</b> current flow. Sources now induce voltage instead of generating an abstract unit called 'fe', devices don't consume these units, instead they allow <b>current flow</b> and use that current flow to do work. This allows for short-circuits, voltage drops, energy losses etc.</p>

The name comes from polish "elektroenergetyka", which means electrical power engineering.

<h3>Usage:</h3>
<p>Electricity provides a nice and elegant way of <b>moving energy</b>. Additionally, electricity can be used directly with electrical components like pumps, heaters etc. Recipes requiring electricity, Forge Energy powered blocks, through a converter, and electrified railways. Yes, real working electrified railways.</p>
<p>Since this implementation has nothing to do with FE, there is a special block to convert electricity - FE.</p>

## Overall direction of the mod:
The primary focus is to provide an elegant way to move energy through electricity.

In fancy words this mod focuses on generation, transmission, distribution and utilization of electric **power**.

This mod aims to:
- Make electricity easy to use and forgiving enough, for people without any electrical background to be able to use the mod.
- Make electricity involving enough, so it's actually a challenge to set up an electric grid.
- Provide an elegant and polished implementation of electricity.
- Provide an alternative to existing electricity implementations and typical patterns in them (aka thinking outside the box).

Complexity:
- Instead of teaching formulas, this mod will teach how things behave.
- While there is work done on AC, this mod will stay DC by default. Not because of technical challenges, but to make it easy to use.
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

### If you want to read more for some reason, I will expand on a few things below

AC will not be included in the mod by default, for the following reasons:
- The mod is supposed to make electricity accessible, simple and forgiving.
- If the player has to figure out everything about AC (frequency, 3-phase, impedance, inductance, reactance, power factor etc.) before making a single transmission line, nobody* will want to play this mod.
- People who want, would be able to install that AC addon I mentioned above, which will be created shortly after the release, and the existing AC content will be moved there.
- Teaching Minecraft players about power factor etc. is not feasible, sorry.
- I'm aware magic DC transformers are not realistic, however to teach something you kinda have to lie. You can't teach someone electricity by having them learn everything at once. This is Minecraft, if a player doesn't get something working in the span of 5 minutes, they will just install another electricity mod.

This mod does not aim to be the most realistic electricity mod on the planet. I don't think it needs to be. If you want something really realistic, you will be able to install the CEE addon I mentioned.

In the ponders you will see some non-technical language used, that directly relates to the stuff above.

*nobody - not literally nobody, but only the nerds would play, which is not a lot of people.