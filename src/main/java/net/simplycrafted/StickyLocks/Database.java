package net.simplycrafted.StickyLocks;

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
    static Connection db;
    static StickyLocks stickylocks;

    public Database() {
        if (db == null) {
            try {
                db = DriverManager.getConnection("jdbc:sqlite:" + StickyLocks.getInstance().getDataFolder() + "/stickylocks.db");
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
            sql = db.createStatement();
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS player (uuid char(36) primary key,name text,notify tinyint not null default 0)");
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS protectable (material text primary key)");
            // Re-populate this every time we load. The config file is authoritative.
            sql.executeUpdate("DELETE FROM protectable");
            sql.close();
            // Load values for protectable blocks from the config
            for (String protectable : stickylocks.getConfig().getStringList("protectables")) {
                Material material = Material.getMaterial(protectable);
                if (material.isBlock()) {
                    psql=db.prepareStatement("INSERT INTO protectable (material) VALUES (?)");
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
            psql = db.prepareStatement("SELECT UUID FROM player WHERE name LIKE ?");
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

    public boolean isProtectable (Block block) {
        PreparedStatement psql;
        ResultSet result;
        try {
            psql = db.prepareStatement("SELECT count(material) FROM protectable WHERE material LIKE ?");
            psql.setString(1, block.getType().name());
            result = psql.executeQuery();
            result.next();
            if (result.getInt(1) > 0) {
                result.close();
                psql.close();
                return true;
            } else {
                result.close();
                psql.close();
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public void shutdown() {
        try {
            db.close();
        } catch (SQLException e) {
            StickyLocks.getInstance().getLogger().info(e.toString());
        }
    }
}
