**English** | [中文](docs/README_zh.md)

# Circulation Flow Networks

A Minecraft energy network mod for 1.12.2 / 1.20.1 / 1.21.1. Build cross-distance, cross-dimension energy networks using
nodes and hubs.

---

## Overview

Remember linking up an entire planet's power grid with wireless towers in Dyson Sphere Program? That's exactly where
this mod came from — Minecraft deserves a cable-free energy system where you just place nodes, and they wire themselves
up.

Circulation Flow Networks lets you place various nodes around the world to form a mesh energy network. A **Hub** sits at
the center of each network, connecting nearby **Port Nodes**, **Charging Nodes**, and **Relay Nodes**. The network
automatically detects energy machines in range and handles energy collection and distribution. No manual wiring needed —
nodes within link range connect automatically. Think power poles without the power lines.

Supports Forge Energy (FE), IC2 EU (1.12.2 only), and provides an API for other mods to register custom energy handlers.

---

## Blocks

### Node Pedestal

The foundation for placing nodes. Port Nodes, Charging Nodes, and Relay Nodes must be placed on top of a pedestal.
Breaking the pedestal drops the node above it.

### Port Node

The basic unit of the energy network. Detects energy machines (generators, consumers, storage) within range and connects
them to the network.

- Energy detection range: 8 blocks
- Link range: 12 blocks

### Charging Node

Charges items in the inventory of players within range. Charging slots are configured through the Hub's charging
settings tab.

- Charging range: 5 blocks
- Link range: 8 blocks

### Relay Node

Does not detect energy machines — used solely to extend the network's link distance.

- Link range: 20 blocks

### Hub

The control center of a network. Only one Hub is allowed per network. Provides a GUI for network management with plugin,
channel, and permission systems.

- Energy detection range: 10 blocks
- Link range: 16 blocks
- Plugin slots: 5

### Circulation Shielder

Blocks energy flow within a specified range. Supports redstone control:

- **Normal mode**: blocks energy flow when receiving a redstone signal
- **Inverted mode**: allows energy flow when receiving a redstone signal

Mode and range visualization can be toggled in the GUI.

---

## Items

### Circulation Wrench

A multi-function inspection and configuration tool with two main modes:

**Network Info Mode** — Sneak + right-click a node to view info

- Show all info
- Show connection range
- Show node network

**Energy Node Config Mode** — Sneak + right-click a machine to manually override type detection

- Energy output (generator)
- Energy input (consumer)
- Energy storage
- Clear config

Controls: Sneak + right-click air to switch main mode, Sneak + scroll wheel to switch sub-mode.

### Hub Channel Plugin

When inserted into a Hub, connects the network to a specified channel. Channel info is stored in the item's NBT —
transferring the plugin also transfers the channel binding.

### Wide Area Charging Plugin

Enables the Hub's network to charge items for all authorized players within the same dimension.

### Dimensional Charging Plugin

Enables the Hub's network to charge items for all authorized players across dimensions.

---

## Hub Interface

The Hub provides several functional tabs:

| Tab              | Function                                                            |
|------------------|---------------------------------------------------------------------|
| Node List        | View all nodes in the network; double-click to locate in world      |
| Charging Config  | Select which players receive charging, controlled by equipment slot |
| Plugin List      | Manage the Hub's 5 plugin slots                                     |
| Channel List     | Browse and switch available channels                                |
| Permissions      | Manage channel member roles (Owner / Admin / Member)                |
| Channel Settings | Create and delete channels                                          |
| Energy Display   | Real-time view of network energy status                             |

---

## Channels & Permissions

Networks are isolated through **channels**. Channels have three visibility modes: Public, Team, and Private.

Each channel has a permission system:

- **Owner**: channel creator, has full permissions, non-transferable
- **Admin**: can manage network configuration and members
- **Member**: can use the network

---

## Network Connection Mechanics

Each node has a **link range** attribute. Nodes within each other's link range automatically connect, forming a mesh
topology. Link types include bidirectional and unidirectional connections.

Energy-detecting nodes (Port Nodes, Charging Nodes, Hubs) scan for energy machines within their **energy range** and
automatically identify machine type (generator / consumer / storage). If detection is incorrect, you can manually
override it with the Circulation Wrench.

A network cannot contain more than one Hub — conflicts are detected on placement.

---

## Configuration

All node range parameters are adjustable in the config file. Additionally configurable:

- Energy machine blacklist (exclude incompatible TileEntities by class name prefix)
- Supply operation blacklist

---

## Developer API

See the [Developer API Documentation](docs/developer-api-en.md).
