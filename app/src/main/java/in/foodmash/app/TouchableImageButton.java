package in.foodmash.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

/**
 * Created by Zeke on Apr 05 2015.
 */
public class TouchableImageButton extends ImageButton {

    public TouchableImageButton(Context context) {
        super(context);
    }

    public TouchableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int maskedAction = event.getActionMasked();
        if (maskedAction == MotionEvent.ACTION_DOWN) {
            setColorFilter(Color.argb(255, 150, 0, 0), PorterDuff.Mode.DST_IN);
        } else if (maskedAction == MotionEvent.ACTION_UP)
            setColorFilter(null);
        return super.onTouchEvent(event);
    }}