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

package com.frostwire.android.gui.activities;

import android.content.Intent;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.views.AbstractActivity;
import com.frostwire.android.gui.views.TOS;
import com.frostwire.android.gui.views.TOS.OnTOSAcceptListener;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class LaunchActivity extends AbstractActivity {

    public LaunchActivity() {
        super(R.layout.activity_launch);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_TOS_ACCEPTED)) {
            if (ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_INITIAL_SETTINGS_COMPLETE)) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, WizardActivity.class));
            }
        } else {
            trackDialog(TOS.showEula(this, new OnTOSAcceptListener() {
                public void onAccept() {
                    startActivity(new Intent(LaunchActivity.this, WizardActivity.class));
                }
            }));
        }
    }
}
