嵌套滑动NestedScrolling
嵌套滑动机制NestedScrolling
主题	嵌套滑动机制NestedScrolling
作者	2018/06/27 省留华
附件	demo代码
目录：
1.NestedScrolling机制简介

2.相关接口类的介绍

3.实现NestedScrolling机制

NestedScrolling机制简介
为什么要有NestedScrolling?

当Parent滑动到一定程度时，Child又开始滑动了，中间整个过程是没有间断的。从正常的事件分发（不手动调用分发事件，不手动去发出事件）角度去做是不可能的，因为当Parent拦截之后，是没有办法再把事件交给Child的，事件分发，对于拦截，相当于一锤子买卖，只要拦截了，当前手势接下来的事件都会交给Parent(拦截者)来处理。

NestedScrolling原理

嵌套滑动的基本原理是在子控件接收到滑动一段距离的请求时, 先询问父控件是否要滑动, 如果滑动了父控件就通知子控件它消耗了一部分滑动距离, 子控件就只处理剩下的滑动距离, 然后子控件滑动完毕后再把剩余的滑动距离传给父控件.

相关接口类的介绍
在LOLLIPOP(SDK21)之前官方在android.support.v4兼容包中提供了：

NestedScrollingChild //接口 9个方法
NestedScrollingParent //接口 8个方法
NestedScrollingChildHelper //辅助类
NestedScrollingParentHelper //辅助类
在SDK21之后嵌套滑动的相关逻辑作为普通方法直接写进View和ViewGroup类，普通方法是指这个方法不是继承自接口或者其他类。

在嵌套滑动中会要求控件要么是继承于SDK21之后的View或ViewGroup, 要么实现了这两个接口, 这是控件能够进行嵌套滑动的前提条件.

一些较新的 系统view 都已经实现了 NestedScrollingChild 或 NestedScrollingParent 接口，也就是说他们直接支持NestedScrolling，例如：
NestedScrollView 已实现 NestedScrollingParent 和 NestedScrollingChild
RecyclerView 已实现 NestedScrollingChild
CoordinatorLayout 已实现 NestedScrollingParent

注意：
虽然View和ViewGroup(SDK21之后)本身就具有嵌套滑动的相关方法, 但是默认情况是不会被调用, 因为View和ViewGroup本身不支持滑动，本身不支持滑动的控件即使有嵌套滑动的相关方法也不能进行嵌套滑动。

首先要控件类具有嵌套滑动的相关方法, 要么仅支持SDK21之后版本, 要么实现对应的接口, 为了兼容低版本, 更常用到的是后者。

因为默认的情况是不会支持滑动的, 所以控件要在合适的位置主动调起嵌套滑动的方法。

NestedScrollingChild 接口
public interface NestedScrollingChild {
    //是否支持嵌套滑动
    public void setNestedScrollingEnabled(boolean enabled)
    public boolean isNestedScrollingEnabled()

    //开始，结束滑动
    public boolean startNestedScroll(int axes)
    public void stopNestedScroll()

    //是否有支持嵌套的父布局（不要求是直接父布局）
    public boolean hasNestedScrollingParent()

    //滑动相关
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow)
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow)

    //滑翔相关
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed)
    public boolean dispatchNestedPreFling(float velocityX, float velocityY)
}
NestedScrollingParent 接口
public interface NestedScrollingParent {
    //开启、停止滑动回调
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes)
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes)
    public void onStopNestedScroll(View target)

    //触摸滑动时回调
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed)
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed)

    //滑翔回调
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed)
    public boolean onNestedPreFling(View target, float velocityX, float velocityY)

    //滑动方向
    public int getNestedScrollAxes()
}
从命名可以看出，这几个都是回调方法。当调用 NestedScrollingChild 中的方法时，NestedScrollingParent 中与之相对应的方法就会被回调。方法之间的具体对应关系如下：
1529999440167

NestedScrollingChild 主要方法介绍
boolean startNestedScroll(int axes)
在开始滑动的时候会调用这个方法，axes 代表滑动的方向，ViewCompat.SCROLL_AXIS_HORIZONTAL 代表水平滑动，ViewCompat.SCROLL_AXIS_VERTICAL 代表垂直滑动

返回值是布尔类型的，根据返回值，我们可以判断是否找到支持嵌套滑动的父View ，返回 true，表示在scrolling parent （需要注意的是这里不一定是直接scrolling parent ，间接scrolling parent 也可会返回 TRUE） 中找到支持嵌套滑动的。反之，则找不到。

boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow)
在scrolling child 滑动之前，提供机会让scrolling parent 先于scrolling child滑动。
dx，dy 是输入参数，表示scrolling child 传递给 scrolling parent 水平方向，垂直方向上的偏移量，consumed 是输出参数，consumed[0] 表示父 View 在水平方向上消费的值，，consumed[1 表示父 View 在垂直方向上消费的值。

返回值也是布尔类型的，根据这个值 ，我们可以判断scrolling parent 是都消费了相应距离

boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow)
在scrolling child 滑动之后，调用这个方法，提供机会给scrolling parent 滑动，dxConsumed，dyConsumed 是输入参数，表示scrolling child 在水平方向，垂直方向消耗的值，dxUnconsumed，dyUnconsumed 也是输入参数，表示scrolling child 在水平方向，垂直方向未消耗的值。

NestedScrollingParent 主要方法介绍
boolean onStartNestedScroll(View child, View target, int nestedScrollAxes)
在 Scrolling Child 开始滑动的时候会调用这个方法
当 Scrolling Child 调用 onStartNestedScroll 方法的时候，通过 NestedScrollingChildHelper 会回调 Scrolling parent 的 onStartNestedScroll 方法，如果返回 true， Scrolling parent 的 onNestedScrollAccepted(View child, View target, int nestedScrollAxes) 方法会被回调。
target 表示发起滑动事件的 View，Child 是 ViewParent 的直接子View，包含 target，nestedScrollAxes 表示滑动方向。

void onNestedScrollAccepted(View child, View target, int nestedScrollAxes)
如果 Scrolling Parent 的onStartNestedScroll 返回 true， Scrolling parent 的 onNestedScrollAccepted(View child, View target, int nestedScrollAxes) 方法会被回调

boolean onNestedPreScroll(View target, int dx, int dy, int[] consumed)
在 Scrolling Child 进行滑动之前，Scrolling Parent 可以先于Scrolling Child 进行相应的处理
如果 Scrolling Child 调用 dispatchNestedPreFling(float velocityX, float velocityY) ，通过 NestedScrollingChildHelper 会回调 Scrolling parent 的 onNestedPreScroll 方法

#####流程总结
我们可以大致将嵌套滚动的流程概括如下(以触摸滚动为例，惯性滚动(fling)的流程与此类似)：

调用child的startNestedScroll()来发起嵌套滚动流程(实质是寻找能够配合child进行嵌套滚动的parent)。parent的onStartNestedScroll()会被回调，如果此方法返回true，则onNestedScrollAccepted()也会被回调。

child每次滚动前，可以先询问parent是否要滚动，即调用dispatchNestedPreScroll()，这会回调到parent的onNestedPreScroll()，parent可以在这个回调中先于child滚动。
disdispatchNestedPreScroll()之后，child可以进行自己的滚动操作。

child滚动以后，可以调用dispatchNestedScroll()，会回调到parent的onNestedScroll()，在这里parent可以进行后于child的滚动。

滚动结束，调用stopNestedScroll()

####案例demo
demo比较简单明了，主要以理解流程为主。实际开发中一般不需要重复造轮子从头到尾实现方法并编写逻辑，可以用CoordinatorLayout, AppBarLayout , 自定义behavior等来实现比较个性的效果。

1530000208637

public class MyNestedScrollParent extends LinearLayout implements NestedScrollingParent

public class MyNestedScrollChild extends LinearLayout implements NestedScrollingChild
child控件关键代码：

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
滑动内部控件的时候注意边界检查：

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
parent控件关键代码：

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.e("xxx", getScrollY() + "  dy:" + dy + scrollParent(dy));
        if (scrollParent(dy)) {//如果需要显示或隐藏顶部，即需要自己(parent)滚动
            scrollBy(0, -dy);//滚动
            consumed[1] = dy;//告诉child我消费了多少
        }
    }