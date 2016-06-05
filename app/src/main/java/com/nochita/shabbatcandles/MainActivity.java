package com.nochita.shabbatcandles;

import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

public class MainActivity extends BaseActivity {

    @Override
    protected Fragment getFragment() {
        return MainFragment.newInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

}
