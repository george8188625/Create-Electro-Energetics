## CEE's Electrical Engine

This is an overview and detailed explanation of CEE's electrical engine.

This is an MNA-based electrical engine that abstracts a lot of the simulation to an upper level.

The low level stuff is really abstracted from the engine. You will never have to manually stamp values, read from matrices, worry about things like this.

### Terminology:
- `micro-ticker` A special electrical connection that ticks and can change its electrical properties in between `micro-ticks` 
- `micro-tick` Describes the act of one MNA solve. There can be `2^n` total micro-ticks. `n=0` is the default, `n=5` is the max. `n=3` is a balanced value.
- `node` It's a node
- `electrical properties` a collection of three values: Resistance, Voltage source value and Current source value. These values define everything.
- `device` Devices are data structures that define the behavior of blocks. when a block is placed, a device may be added for that position.
- `preTick` The phase when the electrical network is built.
- `postTick` The phase when the electrical results are interpreted, and world updates are done.
- `simple resistor` A connection that has only one value: `resistance` and is not a *special* connection. This is the simplest and most common connection type. 

### Flow of the engine
1. Main thread - preTick.<br>All devices are ticked, electrical network topology is collected.
2. The simulation is moved to a separate thread.<br> Minecraft then can continue doing whatever it wants on the main thread.
3. DFS is executed on the global electric network - this splits network into separate circuits.
4. Each network grid is optimized - it's topology is dissolved into a state, that behaves the same, but results in a smaller matrix size.  
5. Micro-ticking starts here.<br>Every micro-ticker is ticked before and after the solve. this is repeated multiple times. Each optimization that has been applied is reversed here, to get the original node values. 
6. Everything is moved back onto the main thread
7. postTick is run.<br>All devices can do world updates and stuff.

### Devices
Devices define behavior but do not solve circuits directly.
Notice how devices are ticked, not blocks. Blocks / BEs may access the device and vice versa, but they are separate, and devices aren't loaded, they just exist. Most components don't even need to be block-entities this way.

During the `preTick` and `postTick` phase, two corresponding events are posted onto the `game bus`
- `AddToElectricGraphEvent`
- `FinishElectricSimulationEvent`

### Micro-ticking
`micro-tickers` are created by using an object that extends `MicroTickingElectricalProperties` in place of a normal `ElectricalProperties` object when connecting nodes.

`micro-tickers` are ticked twice per `micro-tick`.

This ticking does not happen on the main thread. You have to put care to make sure it's thread safe.

### Network Topology Optimization
Three optimization methods are used over and over again, until the network is fully optimized:
- Star-Delta transformation
- Series resistor optimization
- Parallel resistor optimization

These only operate on simple resistor connections.

### Edges
Store electrical properties. Can be `micro-tickers` or coupled properties 

### Node
Nodes defined by the mod. These include:
- `InWorldNode` - In world, owned by device. They contain four values: `[id, x, y, z]`, where `id` is local to the block position.
- `AttachedNode` - Not tied to the block grid. They contain 2 values: `[id, ownerID]`, where `ownerID` is a `String`.

### WrappedIndexedNode
For performance, each node that enters the engine is wrapped in this object. It provides two features:
- Direct lookups instead of hashmap lookups 
- Holds adjacency and grounding of the node

Each one has a unique index

### SimulationNode
This is the next level of locality - this one is per network - it's ID defines it's position in the matrix.

### CoupledElectricalProperties
These define ideal DC-DC transformer-like behavior.

### Voltage storage
All voltages are stored in an array of `n*m` size where `n` is the amount of nodes and `m` is the number of micro-ticks

A specific voltage is accessed through: 
```java
double voltage = voltages[(nodeOrdinal << microTickBits) | microTick];
```

### Solving
This engine uses direct solvers.
- cholesky Decomposition is used for SPD matrices.
- LU Decomposition is used for non SPD matrices.

Matrices are sparse.


