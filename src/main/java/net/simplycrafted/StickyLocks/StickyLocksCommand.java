package net.simplycrafted.StickyLocks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    public boolean onCommand (CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("stickylocks")) {
            if (sender instanceof Player) {
                // Player-specific commands
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "show" :
                            return true;
                        case "add" :
                            return true;
                        case "remove" :
                            return true;
                        case "group" :
                            return true;
                        case "info" :
                            return true;
                        default:
                            return false;
                    }
                }
            } else {
                // Commands that can also be run from console
                return true;
            }
        }
        return false;
    }
}
