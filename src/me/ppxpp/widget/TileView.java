package me.ppxpp.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.TextureView;

import java.util.concurrent.LinkedBlockingQueue;

import me.ppxpp.animation.Rotate3dAnimation;
import me.ppxpp.utils.FloatingEffect;
import me.ppxpp.wp.R;

/**
 * TODO: document your custom view class.
 */
public class TileView extends TextureView implements TextureView.SurfaceTextureListener{

    private String Tag = "TileView";

    public int mSizeX = 2;
    public int mSizeY = 2;
    public int x = -1, y = -1;
    public int BackgroundColor = Color.BLACK;
    public int ForcegroundColor = Color.BLUE;

    public static SparseArray<Bitmap> BitmapCache = new SparseArray<Bitmap>();


    public TileView(Context context) {
        super(context);
        init(null, 0);
    }

    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TileView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TileView, defStyle, 0);

        mSizeX = a.getInt(R.styleable.TileView_sizeX, 2);
        mSizeY = a.getInt(R.styleable.TileView_sizeY, 2);
        x = a.getInt(R.styleable.TileView_x, 0);
        if (x < 0){
            throw  new IllegalArgumentException("Tile need a not negative x coordinate");
        }
        y = a.getInt(R.styleable.TileView_y, 0);
        if (y < 0){
            throw  new IllegalArgumentException("Tile need a not negative y coordinate");
        }
        a.recycle();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(25);
        textPaint.setColor(Color.WHITE);

        this.setSurfaceTextureListener(this);
    }

    Paint textPaint;


    public static final int STATUS_EDIT = 1;
    public static final int STATUS_FLOAT = 2;
    public static final int STATUS_NORMAL = 3;
    public static final int STATUS_PRESSED = 4;

    private int mCurtStatus = STATUS_NORMAL;

    private static enum Corner{
        LeftTop, Top, RightTop,
        leftCenter, Center, RightCenter,
        LeftBottom, Bottom, RightBottom;
    }

    private static Corner[][] CornerMatrix = {
            {Corner.LeftTop, Corner.Top, Corner.RightTop},
            {Corner.leftCenter, Corner.Center, Corner.RightCenter},
            {Corner.LeftBottom, Corner.Bottom, Corner.RightBottom}
    };


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if(width / mSizeX * mSizeY <= height){
            height = width / mSizeX * mSizeY;
        }else{
            width = height / mSizeY * mSizeX;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        switch (action){
            case MotionEvent.ACTION_DOWN:
                onActionDown(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onActionCancelOrUp(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onActionMove(event);
                break;
        }
        //return super.onTouchEvent(event);
        return true;
    }

    private boolean onActionCancelOrUp(MotionEvent event){
        if (!mHasPerformedLongPress) {
            mCurtStatus = STATUS_NORMAL;
            switchToNormalStatus();
            performClick();
        }
        return true;
    }



    private boolean onActionDown(MotionEvent event){
        if (mCurtStatus == STATUS_NORMAL){
            mCurtStatus = STATUS_PRESSED;
            //determin which corner was pressed
            int x = (int)event.getX();
            int y = (int)event.getY();
            switchToPressedStatus(detectCorner(x, y));
            //start to detect if it is long press
            mHasPerformedLongPress = false;
            checkForLongPress(0);
            return true;
        }else if (mCurtStatus == STATUS_EDIT){
            //check if click button

        }
        return true;
    }

    private boolean onActionMove(MotionEvent event){
        return true;
    }

    private void switchToPressedStatus(Corner pressedCorner){
        Log.d(Tag, "switchToPressedState, corner = "+ pressedCorner);
        //pressOnRightCenter();
        pressOnCenter();
    }

    private void pressOnCenter(){
        Canvas canvas = lockCanvas();
        canvas.drawColor(BackgroundColor);
        canvas.save();
        Rect rect = new Rect(PressedScale, PressedScale,
                mSurfaceTextureWidth - PressedScale * 2, mSurfaceTextureHeight - PressedScale * 2);
        canvas.clipRect(rect);
        canvas.drawColor(ForcegroundColor);
        canvas.drawText("locationY="+y, rect.left, rect.top + 100, textPaint);
        canvas.restore();
        unlockCanvasAndPost(canvas);

    }
    private void pressOnRightCenter(){

        Rotate3dAnimation animation = new Rotate3dAnimation(0, 5, 0, mSurfaceTextureHeight/2, 0, true);
        //Rotate3dAnimation animation = new Rotate3dAnimation(0, 2, 0, 0, 10, true);
        //animation.setDuration(100);
        animation.setFillAfter(true);
        this.startAnimation(animation);
    }

    public void switchToFloatStatus(){
        mCurtStatus = STATUS_FLOAT;
        if (mFloatingDrawer == null){
            mFloatingDrawer = new FloatingDrawer();
        }
        Thread drawThread = new Thread(mFloatingDrawer);
        drawThread.start();
    }

    public void switchToEditStatus(){
        mCurtStatus = STATUS_EDIT;
        final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
        final Rect drawRect = new Rect(mDrawRectNormal);
        if (mEditDrawer == null){
            mEditDrawer = new EditDrawer(drawRect, queue);
        }
        ValueAnimator animator = ValueAnimator.ofInt(0, FLOAT_GAP);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer)animation.getAnimatedValue();
                try {
                    queue.put(val);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        animator.start();
        new Thread(mEditDrawer).start();
    }



    public void switchToNormalStatus(){
        mCurtStatus = STATUS_NORMAL;
        Canvas canvas = lockCanvas();
        canvas.drawColor(BackgroundColor);
        canvas.save();
        canvas.clipRect(mDrawRectNormal);
        canvas.drawColor(ForcegroundColor);
        canvas.drawText("locationY="+y, 0, 100, textPaint);
        canvas.restore();
        unlockCanvasAndPost(canvas);
    }

    private Corner detectCorner(int x, int y){
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int idx1 = height / 2 > y ? 0 : (height / 3 * 2 > y ? 1 : 2);
        int idx2 = width / 3 > x ? 0 : (width / 3 * 2 > x ? 1 : 2);
        return CornerMatrix[idx1][idx2];
    }


    @Override
    public boolean performClick() {
        //switch to normal status
        return super.performClick();
    }

    @Override
    public boolean performLongClick() {
        //switch to edit status
        switchToEditStatus();
        return super.performLongClick();
    }

    public void setStatus(int status){
        mCurtStatus = status;
    }

    public void refreshUI(){
        switch (mCurtStatus){
            case STATUS_EDIT:
                switchToEditStatus();
                break;
            case STATUS_FLOAT:
                switchToFloatStatus();
                break;
            case STATUS_NORMAL:
                switchToNormalStatus();
                break;
        }
    }

    private int mSurfaceTextureWidth;
    private int mSurfaceTextureHeight;
    private static final int PressedScale = 3;
    private static final int FLOAT_GAP = 21;
    private Rect mDrawRectNormal;
    private Rect mDrawRectFloat;
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d("TileView", "onSurfaceTextureAvailable, locationY =" + y +", width =" + width + ", height="+height);
        mSurfaceTextureWidth = width;
        mSurfaceTextureHeight = height;
        mDrawRectFloat = new Rect(FLOAT_GAP, FLOAT_GAP, width - FLOAT_GAP, height - FLOAT_GAP);
        mDrawRectNormal = new Rect(0, 0, width, height);

        if (mCurtStatus == STATUS_NORMAL){
            switchToNormalStatus();
        }else if (mCurtStatus == STATUS_FLOAT){
            switchToFloatStatus();
        }else if(mCurtStatus == STATUS_EDIT){
            switchToEditStatus();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        //Log.d("TileView", "onSurfaceTextureSizeChanged, locationY =" + y);
        mSurfaceTextureWidth = width;
        mSurfaceTextureHeight = height;
        mDrawRectFloat = new Rect(FLOAT_GAP, FLOAT_GAP, width - FLOAT_GAP, height - FLOAT_GAP);
        mDrawRectNormal = new Rect(0, 0, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        //Log.d("TileView", "onSurfaceTextureDestroyed, locationY =" + y);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //Log.d("TileView", "onSurfaceTextureUpdated, locationY =" + y);

    }

    private CheckForLongPress mPendingCheckForLongPress;
    private boolean mHasPerformedLongPress;
    private void checkForLongPress(int delayOffset){
        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        //postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout() - delayOffset);
        postDelayed(mPendingCheckForLongPress, 3000);
    }

    class CheckForLongPress implements Runnable {

        public void run() {
            if (mCurtStatus == STATUS_PRESSED) {
                if (performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }
    }

    private EditDrawer mEditDrawer;
    class EditDrawer implements Runnable{

        private Rect drawRect;
        private LinkedBlockingQueue<Integer> queue;
        public EditDrawer(Rect rect, LinkedBlockingQueue<Integer> queue){
            drawRect = rect;
            this.queue = queue;
        }

        @Override
        public void run() {

            while (true) {
                int val = 0;
                try {
                    val = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drawRect.left = val;
                drawRect.right = mDrawRectNormal.right - val;
                drawRect.top = val;
                drawRect.bottom = mDrawRectNormal.bottom - val;
                Canvas canvas = lockCanvas();
                canvas.drawColor(BackgroundColor);
                canvas.save();
                canvas.clipRect(drawRect);
                canvas.drawColor(ForcegroundColor);
                canvas.drawText("locationY=" + y, drawRect.left + 0, drawRect.top + 100, textPaint);
                canvas.restore();
                if (drawRect.left == FLOAT_GAP) {
                    Bitmap settingBtn = BitmapCache.get(R.drawable.ic_drag_setting);
                    if (settingBtn == null) {
                        settingBtn = BitmapFactory.decodeResource(getResources(), R.drawable.ic_drag_setting);
                        BitmapCache.put(R.drawable.ic_drag_setting, settingBtn);
                    }
                    canvas.drawBitmap(settingBtn, 0, 0, null);

                    Bitmap delBtn = BitmapCache.get(R.drawable.ic_drag_del);
                    if (delBtn == null) {
                        delBtn = BitmapFactory.decodeResource(getResources(), R.drawable.ic_drag_del);
                        BitmapCache.put(R.drawable.ic_drag_del, delBtn);
                    }
                    canvas.drawBitmap(delBtn, mSurfaceTextureWidth - delBtn.getWidth(), 0, null);

                    int resId = R.drawable.ic_cell_level2_0;
                    if (mSizeX == 4 && mSizeY == 2) {
                        resId = R.drawable.ic_cell_level2_1;
                    } else if (mSizeX == 2 && mSizeY == 2) {
                        resId = R.drawable.ic_cell_level2_2;
                    }
                    Bitmap levelBtn = BitmapCache.get(resId);
                    if (levelBtn == null) {
                        levelBtn = BitmapFactory.decodeResource(getResources(), resId);
                        BitmapCache.put(resId, levelBtn);
                    }
                    canvas.drawBitmap(levelBtn, mSurfaceTextureWidth - levelBtn.getWidth(), mSurfaceTextureHeight - levelBtn.getHeight(), null);

                    unlockCanvasAndPost(canvas);
                    mEditDrawer = null;
                    break;
                }
                unlockCanvasAndPost(canvas);
            }

        }
    }

    private FloatingDrawer mFloatingDrawer;
    class FloatingDrawer implements Runnable{

        @Override
        public void run() {
            Rect drawRect = new Rect(mDrawRectFloat);
            FloatingEffect fe = new FloatingEffect(mDrawRectNormal, drawRect);
            fe.startFloating();
            while (mCurtStatus == STATUS_FLOAT){
                Canvas canvas = lockCanvas();
                //Log.d("FloatingDrawer", "x="+x+",y="+y);
                if (canvas == null)
                    break;
                canvas.drawColor(BackgroundColor);
                canvas.save();
                drawRect = fe.nextPosition();
                //Log.d("mDrawRectFloat", "l="+mDrawRectFloat.left+",r="+mDrawRectFloat.right+
                //",t="+mDrawRectFloat.top+",b="+mDrawRectFloat.bottom);
                canvas.clipRect(drawRect);
                canvas.drawColor(ForcegroundColor);
                canvas.drawText("locationY=" + y, drawRect.left + 0, drawRect.top + 100, textPaint);
                canvas.restore();
                unlockCanvasAndPost(canvas);
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
