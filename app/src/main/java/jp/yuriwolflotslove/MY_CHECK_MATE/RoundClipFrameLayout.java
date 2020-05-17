package jp.yuriwolflotslove.MY_CHECK_MATE;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class RoundClipFrameLayout extends FrameLayout {
    //viewの成型をやり直す
    // 親形状
    private final Path mPath = new Path();
    // 子形状　四角形
    private final RectF mRect = new RectF();
    // 子形状　角丸
    private int mCornerRadius;

    public RoundClipFrameLayout(Context context) {
        this(context, null);
    }

    public RoundClipFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    //コンストラクタ（メイン）
    public RoundClipFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // RoundClipFrameLayoutを配置する際の全属性・・・ attrs.xmlで定義
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundClipFrameLayout, defStyleAttr, 0);
        mCornerRadius = ta.getDimensionPixelSize(R.styleable.RoundClipFrameLayout_cornerRadius, 0);

        ta.recycle();
    }

    public void setCornerRadius(int radiusPx) {
        if (mCornerRadius != radiusPx) {
            mCornerRadius = radiusPx;
            rebuildPath();
            invalidate();
        }
    }

    private void rebuildPath() {
        mPath.reset();
        mPath.addRoundRect(mRect, mCornerRadius, mCornerRadius, Path.Direction.CW);
        mPath.close();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        mRect.set(0, 0, width, height);
        rebuildPath();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(mPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }
}
