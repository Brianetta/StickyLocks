package net.simplycrafted.StickyLocks;

import org.bukkit.Bukkit;
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
