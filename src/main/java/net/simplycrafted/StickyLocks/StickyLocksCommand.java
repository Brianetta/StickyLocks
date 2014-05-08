package net.simplycrafted.StickyLocks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
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

    public boolean onCommand (CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("stickylocks")) {
            if (sender instanceof Player) {
                UUID playerID;

                // If a block is selected, use that block's owner. If that block has no owner, or
                // if no block is selected, use the player.
                if (stickylocks.SelectedBlock.get(sender) == null) {
                    playerID = ((Player) sender).getUniqueId();
                } else {
                    playerID = db.getUUID(stickylocks.SelectedBlock.get(sender));
                    if (playerID == null) playerID = ((Player) sender).getUniqueId();
                }

                    // Player-specific commands
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "show" :
                            stickylocks.sendMessage(sender,"show: Not implemented yet.", true);
                            return true;
                        case "add" :
                            stickylocks.sendMessage(sender,"add: Not implemented yet.", true);
                            return true;
                        case "remove" :
                            stickylocks.sendMessage(sender,"remove: Not implemented yet.", true);
                            return true;
                        case "group" :
                            if (args.length == 1) {
                                PlayerGroupList groupMembers = db.listGroups(playerID);
                                if (groupMembers.isEmpty()) {
                                    stickylocks.sendMessage(sender, "Player has no groups", true);
                                } else {
                                    stickylocks.sendMessage(sender, "Groups:", true);
                                    // Show group
                                    for (Iterator iterator = groupMembers.getIterator(); iterator.hasNext();) {
                                        stickylocks.sendMessage(sender, iterator.next().toString(), true);
                                    }
                                }
                                return true;
                            }
                            if (args.length == 2) {
                                // list the members of a group
                                PlayerGroupList groupMembers = db.getGroup(playerID, args[1]);
                                if (groupMembers.isEmpty()) {
                                    stickylocks.sendMessage(sender, String.format("Group %s is empty", args[1]), true);
                                } else {
                                    stickylocks.sendMessage(sender, String.format("Members of group %s:", args[1]), true);
                                    // Show group
                                    for (Iterator iterator = groupMembers.getIterator(); iterator.hasNext();) {
                                        stickylocks.sendMessage(sender, iterator.next().toString(), true);
                                    }
                                }
                                return true;
                            }
                            if (args.length == 4) {
                                String error;
                                switch (args[2]) {
                                    case "add" :
                                        // add player to group
                                        error = db.addPlayerToGroup(playerID,args[1],args[3]);
                                        if (error == null) {
                                            stickylocks.sendMessage(sender,String.format("Added %s to group %s",args[3],args[1]),false);
                                        } else {
                                            stickylocks.sendMessage(sender,error,true);
                                        }
                                        return true;
                                    case "remove" :
                                        // remove player from group
                                        error = db.removePlayerFromGroup(playerID,args[1], args[3]);
                                        if (error == null) {
                                            stickylocks.sendMessage(sender,String.format("Removed %s from group %s",args[3],args[1]),false);
                                        } else {
                                            stickylocks.sendMessage(sender,error,true);
                                        }
                                        return true;
                                    case "rename" :
                                        // rename group
                                        error = db.renameGroup(playerID,args[1], args[3]);
                                        if (error == null) {
                                            stickylocks.sendMessage(sender,String.format("Renamed group %s to %s",args[1],args[3]),false);
                                        } else {
                                            stickylocks.sendMessage(sender,error,true);
                                        }
                                        return true;
                                    default :
                                        stickylocks.sendMessage(sender,"Unknown sub-command for group",true);
                                        return false;
                                }
                            }
                            // wrong number of arguments for group
                            stickylocks.sendMessage(sender,"Wrong number of arguments",true);
                            return false;
                        case "info" :
                            stickylocks.sendMessage(sender,"info: Not implemented yet.", true);
                            return true;
                        default:
                            return false;
                    }
                }
            } else {
                // Commands that can also be run from console
                stickylocks.sendMessage(sender,"This doesn't work from console yet.", true);
                return true;
            }
        }
        return false;
    }
}
