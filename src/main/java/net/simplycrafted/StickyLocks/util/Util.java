package net.simplycrafted.StickyLocks.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

/**
 * Copyright © Brian Ronald
 * 30/11/14
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

// Utility function to return the other block of a double-chest block,
// returning null if there isn't one, or the selected block isn't a chest.
public class Util {
    public static Block getOtherHalfOfChest(Block selectedChest) {
          // If it isn't a type of chest, there isn't another half of it to find.
        if ((selectedChest.getType() != Material.CHEST) && selectedChest.getType() != Material.TRAPPED_CHEST) {
            return null;
        }

        InventoryHolder inventoryHolder = ((Chest) selectedChest).getInventory().getHolder();
        if (!(inventoryHolder instanceof DoubleChest)) {
            return null;
        }
        DoubleChest doubleChest = (DoubleChest) inventoryHolder;
        Block left = doubleChest.getLeftSide().getInventory().getLocation().getBlock();
        Block right = doubleChest.getRightSide().getInventory().getLocation().getBlock();

        if (selectedChest.equals(left))
            return right;
        else
            return left;
    }
}
