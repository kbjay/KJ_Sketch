package com.example.kb_jay.kj_sketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

public class SketchView extends View {
    private Paint mPaint;
    /**
     * 分的层数
     */
    private int mTotalCount = 16;
    private int mCount = 0;
    /**
     * 接收分层0，1数组的队列
     */
    LinkedBlockingDeque<int[][]> mQueue = new LinkedBlockingDeque<>(mTotalCount);

    private int mWidth;
    private int mHeight;
    /**
     * 接收第一层数据
     */
    private boolean mIsFirstArray = true;
    /**
     * 当前绘制层的0，1数组
     */
    private int[][] mCurrentArray;
    /**
     * 在该bitmap上绘制
     */
    private Bitmap mTmpBm;
    private Canvas mTmpCanvas;
    /**
     * 4个mask
     */
    private int[][] mHorArray;
    private int[][] mVerArray;
    private int[][] mAngel45Array;
    private int[][] mAngleRevert45Array;
    private boolean mIsStop;

    /**
     * 跟4个mask对应
     */
    private int mDrawType;
    /**
     * 每个像素点绘制的次数（通过该二维数组来获取该点的色值）
     */
    private int[][] mDrawCountArray;

    public SketchView(Context context) {
        super(context);
        init();
    }

    public SketchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mTmpBm != null) {
            canvas.drawBitmap(mTmpBm, 0, 0, mPaint);
        }
    }

    /**
     * 通过该方法添加图像分层数据
     *
     * @param array
     */
    public void addArray(int[][] array) {
        mQueue.offer(array);

        if (mIsFirstArray) {
            mIsStop = false;
            mIsFirstArray = false;
            mWidth = array[0].length;
            mHeight = array.length;

            initMasks();
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.width = mWidth;
            lp.height = mHeight;
            setLayoutParams(lp);

            mDrawCountArray = new int[mHeight][mWidth];

            mTmpBm = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mTmpCanvas = new Canvas(mTmpBm);
            mPaint.setColor(Color.argb(255, 255, 255, 255));

            mTmpCanvas.drawRect(0, 0, mWidth, mHeight, mPaint);
            postInvalidate();
            startDraw();
        }
    }

    /**
     * 开始绘制
     */
    public void startDraw() {
        new Thread() {
            @Override
            public void run() {
                long start = 0L;
                while ((mCurrentArray = mQueue.poll()) != null) {
                    if (mCount == 0) {
                        start = System.currentTimeMillis();
                    }
                    long startTime = System.currentTimeMillis();
                    Log.d("kb_jay", "开始绘制！！" + mCount + " " + startTime);
                    drawArray();
                    Log.d("kb_jay", "结束绘制！！" + mCount + " " + (System.currentTimeMillis() - startTime));

//                    //刷新mask
//                    if (mCount == mTotalCount / 2) {
//                        Log.d("kb_jay", "开始刷新mask！！" + System.currentTimeMillis());
//                        initMasks();
//                        Log.d("kb_jay", "结束刷新mask！！" + System.currentTimeMillis());
//                    }
                    mCount++;
                    if (mCount == mTotalCount) {
                        Log.d("kb_jay", "绘制结束" + (System.currentTimeMillis() - start));
                        break;
                    }
                }
            }
        }.start();
    }

    /**
     * 绘制一层数据
     */
    private void drawArray() {
        //获取一个随机点
        Random random = new Random();
        Point randomPoint = new Point(random.nextInt(mWidth), random.nextInt(mHeight));
        Point prePoint = randomPoint;
        //先画右下角
        Log.d("kb_jay", "开始绘制右下角");
        while (randomPoint.x >= 0 && randomPoint.y > 0 && randomPoint.x < mWidth && randomPoint.y < mHeight && mCurrentArray != null) {
            int y = randomPoint.y;
            int x = randomPoint.x;
            for (int i = randomPoint.x - 1; i >= 0; i--) {
                if (mCurrentArray != null && mCurrentArray[y][i] == 1) {
                    ArrayList<Point> points = getPoints(new Point(i, y));
                    drawPoints(points);
                }
            }

            for (int i = randomPoint.x; i < mWidth; i++) {
                if (mCurrentArray != null && mCurrentArray[y][i] == 1) {
                    ArrayList<Point> points = getPoints(new Point(i, y));
                    drawPoints(points);
                }
            }

            for (int i = randomPoint.y - 1; i >= 0; i--) {
                if (mCurrentArray != null && mCurrentArray[i][x] == 1) {
                    ArrayList<Point> points = getPoints(new Point(x, i));
                    drawPoints(points);
                }
            }
            for (int i = randomPoint.y; i < mHeight; i++) {
                if (mCurrentArray != null && mCurrentArray[i][x] == 1) {
                    ArrayList<Point> points = getPoints(new Point(x, i));
                    drawPoints(points);
                }
            }
            randomPoint = new Point(randomPoint.x + 1, randomPoint.y + 1);
        }
        //再画左上角
        Log.d("kb_jay", "开始绘制左上角");
        randomPoint = prePoint;
        while (randomPoint.x >= 0 && randomPoint.y > 0 && randomPoint.x < mWidth && randomPoint.y < mHeight && mCurrentArray != null) {
            int y = randomPoint.y;
            int x = randomPoint.x;
            for (int i = randomPoint.x - 1; i >= 0; i--) {
                if (mCurrentArray != null && mCurrentArray[y][i] == 1) {
                    ArrayList<Point> points = getPoints(new Point(i, y));
                    drawPoints(points);
                }
            }

            for (int i = randomPoint.x; i < mWidth; i++) {
                if (mCurrentArray != null && mCurrentArray[y][i] == 1) {
                    ArrayList<Point> points = getPoints(new Point(i, y));
                    drawPoints(points);
                }
            }

            for (int i = randomPoint.y - 1; i >= 0; i--) {
                if (mCurrentArray != null && mCurrentArray[i][x] == 1) {
                    ArrayList<Point> points = getPoints(new Point(x, i));
                    drawPoints(points);
                }
            }
            for (int i = randomPoint.y; i < mHeight; i++) {
                if (mCurrentArray != null && mCurrentArray[i][x] == 1) {
                    ArrayList<Point> points = getPoints(new Point(x, i));
                    drawPoints(points);
                }
            }
            randomPoint = new Point(randomPoint.x - 1, randomPoint.y - 1);
        }
    }

    /**
     * 绘制同一个区域中的points
     *
     * @param points
     */
    private void drawPoints(ArrayList<Point> points) {
        //如果区域中的点过多，一次绘制10000个
        long start = System.currentTimeMillis();
        int mOneTimeDrawCount = 10000;

        //设置每次的绘制方向
        Random random = new Random();
        mDrawType = random.nextInt(4);

        for (int i = 0; i < points.size(); i++) {
            //将点绘制到tempBitmap上
            drawPoint(points.get(i), mDrawType);
            if (i != 0 && i % mOneTimeDrawCount == 0) {
                //10000个点之后重新设置一次绘制方向
                mDrawType = random.nextInt(4);
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                //将tmpBitmap绘制到该view上
                long holderStart = System.currentTimeMillis();
                postInvalidate();
            }
        }

        long holderStart = System.currentTimeMillis();

        postInvalidate();
    }

    /**
     * 根据绘制方向绘制点
     *
     * @param point
     * @param type
     */
    private void drawPoint(Point point, int type) {
        if (mIsStop) {
            return;
        }
        mDrawCountArray[point.y][point.x]++;
        int gray = 255 - (256 / mTotalCount / 2) * (mDrawCountArray[point.y][point.x] * 2 - 1);
        mPaint.setColor(Color.argb(255, gray, gray, gray));

        int shouldDraw;
        int l = point.y;
        int k = point.x;
        if (type == 0) {
            shouldDraw = mHorArray[l][k];
        } else if (type == 1) {
            shouldDraw = mVerArray[l][k];
        } else if (type == 2) {
            shouldDraw = mAngel45Array[l][k];
        } else {
            shouldDraw = mAngleRevert45Array[l][k];
        }
        if (shouldDraw == 1) {
            mTmpCanvas.drawPoint(point.x, point.y, mPaint);
        }
    }

    /**
     * 初始化4个mask
     */
    private void initMasks() {

        int itemWidth = 500;
        int itemHeight = mHeight;
        //原始的bitmap（最小单元）
        Bitmap bitmap = Bitmap.createBitmap(itemWidth, itemHeight, Bitmap.Config.ARGB_8888);
        Canvas ca = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.argb(255, 1, 1, 1));

        int temp = 13;
        float[] points = new float[itemHeight * 4];

        Random random = new Random();
        int count = 0;
        for (int i = 0; i < itemHeight / temp; i++) {
            points[count] = 0;
            points[++count] = temp * i;
            points[++count] = itemWidth;
            points[++count] = temp * i;
            count++;
        }
        paint.setStrokeWidth(4);
        for (int i = 0; i < points.length; i++) {
            if (i % 2 == 1) {
                points[i] = points[i] + random.nextInt(8) - 4;
            }
        }

        ca.drawLines(points, paint);
        for (int i = 0; i < points.length; i++) {
            if (i % 2 == 1) {
                points[i] = points[i] + random.nextInt(8) - 4;
            }
        }
        ca.drawLines(points, 2, itemHeight / temp * 4 - 2, paint);

        //设置水平mask
        mHorArray = new int[mHeight][mWidth];

        Bitmap maskBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.MIRROR, Shader.TileMode.REPEAT);
        Canvas canvas = new Canvas(maskBitmap);
        Paint paint1 = new Paint();
        paint1.setShader(bitmapShader);
        canvas.drawRect(0, 0, mWidth, mHeight, paint1);

        int[] pxs = new int[mWidth * mHeight];
        maskBitmap.getPixels(pxs, 0, mWidth, 0, 0, mWidth, mHeight);
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;
                mHorArray[i][j] = Color.red(pxs[index]);
            }
        }

        //设置垂直mask
        mVerArray = new int[mHeight][mWidth];

        Bitmap tempBitmap = Bitmap.createBitmap(mHeight + mWidth, mHeight + mWidth, Bitmap.Config.ARGB_8888);

        Canvas canvasVer = new Canvas(tempBitmap);

        canvasVer.rotate(90, (mWidth + mHeight) / 2, (mWidth + mHeight) / 2);

        canvasVer.drawRect(0, 0, mHeight + mWidth, mHeight + mWidth, paint1);

        Bitmap maskBitmapVer = Bitmap.createBitmap(tempBitmap, 0, 0, mWidth, mHeight);

        int[] pxsVer = new int[mWidth * mHeight];

        maskBitmapVer.getPixels(pxsVer, 0, mWidth, 0, 0, mWidth, mHeight);
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;
                mVerArray[i][j] = Color.red(pxsVer[index]);
            }
        }

        //设置45度mask
        mAngel45Array = new int[mHeight][mWidth];

        Canvas cavans45 = new Canvas(tempBitmap);
        cavans45.rotate(-45, (mWidth + mHeight) / 2, (mWidth + mHeight) / 2);
        cavans45.drawRect(0, 0, mHeight + mWidth, mHeight + mWidth, paint1);

        Bitmap maskBitmap45 = Bitmap.createBitmap(tempBitmap, mHeight / 2, mWidth / 2, mWidth, mHeight);

        int[] pxs45 = new int[mWidth * mHeight];

        maskBitmap45.getPixels(pxs45, 0, mWidth, 0, 0, mWidth, mHeight);

        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;
                mAngel45Array[i][j] = Color.red(pxs45[index]);
            }
        }

        //设置反向45度mask
        mAngleRevert45Array = new int[mHeight][mWidth];
        Canvas cavansRevert45 = new Canvas(tempBitmap);
        cavansRevert45.rotate(45, (mWidth + mHeight) / 2, (mWidth + mHeight) / 2);
        cavansRevert45.drawRect(0, 0, mHeight + mWidth, mHeight + mWidth, paint1);

        Bitmap maskBitmapRevert45 = Bitmap.createBitmap(tempBitmap, mHeight / 2, mWidth / 2, mWidth, mHeight);

        int[] pxsRevert45 = new int[mWidth * mHeight];

        maskBitmapRevert45.getPixels(pxsRevert45, 0, mWidth, 0, 0, mWidth, mHeight);

        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;
                mAngleRevert45Array[i][j] = Color.red(pxsRevert45[index]);
            }
        }

        //回收bitmap
        bitmap.recycle();
        bitmap = null;
        tempBitmap.recycle();
        tempBitmap = null;
        maskBitmap.recycle();
        maskBitmap = null;
        maskBitmap45.recycle();
        maskBitmap45 = null;
        maskBitmapVer.recycle();
        maskBitmapVer = null;
        maskBitmapRevert45.recycle();
        maskBitmapRevert45 = null;
    }

    /**
     * 获取parent所在的区域中的所有点
     * 要求所有点的绘制次数一致
     * <p>
     * （利用栈实现图的深度遍历）
     *
     * @param parent
     * @return
     */
    public ArrayList<Point> getPoints(Point parent) {

        long start = System.currentTimeMillis();
        int drawTime = mDrawCountArray[parent.y][parent.x];

        ArrayList<Point> mData = new ArrayList<>();
        Stack<Point> stack = new Stack<>();
        stack.push(parent);
        mCurrentArray[parent.y][parent.x] = 0;
        while (stack.size() > 0) {
            Point item = stack.pop();
            mData.add(item);
            for (int i = item.x - 1; i <= item.x + 1; i++) {
                for (int j = item.y - 1; j <= item.y + 1; j++) {
                    if (i >= 0 && i < mWidth && j >= 0 && j < mHeight && drawTime == mDrawCountArray[j][i]) {
                        if (mCurrentArray[j][i] == 1) {
                            stack.push(new Point(i, j));
                            mCurrentArray[j][i] = 0;
                        }
                    }
                }
            }
        }
        return mData;
    }

    /**
     * 重新绘制
     */
    public void reTry() {
        mIsStop = false;
        mIsFirstArray = true;
        while (mQueue.poll() != null) {
        }
        mCount = 0;
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                mDrawCountArray[i][j] = 0;
            }
        }
    }

    public void stop() {
        mIsStop = true;
    }
}
