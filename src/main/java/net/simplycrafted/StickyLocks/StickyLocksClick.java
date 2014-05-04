package net.simplycrafted.StickyLocks;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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
public class StickyLocksClick implements Listener{
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block target=event.getClickedBlock();
        //if target.getType() ...check it's a protectable block type
        if (event.getAction()== Action.RIGHT_CLICK_BLOCK) {
            // Initiate locking, or present actions for locked item
            event.getPlayer().sendMessage(target.getType().name());
        }
    }
}

