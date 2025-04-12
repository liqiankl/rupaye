package com.beulah.saver.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.beulah.saver.R;
import com.beulah.saver.activity.RupayeApplication;
import com.beulah.saver.fragment.HelpAboutFragment;
import com.beulah.saver.fragment.HelpRupayeFragment;
import com.beulah.saver.fragment.HelpFeedbackFragment;

/**
 * Created by Weiping on 2016/2/2.
 */

public class HelpFragmentAdapter extends FragmentStatePagerAdapter {

    private int position = 0;

    public HelpFragmentAdapter(android.support.v4.app.FragmentManager fm) {
        super(fm);
    }

    public HelpFragmentAdapter(android.support.v4.app.FragmentManager fm, int position) {
        super(fm);
        this.position = position;
    }

    @Override
    public Fragment getItem(int position) {
        switch (this.position) {
            case 0: return HelpRupayeFragment.newInstance();
            case 1: return HelpFeedbackFragment.newInstance();
            case 2: return HelpAboutFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (this.position) {
            case 0: return RupayeApplication.getAppContext().getResources().getString(R.string.app_name);
            case 1: return RupayeApplication.getAppContext().getResources().getString(R.string.feedback);
            case 2: return RupayeApplication.getAppContext().getResources().getString(R.string.about);
        }
        return "";
    }
}
