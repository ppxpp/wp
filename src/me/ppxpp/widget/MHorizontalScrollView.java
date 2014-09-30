package me.ppxpp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by zhengmeng on 2014/9/29.
 */
public class MHorizontalScrollView extends HorizontalScrollView {
    public MHorizontalScrollView(Context context) {
        super(context);
    }

    public MHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP){
            float scrollX = getScrollX();
            Log.d("onTouchEvent", "aaa,scrollX="+scrollX);
            int width = getWidth();
            if (scrollX < width/2){
                scrollTo(0, 0);
            }else{
                scrollTo(width, 0);
            }
        }
        return super.onTouchEvent(ev);
    }

    ScrollChangeListener mListener;

    public void setScrollChangeListener(ScrollChangeListener l){
        this.mListener = l;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //Log.d("onScrollChanged","width="+getWidth() +", l = " + l);
        if (this.mListener != null){
            this.mListener.onScrollChanged(l,t,oldl,oldt);
        }
    }

    public interface ScrollChangeListener{
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }
}
