package com.xsw.radarchartview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Author   : Ershuai.Fan
 * Email    : 975068649@qq.com
 * Created at 2019/2/27
 * <The purpose or description of this file>
 * <本文件的功能或说明>
 * <p>
 * <p>
 * <p>
 * ——————————————————————————————————————————————————————————————————————
 */
public class ElectricViewAni extends View {


    //最高电压

    public static final double U = 380.00;
    //最高电流
    public static final double I = 60;

    public float ratio = 1.0f;
    //yuan拐点的数目
    private int pointCount = 12;
    //圆环数目
    private int circleCount = 5;
    private double circleRadian = Math.PI / 6;
    //数据源
    private List<DataModel> datas = new ArrayList<>();
    //偏离x的角度
    private float delRadian = 0;
    //外接圆的半径
    private int radiu;

    //中心点（x，y）
    private int centerX;
    private int centerY;
    private Paint framePaint;
    private Paint valuePaint;
    private Paint txtPaint;
    private Paint bgPaint;

    public ElectricViewAni(Context context) {
        super(context);
        // init();
    }

    public ElectricViewAni(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ElectricViewAni(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //   init();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radiu = Math.min(w, h) / 2 - 100;
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


    }

    public void setData(List<DataModel> list) {
        datas.clear();
        datas.addAll(list);
        invalidate();
    }

    public void setDataAni(List<DataModel> list) {
        datas.clear();
        datas.addAll(list);
        startAni();

    }

    ;

    void init(Context context, @Nullable AttributeSet attrs) {
        //背景
        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#E6E6E6"));
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(2);
        //骨架的画笔
        framePaint = new Paint();
        framePaint.setColor(Color.parseColor("#51C95D"));
        framePaint.setAntiAlias(true);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(2);

        //圆数字的画笔
        valuePaint = new Paint();
        valuePaint.setColor(Color.BLACK);
        valuePaint.setAntiAlias(true);
        valuePaint.setStyle(Paint.Style.STROKE);
        valuePaint.setTextSize(14);

        //文字的画笔
        txtPaint = new Paint();
        txtPaint.setColor(Color.BLACK);
        txtPaint.setAntiAlias(true);
        txtPaint.setStyle(Paint.Style.STROKE);
        txtPaint.setStrokeWidth(6);
        valuePaint.setTextSize(22);

    }


    /**
     * 绘制整体框架图
     *
     * @param canvas
     * @param path
     */
    public void drawFrame(Canvas canvas, Path path) {

        drawBg(canvas, path);

        if (datas.size() == 0) return;

        //绘制数值
        for (int i = 0; i < datas.size(); i++) {
            framePaint.setColor(Color.parseColor(datas.get(i).arrowColor));
            double curRadiu = datas.get(i).u / U * radiu*ratio;
            float curRadian = (float) (((datas.get(i).phy + 270) % 360) / 180 * Math.PI);
            float dx = centerX + (float) (curRadiu * Math.cos(curRadian));
            float dy = centerY + (float) (curRadiu * Math.sin(curRadian));
            path.moveTo(centerX, centerY);
            drawAL(canvas, framePaint, centerX, centerY, (int) dx, (int) dy);
            if (Math.sin(curRadian) <= 0) {
                canvas.drawText("U" + getText(i), dx + 30, dy + 50, valuePaint);
            } else {
                canvas.drawText("U" + getText(i), dx - 20, dy - 30, valuePaint);
            }
            //电流
            double curRadiuI = datas.get(i).i / I * radiu*ratio;
            float dxI = centerX + (float) (curRadiuI * Math.cos(curRadian));
            float dyI = centerY + (float) (curRadiuI * Math.sin(curRadian));
            path.moveTo(centerX, centerY);
            drawAL(canvas, framePaint, centerX, centerY, (int) dxI, (int) dyI);
            if (Math.sin(curRadian) <= 0) {
                canvas.drawText("I" + getText(i), dxI + 30, dyI + 50, valuePaint);
            } else {
                canvas.drawText("I" + getText(i), dxI - 20, dyI - 30, valuePaint);
            }
        }


    }


    /**
     * 绘制背景
     *
     * @param canvas
     * @param path
     */
    private void drawBg(Canvas canvas, Path path) {
        //绘制连接线
        for (int i = 0; i < pointCount; i++) {
            float curRadian = (float) (circleRadian * i + delRadian);
            float dx = centerX + (float) (radiu * Math.cos(curRadian));
            float dy = centerY + (float) (radiu * Math.sin(curRadian));
            path.moveTo(centerX, centerY);
            path.lineTo(dx, dy);
            canvas.drawPath(path, bgPaint);


            int valueText = (30 * i + 90) % 360;
            if (valueText >= 0 && valueText < 180) {
                if (valueText == 0) {
                    canvas.drawText(valueText + "", dx, dy - 20, valuePaint);

                } else {
                    canvas.drawText(valueText + "", dx + 30, dy, valuePaint);
                }
            } else if (valueText >= 180) {
                if (valueText == 180) {
                    canvas.drawText(valueText + "", dx - 20, dy + 20, valuePaint);

                } else {
                    canvas.drawText(valueText + "", dx - 60, dy, valuePaint);
                }
            }
        }


        for (int c = 0; c < circleCount; c++) {
            canvas.drawCircle(centerX, centerY, radiu * (c + 1) / circleCount, bgPaint);
        }
    }


    /**
     * 画箭头
     *
     * @param sx
     * @param sy
     * @param ex
     * @param ey
     */
    public void drawAL(Canvas canvas, Paint paint, int sx, int sy, int ex, int ey) {
        double H = 16; // 箭头高度
        double L = 5; // 底边的一半
        int x3 = 0;
        int y3 = 0;
        int x4 = 0;
        int y4 = 0;
        double awrad = Math.atan(L / H); // 箭头角度
        double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        double x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点
        double y_3 = ey - arrXY_1[1];
        double x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点
        double y_4 = ey - arrXY_2[1];
        Double X3 = new Double(x_3);
        x3 = X3.intValue();
        Double Y3 = new Double(y_3);
        y3 = Y3.intValue();
        Double X4 = new Double(x_4);
        x4 = X4.intValue();
        Double Y4 = new Double(y_4);
        y4 = Y4.intValue();
        // 画线
        canvas.drawLine(sx, sy, ex, ey, paint);
        Path triangle = new Path();
        triangle.moveTo(ex, ey);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x4, y4);
        triangle.close();
        canvas.drawPath(triangle, paint);

    }

    // 计算
    public double[] rotateVec(int px, int py, double ang, boolean isChLen, double newLen) {
        double mathstr[] = new double[2];
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathstr[0] = vx;
            mathstr[1] = vy;
        }
        return mathstr;
    }

    /**
     * 根据索引获取abc
     *
     * @param index
     */
    String getText(int index) {
        switch (index) {
            case 0:
                return "a";
            case 1:
                return "b";
            case 2:
                return "c";
        }
        return "";
    }

    private void startAni() {
        final ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
        animator.setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ratio = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    public static class DataModel {
        public double u;
        public double i;
        public double phy;
        public String arrowColor;

        public DataModel(double u, double i, double phy, String arrowColor) {
            this.u = u;
            this.i = i;
            this.phy = phy;
            this.arrowColor = arrowColor;
        }
    }
}
