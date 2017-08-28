package xh.xshare;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * Created by G1494458 on 2017/8/28.
 */

public class LoadingLayout extends FrameLayout {
    private static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;

    private static final String TAG = "LoadingLayout";

    View loadingView;

    public LoadingLayout(@NonNull Context context) {
        super(context);
    }

    public LoadingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LoadingLayout,
                0, 0
        );

        try {
            int loadingViewID = a.getResourceId(R.styleable.LoadingLayout_loadingView, -1);
            if (loadingViewID == -1) {
                loadingView = new ProgressBar(context);
            } else {
                loadingView = LayoutInflater.from(context).inflate(loadingViewID, this, false);
//                if (loadingView instanceof ViewGroup) {
//                    throw new IllegalArgumentException("loadingView cannot use ViewGroup, please use View");
//                }
            }
            addView(loadingView);
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int wrapWidth = 600;
//        int wrapHeight = 600;
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(wrapWidth, wrapHeight);
//        } else if (widthMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(wrapWidth, heightSize);
//        } else if (heightMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(widthSize, wrapHeight);
//        } else {
//            setMeasuredDimension(widthSize, heightSize);
//        }
//    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        super.onLayout(changed, left, top, right, bottom);

        View v = getChildAt(0);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
        lp.gravity = Gravity.CENTER;
        v.setLayoutParams(lp);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void startLoading() {
        loadingView.setVisibility(VISIBLE);
        loadingView.setAlpha(1);
        for (int i = 1; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.setAlpha(0);
            v.setVisibility(GONE);
        }
    }

    public void endLoading() {
        loadingView.animate()
                .alpha(0)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        loadingView.setVisibility(GONE);
                        for (int i = 1; i < getChildCount(); i++) {
                            final View v = getChildAt(i);
                            v.animate()
                                    .alpha(1)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .setDuration(250)
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            v.setVisibility(VISIBLE);
                                        }
                                    })
                                    .start();
                        }
                    }
                })
                .setDuration(250)
                .start();
    }
}
