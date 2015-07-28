package com.p.DrawMap.Utils;

/**
 * Created by p on 2015/6/16.
 */
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class FilterListView extends ListView {

    public FilterListView(Context context) {
        super(context);
    }

    public FilterListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
