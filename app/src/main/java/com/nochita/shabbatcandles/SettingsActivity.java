package com.nochita.shabbatcandles;

import android.support.v4.app.Fragment;

public class SettingsActivity extends BaseActivity {

    @Override
    protected Fragment getFragment() {
        return SettingsFragment.newInstance();
    }
}
