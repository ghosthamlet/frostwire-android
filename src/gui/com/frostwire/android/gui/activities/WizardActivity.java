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

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ViewFlipper;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.views.AbstractActivity;
import com.frostwire.android.gui.views.WizardPageView;
import com.frostwire.android.gui.views.WizardPageView.OnCompleteListener;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class WizardActivity extends AbstractActivity {

    private final OnCompleteListener completeListener;

    private Button buttonPrevious;
    private Button buttonNext;
    private ViewFlipper viewFlipper;

    private View currentPageView;

    public WizardActivity() {
        super(R.layout.activity_wizard);

        completeListener = new OnCompleteListener() {
            public void onComplete(WizardPageView pageView, boolean complete) {
                if (pageView == currentPageView) {
                    buttonNext.setEnabled(complete);
                }
            }
        };
    }

    /**
     * Note: This is supposed to be deprecated, but for some reason, the new Fragment api way doesn't
     * work well with the compatibility library.
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return viewFlipper.getCurrentView().getTag();
    }

    @Override
    protected void initComponents() {
        buttonPrevious = findView(R.id.activity_wizard_button_previous);
        buttonPrevious.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                previousPage();
            }
        });

        buttonNext = findView(R.id.activity_wizard_button_next);
        buttonNext.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                nextPage();
            }
        });

        viewFlipper = findView(R.id.activity_wizard_view_flipper);
        int count = viewFlipper.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewFlipper.getChildAt(i);
            if (view instanceof WizardPageView) {
                ((WizardPageView) view).setOnCompleteListener(completeListener);
            }
            view.setTag(i);
        }

        Integer currentIndex = (Integer) getLastCustomNonConfigurationInstance();
        if (currentIndex != null) {
            viewFlipper.setDisplayedChild(currentIndex);
        }

        setupViewPage();
    }

    private void previousPage() {
        viewFlipper.showPrevious();
        setupViewPage();
    }

    private void nextPage() {
        View view = viewFlipper.getCurrentView();
        if (view instanceof WizardPageView) {
            WizardPageView pageView = (WizardPageView) view;
            pageView.finish();
            if (!pageView.hasNext()) {
                ConfigurationManager.instance().setBoolean(Constants.PREF_KEY_GUI_INITIAL_SETTINGS_COMPLETE, true);
                finish();
            } else {
                viewFlipper.showNext();
                setupViewPage();
            }
        } else {
            viewFlipper.showNext();
            setupViewPage();
        }
    }

    private void setupViewPage() {
        View view = viewFlipper.getCurrentView();
        currentPageView = view;
        if (view instanceof WizardPageView) {
            WizardPageView pageView = (WizardPageView) view;

            buttonPrevious.setVisibility(pageView.hasPrevious() ? View.VISIBLE : View.INVISIBLE);
            buttonNext.setText(pageView.hasNext() ? R.string.wizard_next : R.string.wizard_finish);
            buttonNext.setEnabled(false);

            pageView.load();
        } else {
            buttonPrevious.setVisibility(View.VISIBLE);
            buttonNext.setEnabled(true);
        }
    }
}
