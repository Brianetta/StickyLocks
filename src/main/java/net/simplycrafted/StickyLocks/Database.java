package net.simplycrafted.StickyLocks;

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
        try {
            sql = db.createStatement();
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS player (uuid char(36) primary key,name text,notify tinyint not null default 0)");
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS protectable (id integer primary key autoincrement,material text,extrablock tinyint not null default 0,inventory tinyint not null default 0)");
            sql.close();
        } catch (SQLException e) {
            StickyLocks.getInstance().getLogger().info(e.toString());
        }
    }

    public String getUUID (String name) {
        Statement sql;
        ResultSet result = null;
        try {
            sql = db.createStatement();
        result = sql.executeQuery("SELECT UUID FROM player WHERE name LIKE '" + name + "'");
        if(result.next()) {
            return result.getString(1);
        }
        else {
            return "";
        }
        } catch (SQLException e) {
            return "";
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
