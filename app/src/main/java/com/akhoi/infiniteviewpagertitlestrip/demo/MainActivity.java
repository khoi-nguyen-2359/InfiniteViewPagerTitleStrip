package com.akhoi.infiniteviewpagertitlestrip.demo;

import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akhoi.infiniteviewpagertitlestrip.InfiniteViewPagerTitleStrip;
import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.antonyt.infiniteviewpager.InfiniteViewPager;


public class MainActivity extends ActionBarActivity {

    static final String[] titles = new String[]{"coconut", "organ", "grapes"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InfiniteViewPager viewPager = (InfiniteViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new InfinitePagerAdapter(new MyAdapter()));

        InfiniteViewPagerTitleStrip titleStrip = (InfiniteViewPagerTitleStrip) findViewById(R.id.view_title_strip);
        titleStrip.setViewPager(viewPager);
    }

    class MyAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ViewGroup item = (ViewGroup) LayoutInflater.from(MainActivity.this).inflate(R.layout.item_viewpager, container, false);
            TextView tv = (TextView) item.getChildAt(0);
            tv.setText("page content " + position);
            container.addView(item);

            return item;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
