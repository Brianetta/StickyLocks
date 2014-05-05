package net.simplycrafted.StickyLocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

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
public class StickyLocksClick implements Listener {

    Material tool;
    Database db = new Database();
    static StickyLocks stickylocks;

    // Get the tool item Material from the config
    public StickyLocksClick() {
        stickylocks = StickyLocks.getInstance();
        tool = Material.getMaterial(stickylocks.getConfig().getString("tool"));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Block target = event.getClickedBlock();
        Player player = event.getPlayer();
        //if target.getType() ...check it's a protectable block type
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Right-clicks are either with a stick, or not
            if (event.getPlayer().getItemInHand().getType() == tool) {
                // Stick used - initiate locking, or present actions for locked item
                String info = "";
                Protection protection = db.getProtection(target);
                if (protection.getType() != null) {
                    if (stickylocks.SelectedBlock.get(player) == null || !(stickylocks.SelectedBlock.get(player).distanceSquared(target.getLocation()) < 1)) {
                        stickylocks.SelectedBlock.put(player, target.getLocation());
                        player.sendMessage("Selecting this block:");
                    }
                    if (protection.isProtected())
                        player.sendMessage(String.format("%s owned by %s", protection.getType(), protection.getOwnerName()));
                    else
                        player.sendMessage(String.format("Unowned %s", protection.getType()));
                    event.setCancelled(true);
                }
            } else {
                // Right-click without a stick
            }
        } else {
            // Player is interacting in some other way
        }
    }
}