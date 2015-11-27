package com.eje_c.rajawalicardboard;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by iao on 27/11/15.
 */
public class MyCustomButton extends Button {

    public MyCustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/OPTIMA.TTF"));
    }
}
