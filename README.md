**Create: Electro - Energetics**
<br>

simple create addon that adds realistic electricity

Most implementations of electricity in minecaft mods are really basic ( like a one-way fluid simulation ). This implementation is different, it realistically simulates current flow. Sources now induce voltage instead of generating an abstract unit called 'fe', devices don't consume these units, they allow current flow and use it to do things. This allows for short-circuits, voltage drops, energy lossess etc.

Since wires have resistance, at some point players may need to use higher voltages to prevent wire-breaking and voltage drops. This naturally pushes players to build bigger transmission lines, build substations, etc.

Since the simulation isn't based on BE (block-entities), it stays functional outside of loaded chunks, Most devices ( except alternators, motors, FE converters and devices requiring block interaction ) work on unloaded chunks, which may be really useful when making long transmission lines and on multiplayer ( now that I mention that there also will be energy meters ).

Since it has nothing to do with FE, there is a special block to convert electricity - FE.

Performance seems to be really good, even tho the simulation is really convoluted and runs each tick ( On my modded server with a couple of friends, with an extensive electric grid, it takes ~1ms / tick to solve everything ).

<br>
<img src="https://i.imgur.com/scTN1R3.png">
this is a simple transmission line i built
