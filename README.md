![StickyLocks logo](https://github.com/Brianetta/StickyLocks/wiki/StickyLocks.png)
# StickyLocks #

Locking plugin for Bukkit/Spigot. Database/UUID/tool/command driven (default tool is a stick).

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

**This plugin does not prevent destruction of any of the protected blocks.**
The idea is that this plugin will be used in combination with some
other plugin to provide protection from destruction, such as Towny,
Factions or WorldGuard regions.

## Building StickyLocks ##

StickyLocks is a Maven project. It was developed using IntelliJ Idea, but
should work fine in Eclipse, and shell junkies won't have any trouble.

## Installation: ##

After you've built StickyLocks, put the resulting .jar file into your plugins
folder and start your Bukkit server (or load it with any plugin manager). It
will write out the default **config.yml** file (as seen
[here](src/main/resources/config.yml)).

Edit the config if necessary, although everything should work straight away.
The plugin will create and use StickyLocks.db, which is an
[SQLite](http://www.sqlite.org/) database that can be queried with any
SQLite3 compatible client.

The plugin tries to detect whether other plugins are protecting against
block-breaking. If they are, it won't allow a player to add a lock where they
cannot break. This is to prevent grief-by-locking.

## Permissions

- **stickylocks.lock** permits a user to claim a block (door, chest, etc) and choose who can use it through an access list
- **stickylocks.locksmith** permits a user to modify or unlock other users' claimed blocks
- **stickylocks.ghost** permits a user to open or use blocks that would otherwise be locked by the plugin
- **stickylocks.reload** permits the player to reload the plugin's config from disk

## Commands

First, the user has to use a tool wand. By default, this is a stick. Right-clicking on a protectable block with the stick will tell the player what that block is, who its owner is, or that it is unowned. The plugin uses green text to indicate that the player has access, or a command succeeded. It uses red to indicate that the player has no access, or that a command failed. Right-clicking on a block with the stick will also select that block.

Once selected, left clicking on the block toggles its lock state. If it's unowned, it will become owned by the player. If it is owned by the player, or the player is a locksmith (see permisisons, above) it will become unowned, and all lock information will be removed. Once selected, commands will operate on that block, or on the user associated with that block.

Right-clicking the air with a stick deselects the selected block.

*/stickylocks* is aliased as */sl*

- **/sl show** lists the lock information for the current block. It shows who owns it, and lists the names of players and groups of players with access to that block. Any groups listed will be as defined by that block's owner.
- **/sl add &lt;player|group&gt;** adds the specified player or group of players to the list of people allowed to use the block. The group will be one owned by the block's owner, if the player is a locksmith. In the event that a group and a player both have the same name, the group will be used.
- **/sl remove &lt;player|group&gt;** removes the specified player and/or group of players from the list of people allowed to use that block.
- **/sl group &lt;group&gt;** lists the members of the specified group. If a block is selected, the group will be that belonging to the block's owner.
- **/sl group &lt;group&gt; add &lt;player&gt; ...** adds the specified player(s) to the specified group. If a block is selected, the group will be that belonging to the block's owner (and subject to permission).
- **/sl group &lt;group&gt; remove &lt;player&gt; ...** removes the specified player(s) from the specified group. If a block is selected, the group will be that belonging to the block's owner (and subject to permission).
- **/sl group &lt;group&gt; {rename|merge} &lt;name&gt;** renames the group (rename and merge are identical sub-commands). If the target group exists, the group's members will be added to it. If a block is selected, the group will be that belonging to the block's owner (and subject to permission).
- **/sl clearselection** deselects the player's currently selected block, the same as right-clicking air.
- **/sl reload** reloads the plugin's configuration from the disk, allowing configuration to be changed without restarting the plugin.
- **/sl notify** toggles the chat-spam received when placing lockable items or using locked items. The player is notified of failures to use locked items via the action bar.

## Configuration

Fairly simple, and the defaults are likely to be suitable.

**protectables** is a list of items that can be protected by this plugin. If future versions of Minecraft add new items which should be protected, they can be added by name. Deprecated ID numbers are not supported.

**tool** is the name of the material type the player must hold to select or lock a block.

**chatprefix** is a short text added to the start of chat lines in square brackets. So, the default setting of *SL* would look like *\[SL] Some output here*.

## Incompatibilities

If your server is running a no-cheat plugin, you may need to disable checking for instant breaking in order to use StickyLocks. StickyLocks' heuristic for detecting block protection involves faking and then cancelling a block break event, and your no-cheat plugin may well interpret this as cheating behaviour.

In the case of NoCheatPlus, you would need to disable Fastbreak detection.

## License

StickyLocks is [GPL](http://www.gnu.org/copyleft/gpl.html).
This documentation is [FDL](http://www.gnu.org/copyleft/fdl.html).
