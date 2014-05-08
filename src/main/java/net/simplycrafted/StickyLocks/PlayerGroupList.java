package net.simplycrafted.StickyLocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright © Brian Ronald
 * 07/05/14
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
public class PlayerGroupList {
    // Little utility class for returning a list of player names for output. It could
    // probably be done much better than this.

    private List<String> groupList;

    public PlayerGroupList() {
        groupList = new ArrayList<String>();
    }

    public Iterator getIterator() {
        return groupList.iterator();
    }

    public boolean isEmpty() {
        return groupList.isEmpty();
    }

    public void insert(String name) {
        groupList.add(name);
    }

}
