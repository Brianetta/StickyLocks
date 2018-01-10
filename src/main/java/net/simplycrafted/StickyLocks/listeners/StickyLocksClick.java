package net.simplycrafted.StickyLocks.listeners;

import net.simplycrafted.StickyLocks.Database;
import net.simplycrafted.StickyLocks.DetectBuildLimiter;
import net.simplycrafted.StickyLocks.Protection;
import net.simplycrafted.StickyLocks.StickyLocks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import net.simplycrafted.StickyLocks.util.Util;

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
    DetectBuildLimiter detectBuildLimiter;

    // Get the tool item Material from the config
    public StickyLocksClick() {
        stickylocks = StickyLocks.getInstance();
        tool = Material.getMaterial(stickylocks.getConfig().getString("tool"));
    }

    // This event handler responds to player actions on blocks. It's broken down by
    // type (right click, left click, right click nothing, step on plate) and then
    // further broken down by whether the player is holding the specified tool.
    // If the player is holding the tool, it manipulates locks. Otherwise, it
    // enforces access lists.

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        Block target = event.getClickedBlock();
        Player player = event.getPlayer();
        //if target.getType() ...check it's a protectable block type
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Right-clicks are either with a stick, or not
            if (event.getPlayer().getInventory().getItemInMainHand().getType() == tool) {
                // Stick used - initiate locking, or present actions for locked item
                Protection protection = db.getProtection(target);
                if (protection.getType() != null) {
                    String selected = "";
                    if (stickylocks.selectedBlock.get(player) == null || (stickylocks.selectedBlock.get(player).getWorld().equals(target.getLocation().getWorld()) && !(stickylocks.selectedBlock.get(player).distanceSquared(target.getLocation()) < 1))) {
                        // Block is not one previously selected
                        stickylocks.selectedBlock.put(player, target.getLocation());
                        selected = " " + ChatColor.RED + "(selected)";
                    }
                    if (protection.isProtected())
                        if (player.getUniqueId().equals(protection.getOwner()))
                            stickylocks.sendMessage(player, String.format("%s owned by you%s", protection.getType(), selected), true);
                        else
                            stickylocks.sendMessage(player, String.format("%s owned by %s%s", protection.getType(), protection.getOwnerName(), selected), player.hasPermission("stickylocks.locksmith"));
                        // Use of permission on previous line changes colour of message
                    else
                        stickylocks.sendMessage(player, String.format("Unowned %s%s", protection.getType(), selected), true);
                }
                event.setCancelled(true);
            } else {
                // Right-click without a stick
                Protection protection = db.getProtection(target);
                if (protection.isProtected()) {
                    if (protection.getOwner().equals(player.getUniqueId())) {
                        stickylocks.sendMuteableMessage(player, String.format("%s owned by you", protection.getType()), true);
                    } else {
                        stickylocks.sendMuteableMessage(player, String.format("%s owned by %s", protection.getType(), protection.getOwnerName()), player.hasPermission("stickylocks.ghost"), String.format("LOCKED by %s", protection.getOwnerName()));
                        // Use of permission on previous line changes colour of message
                        if (!player.hasPermission("stickylocks.ghost") && db.accessDenied(player, target)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Left-clicks are either with a stick, or not
            if (player.getInventory().getItemInMainHand().getType() == tool) {
                // Stick used - check if it's the selected block, and lock/unlock if appropriate
                if (stickylocks.selectedBlock.get(player) != null && (stickylocks.selectedBlock.get(player).getWorld().equals(target.getLocation().getWorld()) && stickylocks.selectedBlock.get(player).distanceSquared(target.getLocation()) < 1)) {
                    // Just left-clicked the selected block with a stick!
                    if (player.hasPermission("stickylocks.lock")) {
                        Protection protection = db.getProtection(target);
                        if (protection.isProtected()) {
                            if (protection.getOwner().equals(player.getUniqueId()) || player.hasPermission("stickylocks.locksmith")) {
                                stickylocks.sendMessage(player, "Unlocking...", true);
                                db.unlockBlock(target);
                                if (Util.getOtherHalfOfChest(target) != null) {
                                    db.unlockBlock(Util.getOtherHalfOfChest(target));
                                }
                            } else {
                                stickylocks.sendMessage(player, "You do not own this object", false);
                            }
                        } else if (protection.getType() != null) {
                            detectBuildLimiter = new DetectBuildLimiter();
                            if (detectBuildLimiter.canBreakHere(player, target)) {
                                stickylocks.sendMessage(player, "Locking...", true);
                                db.lockBlock(target, player);
                                if (Util.getOtherHalfOfChest(target) != null) {
                                    db.lockBlock(Util.getOtherHalfOfChest(target),player);
                                }
                            }
                            detectBuildLimiter.cleanup();
                        }
                    } else {
                        stickylocks.sendMessage(player, "You don't have permission to lock or unlock objects", false);
                    }
                }
                event.setCancelled(true);
            }
        } else if (event.getAction() == Action.PHYSICAL) {
            // Pressure plate action
            Protection protection = db.getProtection(target);
            if (protection.isProtected())
                if (!protection.getOwner().equals(player.getUniqueId())) {
                    if (!player.hasPermission("stickylocks.ghost") || db.accessDenied(player, target)) {
                        event.setCancelled(true);
                    }
                }
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (player.getInventory().getItemInMainHand().getType() == tool && stickylocks.selectedBlock.get(player) != null) {
                // Player right-clicked nothing - Deselect whatever might be selected.
                stickylocks.selectedBlock.remove(player);
                stickylocks.sendMessage(player, "Selection cleared", true);
            }
        }
    }
}