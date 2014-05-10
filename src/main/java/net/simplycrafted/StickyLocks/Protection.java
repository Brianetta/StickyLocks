package net.simplycrafted.StickyLocks;

import org.bukkit.Material;

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
public class Protection {
    private Material ptype;
    private boolean pprot;
    private UUID powner;
    private String pownerName;

    // This class is just a handy container for returning information about
    // the protected state (or otherwise) of a block. A null material indicates
    // that the block is something we don't care about (dirt, stone, torch,
    // wood, redstone wire, etc, etc). If material is populated, it's in the
    // config list "protectables" and can be protected buy this plugin.
    //
    // The UUID is the owner of the block, as far as this plugin is concerned.
    // The string is their name, as found in the database, and the boolean value
    // is simply whether the block is locked or not.
    //
    // There's no information about the block itself; whichever class asked for
    // this already knows about the block. This class's instances are expected
    // to be discarded fairly quickly.

    public Protection(Material t,boolean p, String o, String n) {
        ptype = t;
        pprot = p;
        pownerName = n;
        try {
            powner = UUID.fromString(o);
        } catch (IllegalArgumentException | NullPointerException e) {
            powner = null;
            pownerName = null;
        }
    }

    public Material getType() {
        return ptype;
    }

    public boolean isProtected() {
        return pprot;
    }

    public UUID getOwner() {
        return powner;
    }

    public String getOwnerName() {
        return pownerName;
    }
    }
