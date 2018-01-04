package io.github.anotherjack.avoidonresultdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

/**
 * @author zhengkaituo
 * @date 2018/1/4
 */
public class MultiFragmentActiivty extends AppCompatActivity {

    public static final int REQUEST_CODE_CALLBACK = 23;

    ViewPager vpContainer;
    Fragment[] mFragments = new Fragment[2];
    FragmentPagerAdapter mFragmentPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multifragment);
        initView();
    }

    private void initView() {
        vpContainer = (ViewPager) findViewById(R.id.vp_container);

        mFragments[0] = new FirstTestFragment();
        mFragments[1] = new SecondTestFragment();
        mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }
        };

        vpContainer.setAdapter(mFragmentPagerAdapter);
        vpContainer.setOffscreenPageLimit(2);

    }
}
