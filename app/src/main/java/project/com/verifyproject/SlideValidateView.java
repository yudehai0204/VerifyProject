package project.com.verifyproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by 于德海 on 2018/6/7.
 * package inter.baisong.widgets
 * email : yudehai0204@163.com
 *
 * @describe
 */

public class SlideValidateView extends AppCompatImageView {
    //Image背景图
    private Bitmap mBitmap;
    //滑块对应的画笔
    private Paint mPaint;
    //滑块的宽度占比  高度自动对比
    private float mSlideWidthScale;
    //滑块的源图
    private Bitmap mResourseBitmap;
    //滑块图片
    private Bitmap mSlideBitmap;
    //滑块的宽度
    private int mSlide_width = 0;
    //滑块高度
    private int mSlide_height=0;
    //是否重新绘制图片
    private boolean isReset = true;
    //偏移值  最小2 最大100
    private int deviation;
    //阴影颜色
    private int shade_color= Color.GRAY;
    //阴影图片
    private Bitmap mShadeBitmap;
    //图片的最大宽度，最大高度
    private int max_width,max_height;
    //滑块移动距离
    private int mSlideMoveDistance =0;
    //随机生成的目标阴影xy初始坐标
    private int mShadeRandom_x,mShadeRandom_y;
    private OnSlideListener mListener;
    public interface OnSlideListener{
        void success();
        void error();

    }


    public SlideValidateView(Context context) {
        this(context,null);
    }

    public SlideValidateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SlideValidateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(R.styleable.SlideValidateView);
        max_width = ta.getDimensionPixelOffset(R.styleable.SlideValidateView_max_width,0);
        max_height = ta.getDimensionPixelOffset(R.styleable.SlideValidateView_max_height,0);
        Drawable drawable = ta.getDrawable(R.styleable.SlideValidateView_mSlideBitmap);
        mResourseBitmap = initResourseBitmap(drawable);
        shade_color = ta.getColor(R.styleable.SlideValidateView_shade_color, Color.GRAY);
        mSlideWidthScale = ta.getFloat(R.styleable.SlideValidateView_slideWidthScale,0.2f);
        deviation = ta.getInteger(R.styleable.SlideValidateView_deviation,10);
        ta.recycle();

        if(max_height==0||max_width==0){
            Point point = new Point();//x为宽,y为高
            ((Activity)context).getWindowManager().getDefaultDisplay().getSize(point);
            max_width = point.x;
            max_height =point.y/2;
        }
        if(deviation<2){
            deviation=2;
        }else if(deviation>100){
            deviation=100;
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//抗锯齿
    }


    public void setOnSlideListener(OnSlideListener listener){
        this.mListener = listener;
    }


    public void reset(){
        isReset = true;
        mSlideMoveDistance=0;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isReset){
            mBitmap = getViewBitmap();
            if(mSlide_width==0&&mBitmap!=null&&mBitmap.getWidth()!=0){
                initSlideWH();
            }
            initSlideRandomXY();
            mShadeBitmap = Bitmap.createBitmap(mSlide_width,mSlide_height, Bitmap.Config.ARGB_8888);
            mShadeBitmap.eraseColor(shade_color);
            mSlideBitmap = Bitmap.createBitmap(mBitmap,mShadeRandom_x,mShadeRandom_y,mSlide_width,mSlide_height);

        }
        isReset = false;
        canvas.drawBitmap(drawImage(mShadeBitmap),mShadeRandom_x,mShadeRandom_y,mPaint);
        canvas.drawBitmap(drawImage(mSlideBitmap),mSlideMoveDistance,mShadeRandom_y,mPaint);

    }

    /***
     *
     * @param progress 1-100
     */
    public void setSlide_X(int progress){
        mSlideMoveDistance = (mBitmap.getWidth()-mSlide_width)/100*progress;
        if (mSlideMoveDistance > mBitmap.getWidth() - mSlide_width) {
            mSlideMoveDistance = mBitmap.getWidth() - mSlide_width;
        }
        postInvalidate();
    }

    /***
     * 检测
     */
    public void checkSlidePoint(){
        if( mListener !=null ){
            if (Math.abs(mSlideMoveDistance - mShadeRandom_x) <= deviation) {
                mListener.success();
            } else {
                mListener.error();
            }
        }

    }


    /****
     * 重构图片
     * @return
     */
    private Bitmap drawImage(Bitmap bitmap) {
        // 绘制图片
        Bitmap showB;
        if (null != mResourseBitmap) {
            showB = handleBitmap(mResourseBitmap, mSlide_width, mSlide_height);
        } else {
            showB = handleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.star_shade), mSlide_width, mSlide_height);
        }
        Bitmap resultBmp = Bitmap.createBitmap(mSlide_width, mSlide_height, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Canvas canvas = new Canvas(resultBmp);
        canvas.drawBitmap(showB, new Rect(0, 0, mSlide_width, mSlide_height),
                new Rect(0, 0, mSlide_width, mSlide_height), paint);
        // 选择交集去上层图片
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        canvas.drawBitmap(bitmap, new Rect(0, 0, mSlide_width, mSlide_height),
                new Rect(0, 0, mSlide_width, mSlide_height), paint);
        return resultBmp;
    }
    /**
     * 缩放图片
     *
     * @param bp
     * @param x
     * @param y
     * @return
     */
    public static Bitmap handleBitmap(Bitmap bp, float x, float y) {
        int w = bp.getWidth();
        int h = bp.getHeight();
        float sx = (float) x / w;
        float sy = (float) y / h;
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);
        Bitmap resizeBmp = Bitmap.createBitmap(bp, 0, 0, w,
                h, matrix, true);
        return resizeBmp;
    }
    /***
     * 初始化滑块的宽高
     */
    private void initSlideWH() {
        mSlide_width = (int) (mBitmap.getWidth()*mSlideWidthScale);
        float scale = ((float) mSlide_width)/mResourseBitmap.getWidth();
        mSlide_height = (int) (mResourseBitmap.getHeight()*scale);
    }

    /***
     * 初始化滑块起始位置
     */
    private void initSlideRandomXY() {
        mShadeRandom_x = (int) (mBitmap.getWidth()/2+(Math.random()*(mBitmap.getWidth()/2))-mSlide_width);
        mShadeRandom_y = (int) (Math.random() * (mBitmap.getHeight()-mSlide_height));
        if(mShadeRandom_x+mSlide_width>mBitmap.getWidth()||mShadeRandom_y+mSlide_height>mBitmap.getHeight()){
            initSlideRandomXY();
            return;
        }

    }

    private Bitmap initResourseBitmap(Drawable drawable){
        Bitmap bitmap;
        if(drawable ==null){
            bitmap = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.star_shade);
        }else {
            bitmap = drawable2bitmap(drawable);
        }
        return bitmap;

    }
    /***
     * drawable 转 bitmap
     * @param drawable
     * @return
     */
    private Bitmap drawable2bitmap(Drawable drawable){
        if(drawable==null){
            return null;
        }else if(drawable instanceof BitmapDrawable){
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public Bitmap getViewBitmap() {
        Bitmap b = drawable2bitmap(getDrawable());
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        // 如果图片的宽或者高与view的宽高不匹配，计算出需要缩放的比例；缩放后的图片的宽高，一定要大于我们view的宽高；所以我们这里取大值；
        scaleX = getWidth() * 1.0f / b.getWidth();
        scaleY = getHeight() * 1.0f / b.getHeight();
        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY);
        Bitmap bd = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
                matrix, true);
        return bd;
    }
}
