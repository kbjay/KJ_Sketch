package com.example.kb_jay.kj_sketch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "openCV";
    private BaseLoaderCallback mLoaderCallback;
    private SketchView mSumiao;
    private int mCount = 8;
    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initOpenCv();
        mSumiao = (SketchView) this.findViewById(R.id.sv_test);
    }

    public void test(View view) {
        draw();
    }

    /**
     * 绘制
     */
    private void draw() {
        Flowable.create(new FlowableOnSubscribe<int[][]>() {
            @Override
            public void subscribe(FlowableEmitter<int[][]> emitter) throws Exception {
                //利用opencv获取灰度图
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test4);

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
//                int screenWidth = 20;
                int height = (int) (bitmap.getHeight() * screenWidth / (float) bitmap.getWidth() + 0.5f);
                final Bitmap srcBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, height, true);

                Mat srcMat = new Mat();
                Mat grayMat = new Mat();

                Utils.bitmapToMat(srcBitmap, srcMat);

                Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_RGB2GRAY);

                Utils.matToBitmap(grayMat, srcBitmap);

                //获取灰度图的灰度值并分层发给自定义view
                int[] pxs = new int[srcBitmap.getWidth() * srcBitmap.getHeight()];

                srcBitmap.getPixels(pxs, 0, srcBitmap.getWidth(), 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());


                int w = srcBitmap.getWidth();
                int h = srcBitmap.getHeight();

                for (int i = 0; i < mCount; i++) {
                    int[][] temp = new int[h][w];
                    int limit = 256 / mCount * (i + 1);
                    for (int j = 0; j < h; j++) {
                        for (int k = 0; k < w; k++) {
                            int t = j * w + k;
                            int R = Color.red(pxs[t]);
                            if (R > limit) {
                                temp[j][k] = 0;
                            } else {
                                temp[j][k] = 1;
                            }
                        }
                    }
                    emitter.onNext(temp);
                }
                emitter.onComplete();
            }
        }, BackpressureStrategy.ERROR).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<int[][]>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        mSubscription = s;
                        s.request(mCount);
                    }

                    @Override
                    public void onNext(int[][] ints) {
                        mSumiao.addArray(ints);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 重新绘制
     *
     * @param view
     */
    public void reTry(View view) {
        mSumiao.reTry();
        mSubscription.cancel();

        mSumiao.postDelayed(new Runnable() {
            @Override
            public void run() {
                draw();
            }
        }, 500);
    }


    private void initOpenCv() {
        mLoaderCallback = new BaseLoaderCallback(getApplicationContext()) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.e(TAG, "OpenCV loaded successfully");
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }

            @Override
            public void onPackageInstall(int operation, InstallCallbackInterface callback) {

            }
        };
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getApplicationContext(), mLoaderCallback);
        } else {
            Log.e(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
