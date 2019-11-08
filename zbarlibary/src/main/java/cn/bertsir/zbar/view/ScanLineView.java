package cn.bertsir.zbar.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import cn.bertsir.zbar.R;

/**
 * 二维码扫描模式:
 * 1.网格 扫描模式
 * 2.雷达 扫描模式
 * 3.网格+雷达 扫描模式
 * 4.线 扫描模式
 *
 * Created by Bert on 2019-09-16.
 * Mail: bertsir@163.com
 *
 * @version 1.0.1 修改过属性
 */
public class ScanLineView extends View {

    public static final int STYLE_GRIDDING       = 0b001;//网格
    public static final int STYLE_RADAR          = 0b010;//雷达
    public static final int STYLE_GRIDDING_RADAR = STYLE_GRIDDING | STYLE_RADAR;//0b011网格 + 雷达
    public static final int STYLE_LINE           = 0b100;//线

    private Rect mFrame;//最佳扫描区域的Rect

    private Paint paintGridding;//网格样式画笔
    private Paint paintRadar;//雷达样式画笔
    private Paint paintLine;//线条样式画笔

    private Path pathBorder;//边框path
    private Path pathGridding;//网格样式的path, 画网格

    private LinearGradient linearGradientRadar;   //雷达 样式的画笔shader
    private LinearGradient linearGradientGridding;//网格 画笔的shader
    private LinearGradient linearGradientLine;    //线   画笔的shader

    private float griddingLineWidth = 2;//网格线的线宽，单位pix
    private int   griddingDensity   = 40;//网格密度, 每一行/列 分成多少个小格子. (样式是网格才有效)


    private float mCornerLineLen = 50f;//根据比例计算的边框长度，从四角定点向临近的定点画出的长度

    private Matrix        matrixScan;//变换矩阵，用来实现动画效果
    private ValueAnimator valueAnimator;//值动画，用来变换矩阵操作

    private int scanAnimatorDuration = 1800;//值动画的时长, 单位ms

    private int   scanStyle = STYLE_GRIDDING;//扫描样式, 默认网格
    private int scanColor;//扫描颜色, 默认: colorAccent
    private float animatedValue;

    public ScanLineView(Context context) {
        super(context);
        init(context, null);
    }

    public ScanLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScanLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScanLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        //变换矩阵，用来处理扫描的上下扫描效果
        matrixScan = new Matrix();
        matrixScan.setTranslate(0, 30);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScanLineView);
            //扫描样式, 默认网格
            scanStyle = a.getInt(R.styleable.ScanLineView_slvScanStyle, STYLE_GRIDDING);
            //扫描颜色, 默认: colorAccent
            scanColor = a.getColor(R.styleable.ScanLineView_slvScanColor, getResources().getColor(R.color.colorAccent));
            //扫描时长
            scanAnimatorDuration = a.getInt(R.styleable.ScanLineView_slvScanDurationMs, scanAnimatorDuration);
            if ((scanStyle & STYLE_GRIDDING) != 0) {
                //网格密度, 每一行/列 分成多少个小格子. (样式是网格才有效)
                griddingDensity = a.getInt(R.styleable.ScanLineView_slvGriddingDensity, griddingDensity);
            }
            a.recycle();

            switch (scanStyle) {
                case STYLE_GRIDDING://网格
                    // Initialize these once for performance rather than calling them every time in onDraw().
                    paintGridding = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paintGridding.setStyle(Paint.Style.STROKE);
                    paintGridding.setStrokeWidth(griddingLineWidth);
                    break;
                case STYLE_RADAR://雷达
                    paintRadar = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paintRadar.setStyle(Paint.Style.FILL);
                    break;
                case STYLE_GRIDDING_RADAR://网格 + 雷达
                    paintGridding = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paintGridding.setStyle(Paint.Style.STROKE);
                    paintGridding.setStrokeWidth(griddingLineWidth);

                    paintRadar = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paintRadar.setStyle(Paint.Style.FILL);
                    break;
                case STYLE_LINE://线
                    paintLine =new Paint();//创建一个画笔
                    paintLine.setStyle(Paint.Style.FILL);//设置非填充
                    paintLine.setStrokeWidth(10);//笔宽5像素
                    paintLine.setAntiAlias(true);//锯齿不显示
                    break;
            }
        } else {
            scanColor = getResources().getColor(R.color.colorAccent);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mFrame = new Rect(left, top, right, bottom);

        initBoundaryAndAnimator();

        if ((scanStyle & STYLE_GRIDDING) != 0) initGriddingPathAndStyle();
        if ((scanStyle & STYLE_RADAR) != 0) initRadarStyle();
        if ((scanStyle & STYLE_LINE) != 0) initLineStyle();

        //初始化&开始动画
        initScanValueAnim(mFrame.height());
    }

    //初始化 尺寸 和 动画
    private void initBoundaryAndAnimator() {
        if (pathBorder == null) {
            pathBorder = new Path();
            pathBorder.moveTo(mFrame.left, mFrame.top + mCornerLineLen);//. 移动到左下角
            pathBorder.lineTo(mFrame.left, mFrame.top);//│ 左侧
            pathBorder.lineTo(mFrame.left + mCornerLineLen, mFrame.top);//─ 顶部
            pathBorder.moveTo(mFrame.right - mCornerLineLen, mFrame.top);//
            pathBorder.lineTo(mFrame.right, mFrame.top);
            pathBorder.lineTo(mFrame.right, mFrame.top + mCornerLineLen);
            pathBorder.moveTo(mFrame.right, mFrame.bottom - mCornerLineLen);
            pathBorder.lineTo(mFrame.right, mFrame.bottom);
            pathBorder.lineTo(mFrame.right - mCornerLineLen, mFrame.bottom);
            pathBorder.moveTo(mFrame.left + mCornerLineLen, mFrame.bottom);
            pathBorder.lineTo(mFrame.left, mFrame.bottom);
            pathBorder.lineTo(mFrame.left, mFrame.bottom - mCornerLineLen);
        }
    }

    //初始化动画
    private void initScanValueAnim(int height) {
        if (valueAnimator == null) {
            valueAnimator = new ValueAnimator();
            valueAnimator.setDuration(scanAnimatorDuration);
            valueAnimator.setFloatValues(-height, 0);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.setRepeatCount(Animation.INFINITE);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (matrixScan != null ) {
                        animatedValue = (float) animation.getAnimatedValue();
                        matrixScan.setTranslate(0, animatedValue);
                        if ((scanStyle & STYLE_GRIDDING) != 0) linearGradientGridding.setLocalMatrix(matrixScan);
                        if ((scanStyle & STYLE_RADAR) != 0) linearGradientRadar.setLocalMatrix(matrixScan);
                        if ((scanStyle & STYLE_LINE) != 0) linearGradientLine.setLocalMatrix(matrixScan);
                        //mScanPaint.setShader(mLinearGradient); //不是必须的设置到shader即可
                        invalidate();
                    }
                }
            });
            valueAnimator.start();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mFrame == null || pathBorder ==null) {
            return;
        }
        switch (scanStyle) {
            case STYLE_GRIDDING://网格
                canvas.drawPath(pathGridding, paintGridding);
                break;
            case STYLE_RADAR://雷达
                canvas.drawRect(mFrame, paintRadar);
                break;
            case STYLE_GRIDDING_RADAR://网格 + 雷达
                canvas.drawPath(pathGridding, paintGridding);
                canvas.drawRect(mFrame, paintRadar);
                break;
            case STYLE_LINE://线
                canvas.drawLine(0,(mFrame.height()- Math.abs(animatedValue)),getMeasuredWidth(),
                        (mFrame.height()- Math.abs(animatedValue)), paintLine);
                break;
        }
    }

    //初始化网格path 和 渐变
    private void initGriddingPathAndStyle() {
        if (pathGridding == null) {
            pathGridding = new Path();
            float wUnit = mFrame.width() / (griddingDensity + 0f);//每一格宽度
            float hUnit = mFrame.height() / (griddingDensity + 0f);//每一格高度
            for (int i = 0; i <= griddingDensity; i++) {//画竖线
                pathGridding.moveTo(mFrame.left + i * wUnit, mFrame.top);
                pathGridding.lineTo(mFrame.left + i * wUnit, mFrame.bottom);
            }
            for (int i = 0; i <= griddingDensity; i++) {//画横线
                pathGridding.moveTo(mFrame.left, mFrame.top + i * hUnit);
                pathGridding.lineTo(mFrame.right, mFrame.top + i * hUnit);
            }
        }
        if (linearGradientGridding == null) {
            linearGradientGridding = new LinearGradient(0, mFrame.top, 0, mFrame.bottom + 0.01f * mFrame.height(), new int[]{Color.TRANSPARENT, Color.TRANSPARENT, scanColor, Color.TRANSPARENT}, new float[]{0, 0.5f, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);
            linearGradientGridding.setLocalMatrix(matrixScan);
            paintGridding.setShader(linearGradientGridding);
        }
    }

    //初始化雷达渐变
    private void initRadarStyle() {
        if (linearGradientRadar == null) {
            linearGradientRadar = new LinearGradient(0, mFrame.top, 0, mFrame.bottom + 0.01f * mFrame.height(),
                    new int[]{Color.TRANSPARENT, Color.TRANSPARENT, scanColor, Color.TRANSPARENT},
                    new float[]{0, 0.85f, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);
            linearGradientRadar.setLocalMatrix(matrixScan);
            paintRadar.setShader(linearGradientRadar);
        }
    }

    //初始化线渐变
    private void initLineStyle() {
        if (linearGradientLine == null) {
            String line_colors = String.valueOf(Integer.toHexString(scanColor));
            line_colors = line_colors.substring(line_colors.length() - 6, line_colors.length() - 0);
            linearGradientLine = new LinearGradient(0, 0, getMeasuredWidth(), 0,
                    new int[] {Color.parseColor("#00"+line_colors), scanColor, Color.parseColor("#00"+line_colors)},
                    null, Shader.TileMode.CLAMP);
            linearGradientLine.setLocalMatrix(matrixScan);
            paintLine.setShader(linearGradientLine);
        }
    }

    /**
     * @param colorValue 设定扫描的颜色
     */
    public void setScanColor(int colorValue) {
        this.scanColor = colorValue;
    }

    /**
     * @param duration 设置扫描时间
     */
    public void setScanDuration(@IntRange(from = 0) int duration) {
        this.scanAnimatorDuration = duration;
    }

    /**
     * 扫描区域网格的样式
     * @param strokeWidh 网格的线宽
     * @param density 网格的密度
     */
    public void setScanGriddingStyle(float strokeWidh, int density) {
        this.griddingLineWidth = strokeWidh;
        this.griddingDensity = density;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }
}

