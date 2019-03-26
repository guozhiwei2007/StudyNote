package com.xin.nestedscroll.view;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class MyNestedScrollParent extends LinearLayout implements NestedScrollingParent {
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private View myNestedScrollChild;

    public MyNestedScrollParent(Context context) {
        super(context);
    }

    public MyNestedScrollParent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        myNestedScrollChild = (View) getChildAt(2);
    }

    //在此可以判断参数target是哪一个子view以及滚动的方向，然后决定是否要配合其进行嵌套滚动
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return true;
    }


    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
    }

    //先于child滚动
    //前3个为输入参数，最后一个是输出参数
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.e("xxx", getScrollY() + "  dy:" + dy + scrollParent(dy));
        if (scrollParent(dy)) {//如果需要显示或隐藏顶部，即需要自己(parent)滚动
            scrollBy(0, -dy);//滚动
            consumed[1] = dy;//告诉child我消费了多少
        }
    }

    //后于child滚动
    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    //返回值：是否消费了fling
    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    //返回值：是否消费了fling
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }


    public boolean scrollParent(int dy) {
        //下拉的时候是否要滑动自己
        if (dy > 0) {
            if (getScrollY() > 0 && myNestedScrollChild.getScrollY() == 0) {
                return true;
            }
        }
        //上拉的时候是否要滑动自己
        if (dy < 0) {
            if (getScrollY() < 400) {
                return true;
            }
        }
        return false;
    }


    //scrollBy内部会调用scrollTo
    //限制滚动范围
    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > 400) {
            y = 400;
        }

        super.scrollTo(x, y);
    }
}
