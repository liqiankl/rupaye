package com.beulah.saver.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.beulah.saver.activity.RupayeApplication;
import com.beulah.saver.fragment.TagViewFragment;
import com.beulah.saver.model.RecordManager;
import com.beulah.saver.util.RupayeUtil;

/**
 * Created by 伟平 on 2015/10/20.
 */
public class TagViewFragmentAdapter extends FragmentStatePagerAdapter {

    public TagViewFragmentAdapter(android.support.v4.app.FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return TagViewFragment.newInstance(i);
    }

    @Override
    public int getCount() {
        return RecordManager.getInstance(RupayeApplication.getAppContext()).TAGS.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return RupayeUtil.GetTagName(
                RecordManager.getInstance(RupayeApplication.getAppContext()).TAGS.get(position % RecordManager.TAGS.size()).getId());
    }
}
