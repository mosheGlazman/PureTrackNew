package com.supercom.puretrack.util.custom_implementations;

import android.text.TextWatcher;

public abstract class OnOnlyAfterTextChangedListener implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
}
