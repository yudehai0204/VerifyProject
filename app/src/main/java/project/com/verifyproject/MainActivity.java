package project.com.verifyproject;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private int end_x, end_y;//验证按钮距离顶部的距离
    private int start_x, start_y;//开始的x,y位置
    private ImageView img_start, img_end;
    private int move_x, move_y;
    private RelativeLayout.LayoutParams layoutParams,layoutParams_end;
    private boolean isCheck;//是否选中
    private ScaleAnimation scaleAnimation;
    private int dip_110;//View 的高度写死了110dip
    private LinearLayout ll_slide;

    private BanClickSeekbar mSeekbar;
    private SlideValidateView mSlide;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img_start = findViewById(R.id.img_start);
        img_end = findViewById(R.id.img_end);
        mSeekbar = findViewById(R.id.mSeekbar);
        mSlide = findViewById(R.id.mSlide);
        ll_slide = findViewById(R.id.ll_slide);
        layoutParams = (RelativeLayout.LayoutParams) img_start.getLayoutParams();
        layoutParams_end = (RelativeLayout.LayoutParams) img_end.getLayoutParams();
        initData();
        initListener();
    }


    private void initData() {
        Point point = new Point();//x为宽,y为高
        getWindowManager().getDefaultDisplay().getSize(point);
        end_x = (int) (Math.random()* point.x);
        dip_110 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,110,getResources().getDisplayMetrics());
        if(end_x>point.x- dip_110){
            end_x = point.x- dip_110;
        }
        int bottom_margin = (int) (Math.random()*dip_110);
        layoutParams_end.setMargins(end_x,0,0,bottom_margin);
        img_end.setLayoutParams(layoutParams_end);
        scaleAnimation = new ScaleAnimation(1.0f,1.1f,1.0f,1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setRepeatMode(ScaleAnimation.REVERSE);
        scaleAnimation.setRepeatCount(-1);
        scaleAnimation.setDuration(1000);
        img_end.startAnimation(scaleAnimation);
    }

    private float lastX, lastY;

    /***
     * 显示图片验证
     * @param view
     */
    public void imageVerify(View view){
        if(ll_slide.getVisibility() ==View.VISIBLE){
            return;
        }
        img_end.clearAnimation();
        Log.e("test",scaleAnimation.getDuration()+"");
        if(isCheck){
            img_start.setEnabled(true);
            img_end.setImageResource(R.drawable.img_aty_verify_end);
            layoutParams.setMargins(start_x, start_y, 0, 0);
            img_start.setLayoutParams(layoutParams);
            isCheck = false;
        }
        img_end.setVisibility(View.GONE);
        img_start.setVisibility(View.GONE);
        ll_slide.setVisibility(View.VISIBLE);

    }
    /***
     * 显示拖动验证
     * @param view
     */
    public void dragVerify(View view){
        if(img_start.getVisibility() ==View.VISIBLE){
            return;
        }
        img_end.setVisibility(View.VISIBLE);
        img_start.setVisibility(View.VISIBLE);
        img_end.startAnimation(scaleAnimation);
        ll_slide.setVisibility(View.GONE);
        mSeekbar.setProgress(0);
        mSlide.reset();
    }

    private void initListener() {
        img_start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        start_x = img_start.getLeft();
                        start_y = img_start.getTop();
                        end_y = img_end.getTop();
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //  不要直接用getX和getY,这两个获取的数据已经是经过处理的,容易出现图片抖动的情况
                        move_x = (int) (event.getRawX()-lastX);
                        move_y = (int) (event.getRawY()-lastY);
                        int distancex = start_x + move_x;
                        int distancey = start_y + move_y;
                        layoutParams.setMargins(distancex, distancey, 0, 0);
                        img_start.setLayoutParams(layoutParams);
                        if(distancex>end_x&&distancex<(end_x+img_end.getWidth())&&distancey>end_y&&distancey<(end_y+img_end.getHeight())){
                            Log.e("check","true");
                            if(!isCheck){
                                isCheck = true;
                                img_end.setImageResource(R.drawable.img_aty_verify_end_check);
                            }
                        }else if(isCheck){
                            Log.e("check","false");
                            isCheck = false;
                            img_end.setImageResource(R.drawable.img_aty_verify_end);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if(isCheck){
                            img_start.setEnabled(false);
                            Toast.makeText(MainActivity.this,"success",Toast.LENGTH_SHORT).show();
                        }else {
                            img_end.setImageResource(R.drawable.img_aty_verify_end);
                            layoutParams.setMargins(start_x, start_y, 0, 0);
                            img_start.setLayoutParams(layoutParams);
                        }

                        break;
                }

                return true;
            }
        });
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mSlide.setSlide_X(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSlide.checkSlidePoint();
            }
        });
        mSlide.setOnSlideListener(new SlideValidateView.OnSlideListener() {
            @Override
            public void success() {
                Toast.makeText(MainActivity.this,"success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error() {
                Toast.makeText(MainActivity.this,"error",Toast.LENGTH_SHORT).show();
                mSeekbar.setProgress(0);
                mSlide.reset();
            }
        });
    }




}
