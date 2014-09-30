package me.ppxpp.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by zhengmeng on 2014/9/29.
 */
public class MainContainer extends ViewGroup {
    public MainContainer(Context context) {
        this(context, null, 0);
    }

    public MainContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private static int mTouchSlop;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private static final float THRESHOLD_VELOCITY_X = 3000;
    private static final int STATUS_RESET = 0;
    private static final int STATUS_SCROLLING = 1;
    private int curtStatus = STATUS_RESET;
    private int curtScreen = 0;
    float lastX, lastY;



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("onInterceptTouchEvent", "onInterceptTouchEvent--begin");
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE && curtStatus == STATUS_SCROLLING){
            Log.d("onInterceptTouchEvent", "return true");
            return true;
        }
        switch (action & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getX();
                lastY = ev.getY();
                curtStatus = mScroller.isFinished()?STATUS_RESET:STATUS_SCROLLING;
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                curtStatus = STATUS_RESET;
                break;
            case MotionEvent.ACTION_MOVE:
                float diff_x = Math.abs(ev.getX() - lastX);
                float diff_y = Math.abs(ev.getY() - lastY);
                if (diff_x > mTouchSlop && diff_x > diff_y){
                    curtStatus = STATUS_SCROLLING;
                }
                break;
        }
        Log.d("onInterceptTouchEvent", "curtStatus = " + curtStatus);
        return curtStatus == STATUS_SCROLLING;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        Log.d("onTouchEvent","PointerCount = "+event.getPointerCount());
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP){
            Log.d("onTouchEvent", "ACTION_POINTER_UP");
        }
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP){
            Log.d("onTouchEvent", "ACTION_UP");
        }
        if (event.getPointerCount() != 1){
            //return true;
        }

        if (mVelocityTracker == null){
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                Log.d("mScroller.isFinished()", ""+ mScroller.isFinished());
                if (!mScroller.isFinished()){
                    Log.d("abortAnimation", "abortAnimation");
                    mScroller.abortAnimation();
                }
                lastX = event.getX();
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d("ACTION_POINTER_UP", "X=" + event.getX()+", y="+getY());
                //break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (curtStatus == STATUS_RESET)
                    break;
                //get velocity piexs per seconds
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float velocityY = mVelocityTracker.getYVelocity();
                Log.d("ACTION_UP", "velocityX = " + velocityX + ", velocityY = " + velocityY);
                Log.d("ACTION_UP", "current screen = " + curtScreen);
                if (velocityX > THRESHOLD_VELOCITY_X) {
                    gotoScreen(curtScreen - 1);
                } else if (velocityX < -THRESHOLD_VELOCITY_X) {
                    gotoScreen(curtScreen + 1);
                } else {
                    gotoScreen();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                curtStatus = STATUS_RESET;
                break;
            case MotionEvent.ACTION_MOVE:
                float detalX = lastX - event.getX();
                float detalY = lastY - event.getY();
                float diff_x = Math.abs(detalX);
                float diff_y = Math.abs(detalY);
                if (diff_x > mTouchSlop && diff_x > diff_y){
                    curtStatus = STATUS_SCROLLING;
                }
                if (curtStatus == STATUS_RESET)
                    break;
                int curtScrollX = getScrollX();
                if (getScrollX() <= 0 && detalX < 0){
                    detalX /= 2;
                }else if(curtScrollX >= getWidth() * (getChildCount() - 1)
                        && detalX > 0){
                    detalX /= 2;
                }
                Log.d("ACTION_MOVE", "detaX = " + detalX);
                if (detalX != 0) {
                    scrollBy((int) detalX, 0);
                    lastX = event.getX();
                }
                break;
        }
        return true;
    }

    private void gotoScreen(){
        int curtScrollX = getScrollX();
        int screen = (curtScrollX + getWidth()/2) / getWidth();
        Log.d("gotoScreen()", "goto screen " + screen);
        this.gotoScreen(screen);
    }

    private void gotoScreen(int whichScreen){
        if (whichScreen >= getChildCount()){
            whichScreen = getChildCount() - 1;
        }
        if (whichScreen < 0){
            whichScreen = 0;
        }
        curtScreen = whichScreen;
        int width = getWidth();
        int dstX = width * curtScreen;
        int curtX = getScrollX();
        int detaX = dstX - curtX;
        mScroller.startScroll(curtX, getScrollY(), detaX, 0, 500);
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int mw = getMeasuredWidth();
        Log.d("onLayout", "width="+width+", measuredWidth="+mw);
        int left = 0;
        for (int i = 0 ; i < getChildCount(); i++){
            View child = getChildAt(i);
            child.layout(left, t, left + width, b);
            left += width;
        }
        Log.d("onLayout", "scrollX="+getScrollX()+", scrollY="+getScrollY());
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()){
            int x = mScroller.getCurrX();
            scrollTo(x, mScroller.getCurrY());
            postInvalidate();
        }
    }
}
