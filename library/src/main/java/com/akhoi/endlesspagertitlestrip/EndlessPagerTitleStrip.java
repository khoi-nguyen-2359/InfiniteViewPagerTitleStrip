package com.akhoi.endlesspagertitlestrip;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akhoi.endlesstitlestrip.R;

/**
 * Created by khoi2359 on 2/4/15.
 *
 * This view has measurement implementation copied from HorizontalScrollView to make its only child has extended width.</br>
 */
public class EndlessPagerTitleStrip extends FrameLayout implements ViewPager.OnPageChangeListener {
    private static final int MAX_SETTLE_DURATION = 600; // ms

    public interface EventListener {
        void onTitleClicked(int titleIndex);
    }

    private LinearLayout titleContainer;
    private int currPage;
    private int titleCount;
    private String[] titles;
    private int prevPage;
    private EventListener eventListener;
    private ViewPager viewPager;
    private long lastTapUpTime;

    private int titleStripAppearance;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EndlessPagerTitleStrip(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public EndlessPagerTitleStrip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public EndlessPagerTitleStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EndlessPagerTitleStrip(Context context) {
        super(context);
        init(null);
    }

    private void readAttibuteSet(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.EndlessPagerTitleStrip);
        titleStripAppearance = typedArray.getResourceId(R.styleable.EndlessPagerTitleStrip_titleStripAppearance, 0);
    }

    private void init(AttributeSet attrs) {
        readAttibuteSet(attrs);

        titleContainer = new LinearLayout(getContext());
        addView(titleContainer, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }
    });

    private OnTouchListener onContainerTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            boolean ret = gestureDetector.onTouchEvent(ev);

            long fromLastTouch = System.currentTimeMillis() - lastTapUpTime;        // handle new event after view pager is done with scrolling
            if (ret && fromLastTouch >= MAX_SETTLE_DURATION) {
                // this is for touching on title item
                lastTapUpTime = System.currentTimeMillis();
                int titleIndex = (int) v.getTag();
                if (eventListener != null) {
                    viewPager.setCurrentItem(titleIndex);
                    eventListener.onTitleClicked(titleIndex);
                }
            } else if (viewPager != null && fromLastTouch >= MAX_SETTLE_DURATION) {
                // delegate touch event to underlying view pager, this is for swiping on title item
                viewPager.onTouchEvent(ev);
            }

            return true;
        }
    };

    /**
     *
     * @param inflater
     * @param index index of the corresponding page that this title item is with
     * @return
     */
    private TextView createTitleTv(LayoutInflater inflater, int index) {
        TextView tvTitle = new TextView(getContext());
        tvTitle.setTextAppearance(getContext(), titleStripAppearance);
        tvTitle.setText(titles[index]);
        tvTitle.setTag(index);

        return tvTitle;
    }

    public void setViewPager(ViewPager viewPager) {
        PagerAdapter adapter = viewPager.getAdapter();
        titleCount = adapter.getCount();
//        if (adapter instanceof InfinitePagerAdapter) {
//            titleCount = ((InfinitePagerAdapter)adapter).getRealCount();
//        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        TextView[] children = new TextView[titleCount];
        titles = new String[titleCount];
        for (int i = 0; i < titleCount; ++i) {
            titles[i] = adapter.getPageTitle(i).toString();
            children[i] = createTitleTv(inflater, i);
        }

        titleContainer.removeAllViews();
        addTitle(createTitleTv(inflater, titleCount - 1), 0);
        for (TextView view : children) {
            addTitle(view, -1);
        }
        addTitle(createTitleTv(inflater, 0), -1);

        currPage = -1;
        prevPage = -1;

        // dont have width measured at this moment so do some calculation.
        TextView firstTitle = getTitle(0);
        float initX = firstTitle.getPaint().measureText(firstTitle.getText().toString()) + firstTitle.getPaddingLeft() + firstTitle.getPaddingRight();
        titleContainer.setX(- initX);
//        getTitle(1).setTextColor(getResources().getColor(R.color.pager_title));

        this.viewPager = viewPager;
        viewPager.setOnPageChangeListener(this);
    }

    private void removeTitle(int titleViewIndex) {
        View tvTitle = titleContainer.getChildAt(titleViewIndex);
        titleContainer.removeView(tvTitle);
        tvTitle.setOnClickListener(null);
    }

    private TextView getTitle(int index) {
        return (TextView) titleContainer.getChildAt(index);
    }

    /**
     * Add an title textview into the strip container
     * @param titleView
     * @param titleViewIndex pass index of the title in its container. negative number will be understood as adding at the end of container.
     */
    private void addTitle(TextView titleView, int titleViewIndex) {
        if (titleViewIndex >= 0) {
            titleContainer.addView(titleView, titleViewIndex);
        } else {
            titleContainer.addView(titleView);
        }
        titleView.setOnTouchListener(onContainerTouchListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            return;
        }

        if (getChildCount() > 0) {
            final View child = getChildAt(0);
            int width = getMeasuredWidth();
            if (child.getMeasuredWidth() < width) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop()
                        + getPaddingBottom(), lp.height);
                width -= getPaddingLeft();
                width -= getPaddingRight();
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();

        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec, getPaddingTop()
                + getPaddingBottom(), lp.height);

        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin
                        + heightUsed, lp.height);
        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                lp.leftMargin + lp.rightMargin, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // prevent first unexpected call to this callback
        if (currPage < 0) {
            prevPage = currPage;
            currPage = position;
            return;
        }

        float move = 0;
        if (currPage <= position) {
            // swiping right to left
            move = - getTitle(1).getWidth() * positionOffset - getTitle(0).getWidth();
        } else {
            // swiping left to right
            move = - getTitle(0).getWidth() * positionOffset;
        }

        titleContainer.setX(move);
    }

    @Override
    public void onPageSelected(int position) {
        prevPage = currPage;
        currPage = position;

        int offsreenX = getTitle(0).getWidth();
        int moveOffset = Math.abs(currPage - prevPage);     // it can be a multiple-page change
        if (currPage > prevPage) {
            offsreenX = 0;
            // swiped right to left
            for (int i = 0; i < moveOffset; ++i) {
                int lastTitleIndex = ((int) getTitle(titleCount + 1).getTag() + 1) % titleCount;
                removeTitle(0);
                addTitle(createTitleTv(LayoutInflater.from(getContext()), lastTitleIndex), -1);

                offsreenX += getTitle(0).getWidth();
            }
        } else if (currPage < prevPage) {
            offsreenX = 0;
            // swiped left to right
            for (int i = 0; i < moveOffset; ++i) {
                int firstTitleIndex = ((int) getTitle(0).getTag() + titleCount - 1) % titleCount;
                offsreenX += titleContainer.findViewWithTag(firstTitleIndex).getWidth();
                removeTitle(titleContainer.getChildCount() - 1);
                addTitle(createTitleTv(LayoutInflater.from(getContext()), firstTitleIndex), 0);
            }
        }

//        for (int i = 0; i < titleCount + 2; ++i) {
//            if (i == 1) {
//                getTitle(i).setTextColor(getResources().getColor(R.color.pager_title));
//            } else {
//                getTitle(i).setTextColor(getResources().getColor(R.color.pager_title_dim));
//            }
//        }

        titleContainer.setX(- offsreenX);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }
}
