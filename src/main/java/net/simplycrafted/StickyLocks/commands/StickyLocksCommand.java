package net.simplycrafted.StickyLocks.commands;

import net.simplycrafted.StickyLocks.Database;
import net.simplycrafted.StickyLocks.StickyLocks;
import net.simplycrafted.StickyLocks.listeners.StickyLocksClick;
import net.simplycrafted.StickyLocks.util.Util;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.UUID;

/**
 * Copyright © Brian Ronald
 * 04/05/14
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
public class StickyLocksCommand implements CommandExecutor {
    private StickyLocks stickylocks = StickyLocks.getInstance();

    Database db = new Database();

    // Command handler follows the command structure in plugin.yml. It looks for the first
    // argument as being the sub-command. If the sub-command is "group", the next arg is
    // the group name and the one after is a group sub-command. Any arguments that follow
    // commands can be repeated, and are looped over.
    //
    // It's important to note that sender and playerID are not the same guy. playerID is
    // the UUID of the owner of the currently selected block. By selecting somebody else's
    // block, the commands operate on that player's groups and that player's block. Of
    // course, changes can only be made with the relevant permissions.

    public boolean onCommand (CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("stickylocks")) {
            if (sender instanceof Player) {
                UUID playerID;
                String message;
                Location location = null;

                // If a block is selected, use that block's owner. If that block has no owner, or
                // if no block is selected, use the player.
                if (stickylocks.selectedBlock.get(sender) == null) {
                    playerID = ((Player) sender).getUniqueId();
                } else {
                    playerID = db.getUUID(stickylocks.selectedBlock.get(sender));
                    if (playerID == null) playerID = ((Player) sender).getUniqueId();
                    location = stickylocks.selectedBlock.get(sender);
                }

                // Player-specific commands
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "show" :
                            if (location == null) {
                                stickylocks.sendMessage(sender, String.format("You must select a block first (right-click with %s)", stickylocks.getConfig().getString("tool")), false);
                            } else {
                                Boolean playerHasAccess = playerID.equals(((Player) sender).getUniqueId()) || sender.hasPermission("stickylocks.locksmith");
                                stickylocks.sendMessage(sender, "Access details for selected block:", playerHasAccess);
                                List<String> accessInfo = db.getAccess(location);
                                if (accessInfo.isEmpty()) {
                                    stickylocks.sendMessage(sender, "Block is unowned", true);
                                } else {
                                    // Show details
                                    for (String accessLine : accessInfo) {
                                        stickylocks.sendMessage(sender, accessLine, playerHasAccess);
                                    }
                                }
                            }
                            return true;
                        case "add" :
                            if (location == null) {
                                stickylocks.sendMessage(sender, String.format("You must select a block first (right-click with %s)", stickylocks.getConfig().getString("tool")), false);
                            } else if (args.length > 1) {
                                if (playerID.equals(((Player) sender).getUniqueId()) || sender.hasPermission("stickylocks.locksmith")) {
                                    for (int i = 1, argsLength = args.length; i < argsLength; i++) {
                                        message = db.addPlayerOrGroupToACL(location, playerID, args[i]);
                                        if (message == null) {
                                            stickylocks.sendMessage(sender, String.format("Failed to add %s to access list (check spelling)", args[i]), false);
                                        } else {
                                            if (Util.getOtherHalfOfChest(location.getBlock()) != null)
                                                db.addPlayerOrGroupToACL(Util.getOtherHalfOfChest(location.getBlock()).getLocation(), playerID, args[i]);
                                            stickylocks.sendMessage(sender, message, true);
                                        }
                                    }
                                } else {
                                    stickylocks.sendMessage(sender, "The selected block does not belong to you", false);
                                }
                            }else{
                                stickylocks.sendMessage(sender, "Please specify players or groups", false);
                            }
                            return true;
                        case "remove" :
                            if (location == null) {
                                stickylocks.sendMessage(sender, String.format("You must select a block first (right-click with %s)", stickylocks.getConfig().getString("tool")), false);
                            } else if (args.length > 1) {
                                if (playerID.equals(((Player) sender).getUniqueId()) || sender.hasPermission("stickylocks.locksmith")) {
                                    for (int i = 1, argsLength = args.length; i < argsLength; i++) {
                                        message = db.removePlayerOrGroupFromACL(location, args[i]);
                                        if (message == null) {
                                            stickylocks.sendMessage(sender, String.format("Failed to remove %s from access list (check spelling)", args[i]), false);
                                        } else {
                                            if (Util.getOtherHalfOfChest(location.getBlock()) != null)
                                                db.removePlayerOrGroupFromACL(Util.getOtherHalfOfChest(location.getBlock()).getLocation(), args[i]);
                                            stickylocks.sendMessage(sender, message, true);
                                        }
                                    }
                                } else {
                                    stickylocks.sendMessage(sender, "The selected block does not belong to you", false);
                                }
                            }else{
                                stickylocks.sendMessage(sender, "Please specify players or groups", false);
                            }
                            return true;
                        case "group" :
                            if (args.length == 1) {
                                List<String> groupMembers = db.listGroups(playerID);
                                if (groupMembers.isEmpty()) {
                                    stickylocks.sendMessage(sender, String.format("Player %s has no groups",db.getName(playerID)), false);
                                } else {
                                    Boolean colourFlag = playerID.equals(((Player) sender).getUniqueId());
                                    stickylocks.sendMessage(sender, String.format("Groups for %s:", db.getName(playerID)), colourFlag);
                                    // Show group
                                    for (String groupMember : groupMembers) {
                                        stickylocks.sendMessage(sender, groupMember, colourFlag);
                                    }
                                }
                                return true;
                            }
                            if (args.length == 2) {
                                // list the members of a group
                                List<String> groupMembers = db.getGroup(playerID, args[1]);
                                if (groupMembers.isEmpty()) {
                                    stickylocks.sendMessage(sender, String.format("Group \"%s\" (%s) is empty", args[1], db.getName(playerID)), false);
                                } else {
                                    Boolean colourFlag = playerID.equals(((Player) sender).getUniqueId());
                                    stickylocks.sendMessage(sender, String.format("Members of group \"%s\" (%s):", args[1], db.getName(playerID)), colourFlag);
                                    // Show group
                                    for (String groupMember : groupMembers) {
                                        stickylocks.sendMessage(sender, groupMember, colourFlag);
                                    }
                                }
                                return true;
                            }
                            if (args.length > 3) {
                                String error;
                                if (playerID.equals(((Player) sender).getUniqueId()) || sender.hasPermission("stickylocks.locksmith")) {
                                    switch (args[2]) {
                                        case "add":
                                            for (int i = 3, argsLength = args.length; i < argsLength; i++) {
                                                // add player to group
                                                error = db.addPlayerToGroup(playerID, args[1], args[i]);
                                                if (error == null) {
                                                    stickylocks.sendMessage(sender, String.format("Added %s to group %s", args[i], args[1]), true);
                                                } else {
                                                    stickylocks.sendMessage(sender, error, false);
                                                }
                                            }
                                            return true;
                                        case "remove":
                                            for (int i = 3, argsLength = args.length; i < argsLength; i++) {
                                                // remove player from group
                                                error = db.removePlayerFromGroup(playerID, args[1], args[i]);
                                                if (error == null) {
                                                    stickylocks.sendMessage(sender, String.format("Removed %s from group %s", args[i], args[1]), true);
                                                } else {
                                                    stickylocks.sendMessage(sender, error, false);
                                                }
                                            }
                                            return true;
                                        case "rename":
                                        case "merge":
                                            // move members to new group
                                            db.renameGroup(playerID, args[1], args[3]);
                                            stickylocks.sendMessage(sender, String.format("Blindly moving group members of %s into %s", args[1], args[3]), true);
                                            return true;
                                        default:
                                            stickylocks.sendMessage(sender, "Unknown sub-command for group", false);
                                            return false;
                                }
                                } else {
                                    stickylocks.sendMessage(sender, "You cannot alter another player's groups", false);
                                    return true;
                                }
                            }
                            // wrong number of arguments for group
                            stickylocks.sendMessage(sender, "Wrong number of arguments", false);
                            return false;
                        case "notify" :
                            db.toggleNotify((Player)sender);
                            stickylocks.playerNotification.put((Player) sender, !stickylocks.playerNotification.get((Player) sender));
                            stickylocks.sendMessage(sender,"Toggled lock notifications", true);
                            return true;
                        case "reload" :
                            if(sender.hasPermission("stickylocks.reload")) {
                                stickylocks.sendMessage(sender, "Reloading configuration", true);
                                stickylocks.reloadConfig();
                                db.createTables();
                                // Re-register the PlayerInteractEvent in case the tool has changed
                                PlayerInteractEvent.getHandlerList().unregister(stickylocks);
                                stickylocks.getServer().getPluginManager().registerEvents(new StickyLocksClick(),stickylocks);
                            } else {
                                stickylocks.sendMessage(sender,"No permission",false);
                            }
                            return true;
                        case "clearselection" :
                            stickylocks.selectedBlock.remove(sender);
                            stickylocks.sendMessage(sender, "Selection cleared", true);
                            return true;
                        default:
                            return false;
                    }
                }
            } else {
                // Commands that can also be run from console
                if (args.length > 0  && args[0].equals("reload")) {
                    stickylocks.sendMessage(sender, "Reloading configuration", true);
                    stickylocks.reloadConfig();
                    db.createTables();
                    // Re-register the PlayerInteractEvent in case the tool has changed
                    PlayerInteractEvent.getHandlerList().unregister(stickylocks);
                    stickylocks.getServer().getPluginManager().registerEvents(new StickyLocksClick(),stickylocks);
                } else {
                    stickylocks.sendMessage(sender, "Only the reload command works from console", false);
                }
                return true;
            }
        }
        return false;
    }
}
