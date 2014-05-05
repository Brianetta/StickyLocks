# StickyLocks #

New locking plugin for Bukkit. Database/UUID/tool/command driven (default took is a stick).

## Features ##

Using a stick as a wand, a player selects a protectable block with
right-click. The player can claim or un-claim the selected block
with a subsequent left-click. While claimed by a player, only that
player can interact with that block.

While selected, the player can add names or groups of names to the
list of players permitted to interact with the block. Groups can be
renamed, and their membership adjusted, at any time by the player -
dynamically updating all the protected blocks that have that group.

The list of protectable blocks is configurable, but by default includes
doors, chests, switches, buttons and pressure plates. It also includes
redstone repeaters and comparators, protecting redstone device timings.

This plugin does not prevent destruction of any of the protected blocks,
although there will be an option to protect those with an inventory
from destruction. The idea is that this plugin will be used in combination
with some other plugin to provide protection from destruction, such as
Towny, Factions or WorldGuard regions.

## Done so far: ##

- UUIDs and block locations stored in SQLITE database
- User-configurable list of protectable blocks
- User-configurable tool
- Right-click a protectable block with a tool, tells you who owns it (and cancels event)
- Right-click a protectable block with a tool, selects it (and cancels event)

## To-do: ##

- Allow users to claim a protectable block
- Allow users to add users and groups of users to an allowed list
- Actually enforce these lists
- Also handle pressure plate interaction
- Integrate with Logblock (and, perhaps, other logging tools) to limit ownership claim to player who placed block
- Add option to protect blocks with an inventory from being broken
- Handle blocks which change Type naturally (such as a redstone repeater)
- Handle automated inventory changes (such as hopper action)
