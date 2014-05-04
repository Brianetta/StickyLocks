package net.simplycrafted.StickyLocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.sql.*;

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
public class Database {
    // This class handles data storage for the StickyLocks plugin
    static private Connection db_conn;
    static StickyLocks stickylocks;

    public Database() {
        if (db_conn == null) {
            try {
                db_conn = DriverManager.getConnection("jdbc:sqlite:" + StickyLocks.getInstance().getDataFolder() + "/stickylocks.db");
            } catch (SQLException e) {
                StickyLocks.getInstance().getLogger().info(e.toString());
            }
        }
        stickylocks=StickyLocks.getInstance();
    }

    public void createTables() {
        Statement sql;
        PreparedStatement psql;
        try {
            sql = db_conn.createStatement();
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS player (uuid char(36) primary key,name text,notify tinyint not null default 0)");
            // Re-populate this every time we load. The config file is authoritative.
            sql.executeUpdate("DROP TABLE IF EXISTS protectable");
            sql.executeUpdate("CREATE TABLE protectable (material text primary key)");
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS protected (x integer, y integer, z integer, world text, material text, owner char(36),PRIMARY KEY (x,y,z,world))");
            sql.close();
            // Load values for protectable blocks from the config
            for (String protectable : stickylocks.getConfig().getStringList("protectables")) {
                Material material = Material.getMaterial(protectable);
                if (material.isBlock()) {
                    psql= db_conn.prepareStatement("INSERT INTO protectable (material) VALUES (?)");
                    psql.setString(1, material.name());
                    psql.executeUpdate();
                    psql.close();
                } else {
                    stickylocks.getLogger().info(String.format("Warning: Configured item %s is not a Block type.",protectable));
                }
            }
            sql.close();
        } catch (SQLException e) {
            StickyLocks.getInstance().getLogger().info(e.toString());
        }
    }

    public String getUUID (String name) {
        PreparedStatement psql;
        ResultSet result;
        try {
            psql = db_conn.prepareStatement("SELECT UUID FROM player WHERE name LIKE ?");
            psql.setString(1, name);
            result = psql.executeQuery();
            if(result.next()) {
                String returnval = result.getString(1);
                result.close();
                psql.close();
                return returnval;
            } else {
                result.close();
                psql.close();
                return "";
            }
        } catch (SQLException e) {
            return "";
        }
    }

    public Protection getProtection (Block block,Location location) {
        PreparedStatement psql;
        ResultSet result;
        Protection returnValue;
        try {
            psql = db_conn.prepareStatement("SELECT UUID,name " +
                    "FROM protectable " +
                    "LEFT JOIN protected " +
                    "ON protectable.material=protected.material " +
                    "AND x=?" +
                    "AND y=?" +
                    "AND z=?" +
                    "AND world=? " +
                    "LEFT JOIN player " +
                    "ON owner=UUID " +
                    "WHERE protectable.material LIKE ?");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, block.getLocation().getWorld().getName());
            psql.setString(5, block.getType().name());
            result = psql.executeQuery();
            if (result.next()) {
                String owner=result.getString(1);
                if (result.wasNull()) {
                    returnValue = new Protection(block.getType(), false, null, null);
                } else {
                    returnValue = new Protection(block.getType(), true, owner, result.getString(2));
                }
                result.close();
                psql.close();
                return returnValue;
            } else {
                result.close();
                psql.close();
                return new Protection(null, false, null, null);
            }
        } catch (SQLException e) {
            return new Protection(null, false, null, null);
        }
    }

    public void shutdown() {
        try {
            db_conn.close();
        } catch (SQLException e) {
            StickyLocks.getInstance().getLogger().info(e.toString());
        }
    }
}