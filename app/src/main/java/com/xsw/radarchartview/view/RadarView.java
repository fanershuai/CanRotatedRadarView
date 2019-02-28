package com.xsw.radarchartview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import com.xsw.radarchartview.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Author   : Ershuai.Fan
 * Email    : 975068649@qq.com
 * Create at 2018/3/1
 * Description:自定义的雷达图
 */

public class RadarView extends View {

    public static final String Tag = "Radarview";
    public static final int POSITIVE = 1;//正转
    public static final int NEGATIVE = 0;//倒转

    public int rotationFlag;
    //多边形拐点的数目
    private int pointCount = 6;

    //偏离x的角度
    private float delRadian = 0;

    //弧度
    private float radian = (float) (Math.PI * 2 / pointCount);
    //外接圆的半径
    private int radiu;
    //中心点（x，y）
    private int centerX;
    private int centerY;

    //边距
    public int align;

    //数据源
    private String[] datas = {"中国", "美利坚合众国", "日本", "韩国", "澳大利亚", "俄罗斯"};
    private float[] scores = {60, 50, 70, 80, 60, 70};
    private float[] scores2 = {80, 30, 90, 60, 90, 40};
    private int[] draws = {R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher};
    private float maxScore = 100;

    //控制等级密度
    private float levelNum;
    //蜘蛛网画笔
    private Paint linePaint;
    private Paint bgPaint;

    //等级字画笔
    private Paint textPaint;
    //标题画笔
    private Paint titlePaint;
    //图表画笔
    private Paint bitmapPaint;

    //数据画笔
    private Paint scorePaint;
    private Paint score2Paint;


    //速度跟踪
    private VelocityTracker mVelocityTracker;
    private Timer timer;

    public RadarView(Context context) {
        super(context);
        // init();
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //   init();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radiu = Math.min(w, h) / 2 - align;
        centerX = w / 2;
        centerY = h / 2;
        postInvalidate();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        Path path = new Path();

        drawFrame(canvas, path);

        drawData(canvas, path);


    }

    /**
     * 绘制整体框架图
     *
     * @param canvas
     * @param path
     */
    public void drawFrame(Canvas canvas, Path path) {
        //绘制环形
        for (int l = 1; l <= levelNum; l++) {
            path.moveTo((float) (centerX + radiu * (l / levelNum) * Math.cos(delRadian)), (float) (centerY + radiu * (l / levelNum) * Math.sin(delRadian)));

            for (int i = 0; i < datas.length; i++) {
                float curRadian = radian * i + delRadian;
                float dx = centerX + (float) (radiu * (l / levelNum) * Math.cos(curRadian));
                float dy = centerY + (float) (radiu * (l / levelNum) * Math.sin(curRadian));
                path.lineTo(dx, dy);
                //控制第三象限坐标轴有数据(canvas 绘制是顺时针所以想象限有区别)
                if (curRadian > (float) Math.PI && curRadian <= (float) Math.PI * 3 / 2) {
                    canvas.drawText(maxScore * l / levelNum + "", dx, dy, textPaint);
                }
            }
            path.close();
        }


        //绘制连接线
        for (int i = 0; i < datas.length; i++) {
            float curRadian = radian * i + delRadian;
            float dx = centerX + (float) (radiu * Math.cos(curRadian));
            float dy = centerY + (float) (radiu * Math.sin(curRadian));
            path.moveTo(centerX, centerY);
            path.lineTo(dx, dy);
        }
        canvas.drawPath(path, bgPaint);
        canvas.drawPath(path, linePaint);
        Paint.FontMetrics fm = titlePaint.getFontMetrics();
        int titleHeight = (int) (Math.ceil(fm.descent - fm.top) + 2);
        //绘制连接线的标题和图片
        for (int i = 0; i < datas.length; i++) {

            int titleWidth = (int) titlePaint.measureText(datas[i]);
            //获取图片的高度
            Bitmap bit = BitmapFactory.decodeResource(getResources(), draws[i]);
            int bitHeight = bit.getHeight();
            int bitWidth = bit.getWidth();
            int dx = centerX + (int) (radiu * Math.cos(radian * i + delRadian));
            int dy = centerY + (int) (radiu * Math.sin(radian * i + delRadian));
            float curRadian = radian * i + delRadian;
            if (curRadian >= (Math.PI * 2)) {
                curRadian = (float) (curRadian % (Math.PI * 2));
            }
            if (curRadian > (float) Math.PI / 2 + Math.PI * 0.125f && curRadian < (float) Math.PI * 3 / 2 - Math.PI * 0.125f) {
                dx = dx - (titleWidth > bitWidth ? titleWidth : bitWidth);
            } else if (curRadian < (float) Math.PI / 2 - Math.PI * 0.125f || (curRadian > (float) Math.PI * 3 / 2 + Math.PI * 0.125f)) {
                dx = dx + (titleWidth > bitWidth ? titleWidth / 2 : bitWidth / 2);
            }

            if (curRadian >= Math.PI * 2 - Math.PI * 0.125f || curRadian == 0 || (curRadian >= Math.PI && curRadian <= Math.PI + Math.PI * 0.125f)) {
                dy = dy + (titleHeight + bitHeight) / 2;
            }
            if ((curRadian <= Math.PI * 0.125f && curRadian > 0) || (curRadian <= Math.PI && curRadian >= Math.PI - Math.PI * 0.125f)) {
                dy = dy - (titleHeight + bitHeight) / 2;
            }
            if (curRadian > 0 && curRadian < (float) Math.PI) {

                dy = dy + titleHeight + bitHeight;

            }


            canvas.drawText(datas[i], dx, dy, titlePaint);
            //绘制图片

            dy = dy - bitHeight - titleHeight;
            dx = dx - bitWidth / 2 + titleWidth / 2;

            canvas.drawBitmap(bit, dx, dy, bitmapPaint);


        }
    }

    /**
     * 绘制数据
     *
     * @param canvas
     */
    private void drawData(Canvas canvas, Path path) {


        //绘制分数数据
        path.reset();
        for (int i = 0; i < scores.length; i++) {
            if (i == 0)
                path.moveTo((float) (centerX + radiu * scores[i] / maxScore * Math.cos(delRadian)), (float) (centerY + radiu * scores[i] / maxScore * Math.sin(delRadian)));
            float dx = centerX + (float) (radiu * scores[i] / maxScore * Math.cos(radian * i + delRadian));
            float dy = centerY + (float) (radiu * scores[i] / maxScore * Math.sin(radian * i + delRadian));
            path.lineTo(dx, dy);
        }
        path.close();
        canvas.drawPath(path, scorePaint);

        path.reset();
        for (int i = 0; i < scores2.length; i++) {

            if (i == 0) {
                path.moveTo((float) (centerX + radiu * scores2[i] / maxScore * Math.cos(delRadian)), (float) (centerY + radiu * scores2[i] / maxScore * Math.sin(delRadian)));
            }
            float dx = centerX + (float) (radiu * scores2[i] / maxScore * Math.cos(radian * i + delRadian));
            float dy = centerY + (float) (radiu * scores2[i] / maxScore * Math.sin(radian * i + delRadian));
            path.lineTo(dx, dy);
        }
        path.close();
        canvas.drawPath(path, score2Paint);


    }


    @Override
    protected void onDetachedFromWindow() {
        if (mVelocityTracker != null)
            mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }


    int lastX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return mGestureDetector.onTouchEvent(event);
    }

    private GestureDetector mGestureDetector;

    private void init(Context context, AttributeSet attrs) {
        mGestureDetector = new GestureDetector(context, new MyGestureListener());
        float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
        mPhysicalCoeff = SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.37f // inch/meter
                * ppi
                * 0.84f;

        timer = new Timer();


        levelNum = 10;

        linePaint = new Paint();
        linePaint.setColor(Color.BLUE);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);

        bgPaint = new Paint();
        bgPaint.setColor(Color.BLUE);
        bgPaint.setAntiAlias(true);
        bgPaint.setAlpha(128);
        bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextSize(16);

        titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setStyle(Paint.Style.STROKE);
        titlePaint.setTextSize(20);

        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);

        scorePaint = new Paint();
        scorePaint.setColor(Color.GREEN);
        scorePaint.setAlpha(128);
        scorePaint.setAntiAlias(true);
        scorePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        score2Paint = new Paint();
        score2Paint.setColor(Color.YELLOW);
        score2Paint.setAlpha(128);
        score2Paint.setAntiAlias(true);
        score2Paint.setStyle(Paint.Style.FILL_AND_STROKE);


        //控制边界范围
        //最长的标题长度
        int maxTitleLenth = 0;
        for (int i = 0; i < datas.length; i++) {
            if (titlePaint.measureText(datas[i]) > maxTitleLenth) {
                maxTitleLenth = (int) titlePaint.measureText(datas[i]);
            }
        }
        align = 30 + maxTitleLenth;


    }


    class MyGestureListener implements GestureDetector.OnGestureListener { //共有6个方法：

        // 用户轻触触屏：Touch down(仅一次)时触发, e为down时的MotionEvent:
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }


        // 用户（轻触触屏后）松开：Touch up(仅一次)时触发，e为up时的MotionEvent:
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        // 用户轻触触屏，并拖动：
        //   按下并滑动时触发，e1为down(仅一次)时的MotionEvent，e2为move(多个)时的MotionEvent:
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float xDown = e1.getX();
            float yDown = e1.getY();
            float xMove = e2.getX();
            float yMove = e2.getY();

            Log.i("sroll", "distance:" + distanceX + ",distanceY：" + distanceY);


            //判断(防止倒转时的弧度小于0造成象限判断不正确)
            if (delRadian < (Math.PI * 2)) {
                delRadian += (Math.PI * 2);
            }


            //计算旋转角度，根据线速度角速度的关系
            float radianMoved = (float) (Math.sqrt(distanceX * distanceX + distanceY * distanceY) / (radiu));
            //判断象限
            //I象限,II ,III,IV
            if (yMove >= centerY && xMove >= centerX) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    if (distanceX > 0) {
                        rotationFlag = POSITIVE;
                    } else {
                        rotationFlag = NEGATIVE;
                    }
                } else {
                    if (distanceY < 0) {
                        rotationFlag = POSITIVE;

                    } else {
                        rotationFlag = NEGATIVE;
                    }
                }
            } else if (yMove >= centerY && xMove <= centerX) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    if (distanceX > 0) {
                        rotationFlag = POSITIVE;

                    } else {
                        rotationFlag = NEGATIVE;
                    }
                } else {
                    if (distanceY > 0) {
                        rotationFlag = POSITIVE;

                    } else {
                        rotationFlag = NEGATIVE;
                    }
                }
            } else if (yMove <= centerY && xMove <= centerX) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    if (distanceX < 0) {
                        rotationFlag = POSITIVE;

                    } else {
                        rotationFlag = NEGATIVE;
                    }
                } else {
                    if (distanceY > 0) {
                        rotationFlag = POSITIVE;

                    } else {
                        rotationFlag = NEGATIVE;
                    }
                }
            } else if (yMove <= centerY && xMove >= centerX) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    if (distanceX < 0) {
                        rotationFlag = POSITIVE;
                    } else {
                        rotationFlag = NEGATIVE;
                    }
                } else {
                    if (distanceY < 0) {
                        rotationFlag = POSITIVE;

                    } else {
                        rotationFlag = NEGATIVE;
                    }
                }
            }

            if (rotationFlag == POSITIVE) {
                delRadian = (float) (delRadian % (Math.PI * 2) + radianMoved);
            } else if (rotationFlag == NEGATIVE) {
                delRadian = (float) (delRadian % (Math.PI * 2) - radianMoved);
            }

            invalidate();

            return true;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }


        // 用户按下触屏、快速移动后松开：
        //   按下并快速滑动一小段距离（多个move），up时触发，e1为down(仅一次)时的MotionEvent，
        //   e2为up(仅一次)时的MotionEvent:
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
           Log.i("sroll", "velocityX:" + velocityX);
            double v = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            double dis = getSplineFlingDistance((int) v);
            int duration = getSplineFlingDuration((int) v);
            Log.i("sroll", "distance:" + dis + ",duration：" + duration);
            if (v > 8000)
                flingRolate(duration, dis, v);

            return true;
        }
    }

    //滑动参数
    private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
    private static float mPhysicalCoeff;
    private static float mFlingFriction = ViewConfiguration.getScrollFriction();
    private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)

    private double getSplineDeceleration(int velocity) {
        return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
    }

    private static double getSplineDecelerationByDistance(double distance) {
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return decelMinusOne * (Math.log(distance / (mFlingFriction * mPhysicalCoeff))) / DECELERATION_RATE;
    }

    //通过初始速度获取最终滑动距离
    private double getSplineFlingDistance(int velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }

    //获取滑动的时间
    /* Returns the duration, expressed in milliseconds */
    private int getSplineFlingDuration(int velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return (int) (1000.0 * Math.exp(l / decelMinusOne));
    }

    //获取滑动的实时速度
    private double getFlingVelocity(int t) {
        double l = 2 * Math.log(t * 0.001);
        return Math.exp(l) * ((mFlingFriction * mPhysicalCoeff) / DECELERATION_RATE) / INFLEXION;
    }

    //通过需要滑动的距离获取初始速度
    public static int getVelocityByDistance(double distance) {
        final double l = getSplineDecelerationByDistance(distance);
        int velocity = (int) (Math.exp(l) * mFlingFriction * mPhysicalCoeff / INFLEXION);
        return Math.abs(velocity);
    }


    public boolean stop = false;

    //惯性旋转
    public void flingRolate(final int duration, final double distance, double velocity) {
        final int[] spacingTime = {8};
        final int[] t = {duration};
        stop = false;
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (!stop) {
                    spacingTime[0] = spacingTime[0] + 2;
                    t[0] = t[0] - spacingTime[0];
                    double v = getFlingVelocity(t[0] - spacingTime[0]);
                    double movedDistance = distance - getSplineFlingDistance((int) v);

                    //判断(防止倒转时的弧度小于0造成象限判断不正确)
                    if (delRadian < (Math.PI * 2)) {
                        delRadian += (Math.PI * 2);
                    }
                    if (rotationFlag == POSITIVE) {
                        delRadian = (float) (delRadian % (Math.PI * 2) + movedDistance / radiu);
                    } else if (rotationFlag == NEGATIVE) {
                        //判断(防止倒转时的弧度小于0造成象限判断不正确)
                        delRadian = (float) ((float) (delRadian  - (movedDistance / radiu) % (Math.PI * 2))%(Math.PI * 2));
                      //  Log.i("sroll", "delRadian:" + delRadian);
                    }

                  //  Log.i("sroll", " duration：" + t[0] + "------radian:" + delRadian + "------space:" + spacingTime[0]);
                    postInvalidate();
                    if (t[0] <= 0) {
                        stop = true;
                        timer.cancel();
                    }
                }


            }
        }, 0, spacingTime[0]);


    }





}
