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

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import com.frostwire.android.R;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ClearableEditTextView extends RelativeLayout {

    private AutoCompleteTextView input;
    private Button buttonClear;

    private OnActionListener listener;
    private String hint;

    public ClearableEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ClearableEditTextView);
        hint = arr.getString(R.styleable.ClearableEditTextView_clearable_hint);
    }

    public OnActionListener getOnActionListener() {
        return listener;
    }

    public void setOnActionListener(OnActionListener listener) {
        this.listener = listener;
    }

    @Override
    public OnFocusChangeListener getOnFocusChangeListener() {
        return input.getOnFocusChangeListener();
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        input.setOnFocusChangeListener(l);
    }

    @Override
    public void setOnKeyListener(OnKeyListener l) {
        input.setOnKeyListener(l);
    }

    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        input.setAdapter(adapter);
    }

    public Editable getText() {
        return input.getText();
    }

    public void setText(String text) {
        input.setText(text);
    }

    public void setListSelection(int position) {
        input.setListSelection(position);
    }

    public void dismissDropDown() {
        input.dismissDropDown();
    }

    public void selectAll() {
        input.selectAll();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View.inflate(getContext(), R.layout.view_clearable_edit_text, this);

        input = (AutoCompleteTextView) findViewById(R.id.view_clearable_edit_text_input);
        input.setHint(hint);
        input.setTransformationMethod(new SingleLineTransformationMethod());
        input.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    buttonClear.setVisibility(RelativeLayout.VISIBLE);
                } else {
                    buttonClear.setVisibility(RelativeLayout.INVISIBLE);
                }
                ClearableEditTextView.this.onTextChanged(s.toString());
                input.setListSelection(-1);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        buttonClear = (Button) findViewById(R.id.view_clearable_edit_text_button_clear);
        buttonClear.setVisibility(RelativeLayout.INVISIBLE);
        buttonClear.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                input.setText("");
                onClear();
            }
        });
    }

    private void onTextChanged(String str) {
        if (listener != null) {
            listener.onTextChanged(this, str.trim());
        }
    }

    private void onClear() {
        if (listener != null) {
            listener.onClear(this);
        }
    }

    public static interface OnActionListener {

        public void onTextChanged(View v, String str);

        public void onClear(View v);
    }

    private static class SingleLineTransformationMethod extends ReplacementTransformationMethod {

        private static char[] ORIGINAL = new char[] { '\n', '\r' };
        private static char[] REPLACEMENT = new char[] { '\uFEFF', '\uFEFF' };

        protected char[] getOriginal() {
            return ORIGINAL;
        }

        protected char[] getReplacement() {
            return REPLACEMENT;
        }
    }
}
