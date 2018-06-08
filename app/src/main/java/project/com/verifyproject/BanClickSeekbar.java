package project.com.verifyproject;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by 于德海 on 2018/6/7.
 * package inter.baisong.widgets
 * email : yudehai0204@163.com
 *
 * @describe
 */

public class BanClickSeekbar extends AppCompatSeekBar {
    private int index = 150;
    private boolean k = true;

    public BanClickSeekbar(Context context) {
        super(context);
    }

    public BanClickSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BanClickSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            k = true;
            if (x - index > 20) {
                k = false;
                return true;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (!k){
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }

}
