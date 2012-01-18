/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.android.gui.views;

import android.view.Menu;
import android.view.MenuItem;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public interface OptionsMenuBuilder {

    /**
     * Initialize the contents of the Activity's standard options menu.
     * You should place your menu items in to menu.
     * 
     * This is only called once, the first time the options menu is
     * displayed. To update the menu every time it is displayed, see
     * onPrepare(Menu).
     * 
     * You can safely hold on to menu (and any items created from it),
     * making modifications to it as desired, until the next time
     * onCreate() is called.
     * 
     * You need implement onItemSelected(MenuItem) method to handle
     * them there.
     * 
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    public boolean onCreate(Menu menu);

    /**
     * Prepare the options menu to be displayed. This is called right
     * before the menu is shown, every time it is shown. You can use
     * this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.
     * 
     * @param menu The options menu as last shown or first initialized by onCreate().
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    public boolean onPrepare(Menu menu);

    /**
     * This hook is called whenever an item in your options menu is selected.
     * 
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed,
     * true to consume it here.
     */
    public boolean onItemSelected(MenuItem item);
}
