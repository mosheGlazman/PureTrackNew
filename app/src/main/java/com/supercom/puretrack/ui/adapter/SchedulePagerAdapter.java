package com.supercom.puretrack.ui.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.supercom.puretrack.data.source.local.table_managers.TableScheduleOfZones;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.ui.schedule.ScheduleTab;
import com.supercom.puretrack.util.date.DateFormatterUtil;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class SchedulePagerAdapter extends FragmentStatePagerAdapter {

    private final SparseArray<Fragment> mPages;
    private final ArrayList<String> scheduleDays;

    public SchedulePagerAdapter(FragmentManager fm, ArrayList<String> scheduleDays) {
        super(fm);
        mPages = new SparseArray<>();
        this.scheduleDays = scheduleDays;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment f = createItem(position);
        mPages.put(position, f);
        return f;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (0 <= mPages.indexOfKey(position)) {
            mPages.remove(position);
        }
        super.destroyItem(container, position, object);
    }

    public Fragment getItemAt(int position) {
        return mPages.get(position);
    }

    protected Fragment createItem(int position) {
        if(position < 0 || position > 20){
            return null;
        }

        return createTab(position, (position - 2)*24, (position - 1)*24);
    }

    private Fragment createTab(int pageNumber, int startTimeOffset, int endTimeOffset) {
        List<EntityScheduleOfZones> recordsScheduleOfZonesList;
        recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(startTimeOffset, endTimeOffset);
        return ScheduleTab.newInstance(recordsScheduleOfZonesList, TimeUtil.convertDateToFormat(scheduleDays.get(pageNumber), DateFormatterUtil.DMY), TimeUtil.GetTimeWithOffset(startTimeOffset), TimeUtil.GetTimeWithOffset(endTimeOffset));
    }

    @Override
    public int getCount() {
        return scheduleDays.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TimeUtil.convertDateToFormat(scheduleDays.get(position), DateFormatterUtil.D);
    }

}
