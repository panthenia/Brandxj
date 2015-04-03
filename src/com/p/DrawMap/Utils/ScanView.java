package com.p.DrawMap.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.p.DrawMap.DataType.PublicData;


/**
 * Created by p on 2015/3/3.
 */
public class ScanView extends View {
    Paint mPaint = null;
    Paint wpaint1 = null;
    Paint wpaint2 = null;
    int raduis = 10;
    int alpha = 250;
    int findNum = PublicData.getInstance().beacons.size();
    boolean flag = true;
    public ScanView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        mPaint = new Paint();
        wpaint1 = new Paint();
        wpaint2 = new Paint();

    }
    public void setFindNum(int newNum){
        findNum = newNum;
    }
    public void rePaint(){
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(0x0180ff);
        mPaint.setAlpha(alpha);
        mPaint.setAntiAlias(true);
        wpaint1.setColor(0xff0180ff);
        wpaint1.setAntiAlias(true);
        wpaint2.setAntiAlias(true);
        wpaint2.setStyle(Paint.Style.STROKE);
        wpaint2.setColor(0xff0180ff);
        wpaint2.setTextSize(50);
        wpaint1.setStrokeWidth(10);
        wpaint1.setTextSize(100);
        wpaint2.setTextAlign(Paint.Align.CENTER);
        wpaint1.setTextAlign(Paint.Align.CENTER);
        if(raduis > getWidth()/3){
            raduis = 10;
            alpha = 250;
        }
        else {
            raduis += 5;
            alpha -= 250/((getWidth()/3)/5);
        }
        mPaint.setAlpha(alpha);
        canvas.drawCircle(getWidth() / 2, (getHeight() / 8)*3, raduis, mPaint);
        canvas.drawText("已检测到Beacon数量",getWidth() / 2, (getHeight() / 8) *6,  wpaint2);
        canvas.drawText(String.valueOf(findNum), getWidth() / 2, (getHeight() / 8) *7, wpaint1);

    }

}
