package net.simplycrafted.StickyLocks;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Copyright © Brian Ronald
 * 10/05/14
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

public class OtherPlugins {
    private static Towny towny;
    private static StickyLocks stickylocks = StickyLocks.getInstance();

    // This class's job is to talk to other plugins, so that StickyLocks can find out
    // whether a player's attempt to lock an object should be prevented on the grounds
    // that they couldn't have built the object. Currently, the following plugins
    // are supported:
    //
    // Towny

    public OtherPlugins() {
        stickylocks.getLogger().info("Detecting other plugins:");
        if (stickylocks.getConfig().getBoolean("integration.towny")) {
            towny = (Towny) stickylocks.getServer().getPluginManager().getPlugin("Towny");
            if (towny != null) {
                stickylocks.getLogger().info("Towny detected. Will obey Towny.");
                towny = (Towny) stickylocks.getServer().getPluginManager().getPlugin("Towny");
            }
        }
    }

    // Function that takes a player and a block, and determines whether the player would
    // be able to build here. Used to decide whether they should be allowed to lock something.

    public Boolean canBuildHere(Player player, Block block) {
        Boolean returnVal=true;
        stickylocks.getLogger().info("Attempt to lock.");
        if (towny != null) {
            // As soon as Towny gets up to date, this needs to change to use Material
            // instead of the old magic numbers.
            if (!PlayerCacheUtil.getCachePermission(player,block.getLocation(),block.getTypeId(),block.getData(), TownyPermission.ActionType.BUILD)) {
                returnVal = false;
            }
        }
        return returnVal;
    }
}