package net.simplycrafted.StickyLocks;

/**
 * Copyright © Brian Ronald
 * 07/05/14
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
public class PlayerGroupList {
    // Little utility class for returning a list of player names for output. It could
    // probably be done much better than this.

    private String[] groupList;
    private int pointer=0;

    public PlayerGroupList() {
        groupList = null;
    }

    public String getFirst() {
        pointer=0;
        return groupList[0];
    }

    public String getNext() {
        if (groupList.length < pointer) {
            pointer += 1;
            return groupList[pointer];
        } else {
            return null;
        }
    }

    public boolean isEmpty() {
        return groupList.length == 0;
    }

    public int getSize() {
        return groupList.length;
    }

    public void insert(String name) {
        groupList[groupList.length] = name;
    }
}
