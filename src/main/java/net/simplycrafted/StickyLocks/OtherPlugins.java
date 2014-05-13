package net.simplycrafted.StickyLocks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

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

public class OtherPlugins implements Listener {
    private static StickyLocks stickylocks = StickyLocks.getInstance();
    BlockPlaceEvent blockPlaceEvent;
    Boolean canPlace = true;

    // This class's job is to talk to other plugins, so that StickyLocks can find out
    // whether a player's attempt to lock an object should be prevented on the grounds
    // that they couldn't have built the object.
    //
    // It does so by launching a fake build event, then seeing if anything cancelled it.

    public OtherPlugins() {
        stickylocks.getServer().getPluginManager().registerEvents(this,stickylocks);
    }

    @EventHandler (priority= EventPriority.HIGHEST)
    public void onBlockPlaceEvent (BlockPlaceEvent event) {
        if (event.equals(blockPlaceEvent)) {
            if (event.isCancelled()) {
                canPlace = false;
            }
            event.setCancelled(true);
        }
    }

    public void cleanup () {
        BlockPlaceEvent.getHandlerList().unregister(this);
    }

    // Function that takes a player and a block, and determines whether the player would
    // be able to build here. Used to decide whether they should be allowed to lock something.

    public Boolean canBuildHere(Player player, Block block) {
        blockPlaceEvent = new BlockPlaceEvent(block, block.getState(),block,new ItemStack(block.getType()),player,true);
        stickylocks.getServer().getPluginManager().callEvent(blockPlaceEvent);
        return canPlace;
    }
}