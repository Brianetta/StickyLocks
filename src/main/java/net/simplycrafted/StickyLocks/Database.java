package net.simplycrafted.StickyLocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
public class Database {
    // This class handles data storage for the StickyLocks plugin.
    // This class does not talk to players.
    // Only this class talks to the database.

    static private Connection db_conn;
    static StickyLocks stickylocks;


    // The constructor connects the database. The one database connection is
    // shared across Database instances, of which there will generally be one
    // per class across the plugin.

    public Database() {
        if (db_conn == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                db_conn = DriverManager.getConnection("jdbc:sqlite:" + StickyLocks.getInstance().getDataFolder() + "/stickylocks.db");
            } catch (SQLException | ClassNotFoundException e) {
                StickyLocks.getInstance().getLogger().info(e.toString());
            }
        }
        stickylocks=StickyLocks.getInstance();
    }

    // Create our tables, if they're not there. One table (protectables) is rebuilt every time
    // that the plugin is loaded. It just contains a list of block types that we care about.

    public void createTables() {
        Statement sql;
        PreparedStatement psql;
        try {
            sql = db_conn.createStatement();

            // Player table - contains name and UUID for players that have been seen by the
            // plugin. "notify" is whether they want chat spam. "automatic" is whether placed
            // blocks will automatically be locked.
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS player (uuid char(36) primary key,name text,notify tinyint not null default 1,automatic tinyint not null default 0)");

            // Check whether the players table needs the 'automatic' column adding (introduced in 1.0)
            ResultSet result = sql.executeQuery("PRAGMA table_info(player)");
            boolean needsUpgrade = true;
            while (result.next()) {
                if (result.getString("name").equals("automatic")) {
                    needsUpgrade = false;
                    break;
                }
            }
            result.close();

            if (needsUpgrade) {
                sql.executeUpdate("ALTER TABLE player ADD COLUMN automatic tinyint NOT NULL DEFAULT 0");
            }

            // Fill that table up with players! (if it's the first time we ever run)
            if(stickylocks.getConfig().getBoolean("populateplayers")) {
                stickylocks.getLogger().info("WARNING: BUILDING INITIAL DATABASE FROM ALL OFFLINE PLAYERS");
                stickylocks.getLogger().info("WARNING: THIS COULD TAKE A LONG TIME AND WILL BE UNRESPONSIVE");
                for (OfflinePlayer offlinePlayer : stickylocks.getServer().getOfflinePlayers()) {
                    addPlayer(offlinePlayer);
                }
                stickylocks.getLogger().info("Database population complete. This will not happen again unless requested.");
                stickylocks.getConfig().set("populateplayers", false);
                stickylocks.saveConfig();
            }

            // Re-populate this every time we load. The config file is authoritative.
            // Simple single-column table of Block Type names.
            sql.executeUpdate("DROP TABLE IF EXISTS protectable");
            sql.executeUpdate("CREATE TABLE protectable (material text primary key)");

            // Protected table - links a block location with a UUID as owner. It also keeps a
            // record of the Material for joining with protectable.
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS protected (x integer, y integer, z integer, world text, material text, owner char(36),PRIMARY KEY (x,y,z,world))");

            // Access group table - each player can have zero or more lists of one or more
            // players, for easy management of access. This table links list owner to list
            // member by UUID, with name being the name of the list.
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS accessgroup (owner char(36),name text,member char(36),PRIMARY KEY (owner,name,member))");

            // Access list table - links a block location with a UUID or an access group
            // list name. The member field contains the UUID or group name. It's not fussy
            // which is which - the plugin dynamically figures out what that field has in it.
            sql.executeUpdate("CREATE TABLE IF NOT EXISTS accesslist (member text,x integer,y integer,z integer,world text,PRIMARY KEY (x,y,z,world,member))");
            sql.close();

            // Load values for protectable blocks from the config
            for (String protectable : stickylocks.getConfig().getStringList("protectables")) {
                Material material = Material.getMaterial(protectable);
                if (material != null && material.isBlock()) {
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

    // This member function takes a block, and gives you a location. It's the location
    // of the block, except when it's a double chest or a door. These types occupy more
    // than one block, but must be treated as one lockable thing. In these cases, this
    // function will return a predictable location, regardless of which half of the
    // object is being looked at.

    private Location getUnambiguousLocation(Block target) {
        // Doors only. This function's useful only for multiple-block Blocks that can't be
        // partially destroyed, which excludes chests.
        Block blockBelow = target.getRelative(BlockFace.DOWN);
        if (target.getType().equals(Material.getMaterial("OAK_DOOR")) && blockBelow.getType().equals(Material.getMaterial("OAK_DOOR"))
                || (target.getType().name().equals("IRON_DOOR") && blockBelow.getType().name().equals("IRON_DOOR"))
                || (target.getType().name().equals("SPRUCE_DOOR") && blockBelow.getType().name().equals("SPRUCE_DOOR"))
                || (target.getType().name().equals("BIRCH_DOOR") && blockBelow.getType().name().equals("BIRCH_DOOR"))
                || (target.getType().name().equals("JUNGLE_DOOR") && blockBelow.getType().name().equals("JUNGLE_DOOR"))
                || (target.getType().name().equals("ACACIA_DOOR") && blockBelow.getType().name().equals("ACACIA_DOOR"))
                || (target.getType().name().equals("DARK_OAK_DOOR") && blockBelow.getType().name().equals("DARK_OAK_DOOR"))
                ) {
            // Doors have an ambiguous location, but we only need to check
            // the block below to disambiguate.
            return blockBelow.getLocation();
        }
        return target.getLocation();
    }

    // This method queries the database to get basic information about
    // whether a block is protectable, whether it is protected, and who
    // owns it.

    public Protection getProtection (Block block) {
        PreparedStatement psql;
        ResultSet result;
        Protection returnValue;
        // administrativeLock contains any recently placed chests while they're being checked to see
        // if they're the second half of a double chest.
        boolean administrativelyLocked = false;
        for (Location location : stickylocks.administrativeLock) {
            if (location.equals(block.getLocation())) {
                administrativelyLocked = true;
            }
        }
        if (administrativelyLocked) {
            return new Protection(Material.CHEST, true, null, "SERVER");
        }
        returnValue = new Protection(null, false, null, null);
        Location location = getUnambiguousLocation(block);
        try {
            // This query selects protectable first, with everything else being
            // joined to that one-field table. This ensures that no block can be
            // found to be protected if that type is no longer listed in that
            // table.
            //
            // There are several other ways this could be done, some much more
            // elegant than this, but this works. It's a candidate for future
            // optimisation.
            psql = db_conn.prepareStatement("SELECT uuid,name,protectable.material " +
                    "FROM protectable " +
                    "LEFT JOIN protected " +
                    "ON protectable.material=protected.material " +
                    "AND x=?" +
                    "AND y=?" +
                    "AND z=?" +
                    "AND world=? " +
                    "LEFT JOIN player " +
                    "ON owner=uuid " +
                    "WHERE protected.material IS NOT NULL " +
                    "OR protectable.material LIKE ? " +
                    "ORDER BY protected.material DESC " +
                    "LIMIT 1");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, block.getLocation().getWorld().getName());
            psql.setString(5, block.getType().name());
            result = psql.executeQuery();
            if (result.next()) {
                returnValue = new Protection(
                        Material.getMaterial(result.getString(3)),  // material
                        !(result.getString(1)==null),                 // protected
                        result.getString(1),                        // owner uuid (as string)
                        result.getString(2)                         // owner name
                );
            }
            result.close();
            psql.close();
            return returnValue;
        } catch (SQLException e) {
            return returnValue;
        }
    }

    // This method locks a block. It's non-destructive when it comes to
    // access lists, but if it's used to change owners then groups won't
    // be updated, and the new owner will need to edit them manually.
    //
    // This function assumes all checking has been done, and just does
    // the work.

    public void lockBlock(Block block, Player player) {
        PreparedStatement psql;
        Location location = getUnambiguousLocation(block);
        try {
            psql = db_conn.prepareStatement("REPLACE INTO protected (x,y,z,world,material,owner) VALUES (?,?,?,?,?,?)");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, location.getWorld().getName());
            psql.setString(5, block.getType().name());
            psql.setString(6, player.getUniqueId().toString());
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to insert record to lock block");
        }
    }

    // This method unlocks a block. It's destructive when it comes to
    // access lists, erasing all players and groups associated with this
    // block from the database.
    //
    // This function assumes all checking has been done, and just does
    // the work.

    public void unlockBlock(Block block) {
        PreparedStatement psql;
        Location location = getUnambiguousLocation(block);
        try {
            psql = db_conn.prepareStatement("DELETE FROM protected WHERE x=? AND y=? AND z=? AND world=?");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, location.getWorld().getName());
            psql.executeUpdate();
            psql = db_conn.prepareStatement("DELETE FROM accesslist WHERE x=? AND y=? AND z=? AND world=?");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, location.getWorld().getName());
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to remove records to unlock block");
        }
    }

    // Cleanly disposes of the database connection.

    public void shutdown() {
        try {
            db_conn.close();
        } catch (SQLException e) {
            StickyLocks.getInstance().getLogger().info(e.toString());
        }
    }

    // Used in the onPlayerJoin handler, to store the player's UUID against
    // their name. By updating every time a player joins, the plugin responds
    // properly to name changes.

    public void addPlayer(Player player) {
        PreparedStatement psql;
        try {
            // The sub-select is used to preserve the notification setting. If
            // nothing is returned, the default is used.
            psql = db_conn.prepareStatement("REPLACE INTO player (uuid,name,notify,automatic) SELECT ?,?,notify,automatic FROM player WHERE uuid=?))");
            psql.setString(1, player.getUniqueId().toString());
            psql.setString(2, player.getName());
            psql.setString(3, player.getUniqueId().toString());
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to insert/replace newly joined player");
        }
    }

    // Used at database initialisation to add all the players that the server has seen already

    public void addPlayer(OfflinePlayer offlinePlayer) {
        PreparedStatement psql;
        try {
            // The sub-select is used to preserve the notification setting. If
            // nothing is returned, the default is used.
            psql = db_conn.prepareStatement("REPLACE INTO player (uuid,name,notify,automatic) SELECT ?,?,notify,automatic FROM player WHERE uuid=?))");
            psql.setString(1, offlinePlayer.getUniqueId().toString());
            psql.setString(2, offlinePlayer.getName());
            psql.setString(3, offlinePlayer.getUniqueId().toString());
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to insert/replace newly joined player");
        }
    }

    // This method inserts a player's UUID into the groups table, against that
    // group's name and the group's owner's UUID.

    public String addPlayerToGroup(UUID owner, String group, String member) {
        PreparedStatement psql;
        String memberUUID;
        ResultSet result;
        try {
            psql = db_conn.prepareStatement("SELECT uuid FROM player WHERE name LIKE ?");
            psql.setString(1,member);
            result = psql.executeQuery();
            if (result.next()) {
                memberUUID = result.getString(1);
            } else {
                // Caller uses non-null return string to indicate failure, and sends it on.
                return String.format("Player %s is not known", member);
            }
            result.close();
            // Use of "REPLACE" syntax avoids duplicates
            psql = db_conn.prepareStatement("REPLACE INTO accessgroup (owner, name, member) VALUES (?,?,?)");
            psql.setString(1,owner.toString());
            psql.setString(2,group);
            psql.setString(3,memberUUID);
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to insert/replace group member");
        }
        return null;
    }

    // This method removes a player from a group, using a blind delete. It
    // doesn't matter if they're actually in the group or not.

    public String removePlayerFromGroup(UUID owner, String group, String member) {
        PreparedStatement psql;
        String memberUUID;
        ResultSet result;
        try {
            psql = db_conn.prepareStatement("SELECT uuid FROM player WHERE name LIKE ?");
            psql.setString(1,member);
            result = psql.executeQuery();
            if (result.next()) {
                memberUUID = result.getString(1);
            } else {
                // Caller uses non-null return string to indicate failure, and sends it on.
                return String.format("Player %s is not known", member);
            }
            result.close();
            psql = db_conn.prepareStatement("DELETE FROM accessgroup WHERE owner=? AND name LIKE ? AND member=?");
            psql.setString(1,owner.toString());
            psql.setString(2,group);
            psql.setString(3,memberUUID);
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to remove group member");
        }
        return null;
    }

    // Rename a group, for a specific user. This function is blind, and doesn't
    // care if the group doesn't exist. If the target name newName already exists as a
    // group, the end result is that all the players in the group are merged into the new
    // one. There's some duplicate handling here, because there is a key constraint.

    public void renameGroup(UUID owner, String group, String newName) {
        PreparedStatement psql;
        try {
            // Rename the access groups
            psql = db_conn.prepareStatement("UPDATE OR IGNORE accessgroup SET name=? WHERE name LIKE ? AND owner like ?");
            psql.setString(1,newName);
            psql.setString(2,group);
            psql.setString(3,owner.toString());
            psql.executeUpdate();
            // This query mops up all those left behind, who only remain because they're already in the target group
            psql = db_conn.prepareStatement("DELETE FROM accessgroup WHERE owner=? AND name LIKE ?");
            psql.setString(1,owner.toString());
            psql.setString(2, group);
            psql.executeUpdate();
            // Add the new access group name to access lists that have this access group
            psql = db_conn.prepareStatement("REPLACE INTO accesslist (member,x,y,z,world) " +
                    "SELECT ?,p.x,p.y,p.z,p.world FROM accesslist AS a " +
                    "INNER JOIN protected AS p ON a.x=p.x AND a.y=p.y AND a.z=p.z AND a.world=p.world " +
                    "WHERE member LIKE ? AND owner LIKE ?");
            psql.setString(1,newName);
            psql.setString(2,group);
            psql.setString(3,owner.toString());
            psql.executeUpdate();
            // Now remove the old access group name
            psql = db_conn.prepareStatement("DELETE FROM accesslist WHERE EXISTS(" +
                    "SELECT 1 FROM protected AS p " +
                    "WHERE accesslist.x=p.x AND accesslist.y=p.y AND accesslist.z=p.z AND accesslist.world=p.world " +
                    "AND member LIKE ? AND owner LIKE ?)");
            psql.setString(1,group);
            psql.setString(2,owner.toString());
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to rename group");
        }
    }

    // Method returns an ArrayList of Strings, being the names of players in a group.

    public List<String> getGroup(UUID owner, String name) {
        List<String> groupList = new ArrayList<>();
        PreparedStatement psql;
        ResultSet result;
        try {
            psql = db_conn.prepareStatement("SELECT player.name FROM accessgroup INNER JOIN player ON member=uuid WHERE owner=? AND accessgroup.name=? ORDER BY player.name");
            psql.setString(1, owner.toString());
            psql.setString(2, name);
            result = psql.executeQuery();
            while(result.next()) {
                result.getString(1);
                if(!result.wasNull())
                    groupList.add(result.getString(1));
            }
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to retrieve group membership list");
        }
        return groupList;
    }

    // Returns the UUID of the owner of the block at a Location. Works like a cut-down
    // getProtection, but takes a Location instead of a Block argument.

    public UUID getUUID(Location blockLocation) {
        Location location = getUnambiguousLocation(blockLocation.getBlock());
        PreparedStatement psql;
        ResultSet result;
        UUID returnVal = null;
        try {
            psql = db_conn.prepareStatement("SELECT owner " +
                    "FROM protectable " +
                    "INNER JOIN protected " +
                    "ON protectable.material=protected.material " +
                    "AND x=?" +
                    "AND y=?" +
                    "AND z=?" +
                    "AND world=?");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, location.getWorld().getName());
            result = psql.executeQuery();
            if (result.next()) {
                result.getString(1);
                if (result.wasNull()) {
                    returnVal = null;
                } else {
                    returnVal = UUID.fromString(result.getString(1));
                }
            }
            result.close();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to retrieve UUID from block");
        }
        return returnVal;
    }

    // Returns an ArrayList of Strings, being a list of group names for a player.

    public List<String> listGroups(UUID owner) {
        List<String> groupList = new ArrayList<>();
        PreparedStatement psql;
        ResultSet result;
        try {
            psql = db_conn.prepareStatement("SELECT name FROM accessgroup WHERE owner=? GROUP BY name ORDER BY name");
            psql.setString(1, owner.toString());
            result = psql.executeQuery();
            while(result.next()) {
                result.getString(1);
                if(!result.wasNull())
                    groupList.add(result.getString(1));
            }
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to retrieve group list");
        }
        return groupList;
    }

    // Function to fetch the name of a player, by UUID. Works on offline players
    // who have been seen by the plugin.

    public String getName(UUID player) {
        PreparedStatement psql;
        ResultSet result;
        String returnVal = null;
        try {
            psql = db_conn.prepareStatement("SELECT name FROM player WHERE uuid LIKE ?");
            psql.setString(1,player.toString());
            result = psql.executeQuery();
            if (result.next()) {
                returnVal = result.getString(1);
            }
            result.close();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to get UUID from name");
        }
        return returnVal;
    }

    // Adds a player or group to an access control list. Returns a string, which is
    // assumed by the caller to indicate success if non-null.

    public String addPlayerOrGroupToACL(Location blockLocation, UUID owner, String arg) {
        Location location = getUnambiguousLocation(blockLocation.getBlock());
        PreparedStatement psql;
        ResultSet results;
        Boolean useGroup = false;
        if (location == null) {
            return null;
        }
        try {
            // First get the owner of the block, if there is one.
            psql = db_conn.prepareStatement("SELECT count(*) FROM protected WHERE owner LIKE ? AND x=? AND y=? AND z=? AND world LIKE ?");
            psql.setString(1, owner.toString());
            psql.setInt(2, location.getBlockX());
            psql.setInt(3, location.getBlockY());
            psql.setInt(4, location.getBlockZ());
            psql.setString(5, location.getWorld().getName());
            results = psql.executeQuery();
            if (results.next() && results.getInt(1) > 0) {
                // The block is protected and owned by this player
                results.close();
                psql.close();
                // Return two rows; the first is the count of groups with arg as the name, the second the
                // count of players with arg as the name. It's unlikely that both will be non-zero, but
                // isn't fatal - it'll just use the group instead of the player.
                psql = db_conn.prepareStatement("SELECT count(*) FROM accessgroup WHERE owner LIKE ? AND name LIKE ?" +
                        " UNION ALL " +
                        "SELECT count(*) FROM player WHERE name LIKE ?");
                psql.setString(1, owner.toString());
                psql.setString(2, arg);
                psql.setString(3, arg);
                results = psql.executeQuery();
                if (results.next() && results.getInt(1) > 0) {
                    // A group exists by this name with this owner; we'll use group names first
                    useGroup = true;
                } else if (results.next() && results.getInt(1) == 0) {
                    // no group exists, but neither is there a player of that name
                    results.close();
                    psql.close();
                    return null;
                }
                results.close();
                psql.close();            if (useGroup) {
                    // Insert a group into the ACL by name
                    psql = db_conn.prepareStatement("REPLACE INTO accesslist (x, y, z, world, member) VALUES (?,?,?,?,?)");
                } else {
                    // Insert a player into the ACL by UUID, using subquery
                    psql = db_conn.prepareStatement("REPLACE INTO accesslist (x, y, z, world, member) VALUES (?,?,?,?,(" +
                            "SELECT uuid FROM player WHERE name LIKE ?" +
                            "))");
                }
                psql.setInt(1, location.getBlockX());
                psql.setInt(2, location.getBlockY());
                psql.setInt(3, location.getBlockZ());
                psql.setString(4, location.getWorld().getName());
                psql.setString(5, arg);
                psql.executeUpdate();
            } else {
                results.close();
                psql.close();
                return "This block is unowned";
            }
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to add item to ACL");
        }
        // Caller uses non-null return string to indicate success, and sends it on.
        return useGroup ? String.format("Added GROUP %s to access list", arg) :
                String.format("Added PLAYER %s to access list", arg);
    }

    // Removes a player and/or group from an access control list. Returns a string, which is
    // assumed by the caller to indicate success if non-null.

    public String removePlayerOrGroupFromACL(Location blockLocation, String arg) {
        Location location = getUnambiguousLocation(blockLocation.getBlock());
        PreparedStatement psql;
        if (location == null) {
            return null;
        }
        try {
            // Delete any player or group from the ACL by name
            psql = db_conn.prepareStatement("DELETE FROM accesslist WHERE x=? AND y=? AND z=? AND world like ? AND " +
                    "(member LIKE ? OR member LIKE (SELECT uuid FROM player WHERE name LIKE ?))");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, location.getWorld().getName());
            psql.setString(5, arg);
            psql.setString(6, arg);
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to remove item from ACL");
        }
        // Caller uses non-null return string to indicate success, and sends it on.
        return String.format("Removed %s from access list", arg);
    }

    // Returns an ArrayList of formatted strings, being the access control list for a
    // block, beginning with the owner, then listing groups and players.

    public List<String> getAccess(Location blockLocation) {
        Location location = getUnambiguousLocation(blockLocation.getBlock());
        List<String> accessDetails = new ArrayList<>();
        PreparedStatement psql;
        ResultSet results;
        if (location == null) {
            return null;
        }
        try {
            // First, attempt to list the owner
            psql = db_conn.prepareStatement("SELECT name FROM protected INNER JOIN player ON owner=uuid " +
                    "WHERE x=? AND y=? AND z=? AND world LIKE ?");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, location.getWorld().getName());
            results = psql.executeQuery();
            if (results.next()) {
                accessDetails.add(String.format("Owner of block at (%s,%s,%s) is %s",
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        results.getString(1)));
            }
            results.close();
            psql.close();
            // Next, attempt to list the groups
            psql = db_conn.prepareStatement("SELECT member FROM accesslist LEFT JOIN player ON member=uuid " +
                    "WHERE x=? AND y=? AND z=? AND world LIKE ? AND player.uuid IS NULL");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, location.getWorld().getName());
            results = psql.executeQuery();
            while (results.next()) {
                accessDetails.add(String.format("Group with access: %s", results.getString(1)));
            }
            results.close();
            psql.close();
            // Next, attempt to list the players
            psql = db_conn.prepareStatement("SELECT name FROM accesslist INNER JOIN player ON member=uuid " +
                    "WHERE x=? AND y=? AND z=? AND world LIKE ?");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, location.getWorld().getName());
            results = psql.executeQuery();
            while (results.next()) {
                accessDetails.add(String.format("Player with access: %s", results.getString(1)));
            }
            results.close();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to determine information about block");
        }
        return accessDetails;
    }

    // Simple to use Boolean method, answering the inverted question, can this player
    // use that target block?

    public boolean accessDenied(Player player, Block target) {
        PreparedStatement psql;
        ResultSet results;
        Location location = getUnambiguousLocation(target);
        String playerUUID = player.getUniqueId().toString();
        Boolean returnVal = false;
        try {
            // Complicated query. Join the lot, and look for the UUID in three of the fields.
            // If the count is non-zero, the player is allowed to use the block.
            psql = db_conn.prepareStatement("SELECT count(*) FROM protectable " +
                    "LEFT JOIN protected ON protectable.material=protected.material " +
                    "LEFT JOIN accesslist ON protected.x=accesslist.x " +
                                        "AND protected.y=accesslist.y " +
                                        "AND protected.z=accesslist.z " +
                                        "AND protected.world=accesslist.world " +
                    "LEFT JOIN accessgroup ON accesslist.member=accessgroup.name " +
                                        "AND accessgroup.owner=protected.owner "+
                    "WHERE protected.x=? AND protected.y=? AND protected.z=? AND protected.world LIKE ? " +
                    "AND (accesslist.member LIKE ? OR accessgroup.member LIKE ? OR protected.owner LIKE ?)");
            psql.setInt(1, location.getBlockX());
            psql.setInt(2, location.getBlockY());
            psql.setInt(3, location.getBlockZ());
            psql.setString(4, location.getWorld().getName());
            psql.setString(5, playerUUID);
            psql.setString(6, playerUUID);
            psql.setString(7, playerUUID);
            results = psql.executeQuery();
            if (results.next()) {
                returnVal = results.getInt(1) == 0;
            }
            results.close();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to check access for block");
        }
        return returnVal;
    }

    // Toggle notification bit for player. Doesn't do anything at the moment.

    public void toggleNotify(Player player) {
        PreparedStatement psql;
        try {
            psql = db_conn.prepareStatement("UPDATE player SET notify=1-notify WHERE uuid LIKE ?");
            psql.setString(1,player.getUniqueId().toString());
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to toggle notification for player");
        }
    }

    // Toggle automatic lock bit for player. Doesn't do anything at the moment.

    public void toggleAutomatic(Player player) {
        PreparedStatement psql;
        try {
            psql = db_conn.prepareStatement("UPDATE player SET automatic=1-automatic WHERE uuid LIKE ?");
            psql.setString(1,player.getUniqueId().toString());
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to toggle automatic lock for player");
        }
    }

    // Find out if a material is protectable

    public boolean isProtectable(Material type) {
        PreparedStatement psql;
        ResultSet results;
        Boolean returnVal = false;
        try {
            psql = db_conn.prepareStatement("SELECT 1 FROM protectable WHERE material LIKE ?");
            psql.setString(1,type.name());
            results = psql.executeQuery();
            if (results.next()) returnVal = true;
            results.close();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to determine if type is protectable");
        }
        return returnVal;
    }

    public void duplicate(Block target, Block source) {
        PreparedStatement psql;
        Location targetLoc = target.getLocation(), sourceLoc=source.getLocation();
        try {
            psql = db_conn.prepareStatement("INSERT INTO protected SELECT ? AS x,? AS y,? AS z, ? as world, material, owner FROM protected WHERE x=? AND y=? AND z=? AND world=?");
            psql.setInt(1,targetLoc.getBlockX());
            psql.setInt(2,targetLoc.getBlockY());
            psql.setInt(3,targetLoc.getBlockZ());
            psql.setString(4,targetLoc.getWorld().getName());
            psql.setInt(5,sourceLoc.getBlockX());
            psql.setInt(6,sourceLoc.getBlockY());
            psql.setInt(7,sourceLoc.getBlockZ());
            psql.setString(8,sourceLoc.getWorld().getName());
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to duplicate lock ownership information");
        }
        try {
            psql = db_conn.prepareStatement("INSERT INTO accesslist SELECT member, ? AS x,? AS y,? AS z, ? as world FROM accesslist WHERE x=? AND y=? AND z=? AND world=?");
            psql.setInt(1,targetLoc.getBlockX());
            psql.setInt(2,targetLoc.getBlockY());
            psql.setInt(3,targetLoc.getBlockZ());
            psql.setString(4,targetLoc.getWorld().getName());
            psql.setInt(5,sourceLoc.getBlockX());
            psql.setInt(6,sourceLoc.getBlockY());
            psql.setInt(7,sourceLoc.getBlockZ());
            psql.setString(8,sourceLoc.getWorld().getName());
            psql.executeUpdate();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to duplicate access list information");
        }
    }

    public Boolean getNotification(Player player) {
        PreparedStatement psql;
        ResultSet results;
        Boolean returnVal = false;
        try {
            psql = db_conn.prepareStatement("SELECT notify FROM player WHERE uuid LIKE ?");
            psql.setString(1,player.getUniqueId().toString());
            results = psql.executeQuery();
            if (results.next()) returnVal = (results.getInt(1) ==1);
            results.close();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to fetch notification setting for player");
        }
        return returnVal;
    }

    public Boolean getAutomatic(Player player) {
        PreparedStatement psql;
        ResultSet results;
        Boolean returnVal = false;
        try {
            psql = db_conn.prepareStatement("SELECT automatic FROM player WHERE uuid LIKE ?");
            psql.setString(1,player.getUniqueId().toString());
            results = psql.executeQuery();
            if (results.next()) returnVal = (results.getInt(1) ==1);
            results.close();
            psql.close();
        } catch (SQLException e) {
            stickylocks.getLogger().info("Failed to fetch automatic lock setting for player");
        }
        return returnVal;
    }
}