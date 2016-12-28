package com.example.pluginsharelib;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * 等比控件 可自定义宽高比
 *
 * @author cailiming
 *
 */
public class RectImageView extends ImageView {

    // 默认是正方形
    private float activeType = 1.0f;

    public RectImageView(Context context) {
       this(context, null);
    }

    public RectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomViewBounds);

        activeType = a.getFloat(R.styleable.CustomViewBounds_widthToheight, 1.0f);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);

        int width = measureWidth(widthMeasureSpec, heightMeasureSpec);
        int height = measureHeight(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    /**
     * MeasureSpec.EXACTLY是精确尺寸，当我们将控件的layout_width或layout_height指定为具体数值时如andorid:layout_width="50dip"，
     * 或者为FILL_PARENT是，都是控件大小已经确定的情况，都是精确尺寸。
     *
     * MeasureSpec.AT_MOST是最大尺寸，当控件的layout_width或layout_height指定为WRAP_CONTENT时，控件大小一般随着控件的子空间或内容进行变化，
     * 此时控件尺寸只要不超过父控件允许的最大尺寸即可。因此，此时的mode是AT_MOST，size给出了父控件允许的最大尺寸。
     *
     * MeasureSpec.UNSPECIFIED是未指定尺寸，这种情况不多，一般都是父控件是AdapterView，通过measure方法传入的模式。
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     * @return
     */
    private int measureWidth(int widthMeasureSpec, int heightMeasureSpec) {
        int specMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int specSize = View.MeasureSpec.getSize(widthMeasureSpec);

        int result = 0;
        // 表示宽使用了fill_parent
        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        }
        // 表示宽使用了wrap_parent
        else if (specMode == View.MeasureSpec.AT_MOST || specMode == View.MeasureSpec.UNSPECIFIED) {
            int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
            // 如高是fill_parent
            if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                // 将宽设置为高的 activeType
                result = (int) (View.MeasureSpec.getSize(heightMeasureSpec) * activeType);
            } else {
                result = specSize;
            }
        }

        return result;
    }

    private int measureHeight(int widthMeasureSpec, int heightMeasureSpec) {

        int specMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int specSize = View.MeasureSpec.getSize(heightMeasureSpec);

        int result = 0;
        // 表示高是fill_parent
        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        }
        // 表示高是wrap_parent
        else if (specMode == View.MeasureSpec.AT_MOST || specMode == View.MeasureSpec.UNSPECIFIED) {
            int withSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
            // 如宽是fill_parent
            if (withSpecMode == View.MeasureSpec.EXACTLY) {
                // 将高设置为宽的activeType
                result = (int) (View.MeasureSpec.getSize(widthMeasureSpec) * activeType);
            } else {
                result = specSize;
            }
        }
        return result;
    }

}
