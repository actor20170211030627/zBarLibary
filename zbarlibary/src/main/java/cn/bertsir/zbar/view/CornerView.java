package cn.bertsir.zbar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import cn.bertsir.zbar.R;

/**
 * 扫描二维码的4个角
 * Created by Bert on 2017/9/22.
 *
 * @version 1.0.1 修改过属性名称
 */
public class CornerView extends View {

    private Paint  paint;//声明画笔
    private              int    width  = 0;
    private              int    height = 0;

    private static final int LEFT_TOP = 0;
    private static final int LEFT_BOTTOM = 1;
    private static final int RIGHT_TOP = 2;
    private static final int RIGHT_BOTTOM = 3;

    private int cornerColor;//角的颜色
    private int cornerWidth = 10;//角的厚度
    private int cornerGravity = LEFT_TOP;

    public CornerView(Context context) {
        super(context);
        init(context, null);
    }

    public CornerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CornerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CornerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CornerView);
            cornerColor = a.getColor(R.styleable.CornerView_cvCornerColor, getResources().getColor(R.color.colorAccent));
            cornerWidth = a.getDimensionPixelSize(R.styleable.CornerView_cvCornerWidth, cornerWidth);//角的厚度
            cornerGravity = a.getInt(R.styleable.CornerView_cvCornerGravity, LEFT_TOP);//默认左上角
            a.recycle();
        } else {
            cornerColor = getResources().getColor(R.color.colorAccent);//默认 colorAccent
        }
        paint=new Paint();//创建一个画笔
        paint.setStyle(Paint.Style.FILL);//设置非填充
        paint.setStrokeWidth(cornerWidth);//笔宽
        paint.setColor(cornerColor);//设置为红笔
        paint.setAntiAlias(true);//锯齿不显示
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (cornerGravity){
            case LEFT_TOP://┏
                canvas.drawLine(0, 0, width, 0, paint);
                canvas.drawLine(0, 0, 0, height, paint);
                break;
            case LEFT_BOTTOM://┗
                canvas.drawLine(0, 0, 0, height, paint);
                canvas.drawLine(0, height, width, height, paint);
                break;
            case RIGHT_TOP://┓
                canvas.drawLine(0, 0, width, 0, paint);
                canvas.drawLine(width, 0, width, height, paint);
                break;
            case RIGHT_BOTTOM://┛
                canvas.drawLine(width, 0, width, height, paint);
                canvas.drawLine(0, height, width, height, paint);
                break;
        }
    }

    /**
     * 设置角的颜色
     */
    public void setColor(@ColorInt int color) {
        cornerColor = color;
        paint.setColor(cornerColor);
        invalidate();
    }

    /**
     * 设置角的宽度, 单位px
     */
    public void setWidth(@Px int px){
        cornerWidth = px;
        paint.setStrokeWidth(cornerWidth);
        invalidate();
    }
}
