package net.simplycrafted.StickyLocks.commands;

import net.simplycrafted.StickyLocks.Database;
import net.simplycrafted.StickyLocks.StickyLocks;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copyright © Brian Ronald
 * 09/09/18
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
public class StickyLocksTabCompleter implements TabCompleter {
    private final List<String> initialCommands = Arrays.asList("show","add","remove","group","notify","autolock","reload","clearselection");
    Database db = new Database();
    StickyLocks stickylocks = StickyLocks.getInstance();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Predicate<String> matchTypedSoFar = arg -> arg.toLowerCase().startsWith(args[args.length-1].toLowerCase());
        if (command.getName().equals("stickylocks")) {
            if (!(sender instanceof Player)) {
                // Just one command for console right now
                return args.length <= 1 ? Collections.singletonList("reload") : Collections.emptyList();
            } else {
                // We have a player, get their selected block and the block's owner
                Location selectedLocation = null;
                UUID playerID;
                if (stickylocks.selectedBlock.get(sender) == null) {
                    // default to the player
                    playerID = ((Player) sender).getUniqueId();
                } else {
                    playerID = db.getUUID(stickylocks.selectedBlock.get(sender));
                    if (playerID == null) playerID = ((Player) sender).getUniqueId();
                    selectedLocation = stickylocks.selectedBlock.get(sender);
                }
                switch (args.length) {
                    case 0:
                        return initialCommands;
                    case 1:
                        return initialCommands.stream().filter(matchTypedSoFar).collect(Collectors.toList());
                    case 2:
                        switch (args[0].toLowerCase()) {
                            // The following take no further arguments
                            case "show":
                            case "notify":
                            case "autolock":
                            case "reload":
                            case "clearselection":
                                return Collections.emptyList();
                            case "add":
                                if (selectedLocation == null ||
                                        (playerID != ((Player) sender).getUniqueId() && !sender.hasPermission("stickylocks.locksmith"))){
                                    return Collections.emptyList();
                                }
                                return db.getGroupsAndPlayersTab(playerID).stream().filter(matchTypedSoFar).collect(Collectors.toList());
                            case "remove":
                                if (selectedLocation == null ||
                                        (playerID != ((Player) sender).getUniqueId() && !sender.hasPermission("stickylocks.locksmith"))) {
                                    return Collections.emptyList();
                                }
                                return db.getAccessTab(selectedLocation).stream().filter(matchTypedSoFar).collect(Collectors.toList());
                            case "group":
                            case "groups":
                                return db.getGroupsTab(((Player) sender).getUniqueId()).stream().filter(matchTypedSoFar).collect(Collectors.toList());
                        }
                    case 3:
                        if (args[0].toLowerCase().startsWith("group")) {
                            // group or groups, next sub-command
                            return Stream.of("add", "remove", "rename", "merge").filter(matchTypedSoFar).collect(Collectors.toList());
                        } else {
                            return Collections.emptyList();
                        }
                    case 4:
                        if (args[0].toLowerCase().startsWith("group")) {
                            switch (args[2].toLowerCase()) {
                                case "add":
                                    return db.getPlayersTab(playerID).stream().filter(matchTypedSoFar).collect(Collectors.toList());
                                case "remove":
                                    return db.getGroup(playerID,args[1]).stream().filter(matchTypedSoFar).collect(Collectors.toList());
                                case "rename":
                                case "merge":
                                    return db.getGroupsTab(((Player) sender).getUniqueId()).stream().filter(matchTypedSoFar).collect(Collectors.toList());
                            }
                        }
                }
            }
        }
        return Collections.emptyList();
    }
}
