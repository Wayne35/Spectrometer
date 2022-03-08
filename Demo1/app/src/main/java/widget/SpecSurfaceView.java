package widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class SpecSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    //SurfaceHolder
    private SurfaceHolder mHolder;
    //用于绘制的Canvas
    private Canvas mCanvas;
    //子线程标志位
    private boolean mIsDrawing;

    /**
     * 构造方法
     *
     * @param context
     * @param attrs
     */
    public SpecSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mIsDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        while (mIsDrawing) {
            draw();
            //在这里不断绘制要绘制的内容
        }
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            //这里要不断地用mCanvas方法绘制
        } catch (Exception e) {

        } finally {
            if (mCanvas != null) {
                //提交
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }
}
