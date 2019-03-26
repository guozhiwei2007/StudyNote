package com.xin.nestedscroll.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;


public class MyNestedScrollChild extends LinearLayout implements NestedScrollingChild {
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mOffset = new int[2]; //偏移量
    private final int[] mConsumed = new int[2]; //消费
    private int mLastY;
    private int mShowHeight;


    public MyNestedScrollChild(Context context) {
        super(context);
    }

    public MyNestedScrollChild(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //第一次测量，因为布局文件中高度是wrap_content，因此测量模式为atmost，即高度不超过父控件的剩余空间
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mShowHeight = getMeasuredHeight();

        //第二次测量，对高度没有任何限制，那么测量出来的就是完全展示内容所需要的高度
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            //按下
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) event.getRawY();
                startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL);
                break;
            //移动
            case MotionEvent.ACTION_MOVE:
                int y = (int) (event.getRawY());
                int dy = y - mLastY;
                mLastY = y;
                if (dispatchNestedPreScroll(0, dy, mConsumed, mOffset)) //如果找到了支持嵌套滑动的父类,父类进行了一系列的滑动
                {
                    //获取滑动距离
                    int remain = dy - mConsumed[1];
                    if (remain != 0) {
                        scrollBy(0, -remain);
                        dispatchNestedScroll(0, remain, 0, 0, mOffset);
                    }

                } else {
                    scrollBy(0, -dy);
                }
                break;
        }

        return true;
    }

    //限制滚动范围
    @Override
    public void scrollTo(int x, int y) {
        int maxY = getMeasuredHeight() - mShowHeight;
        if (y > maxY) {
            y = maxY;
        }
        if (y < 0) {
            y = 0;
        }
        Log.e("xxx", maxY + " " + y);
        super.scrollTo(x, y);
    }

    //初始化helper对象
    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mNestedScrollingChildHelper == null) {
            mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
            mNestedScrollingChildHelper.setNestedScrollingEnabled(true);
        }
        return mNestedScrollingChildHelper;
    }

    //实现一下接口
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getScrollingChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getScrollingChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {

        return getScrollingChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }
}
