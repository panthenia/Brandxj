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
    int countSec = 0;
    int findNum = PublicData.getInstance().beacons.size();
    boolean enbled = true;
    public ScanView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        mPaint = new Paint();
        wpaint1 = new Paint();
        wpaint2 = new Paint();

        mPaint.setColor(0x0180ff);
        mPaint.setAlpha(alpha);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(150);
        mPaint.setTextAlign(Paint.Align.CENTER);
        wpaint1.setColor(0xff0180ff);
        wpaint1.setAntiAlias(true);
        wpaint1.setStrokeWidth(10);
        wpaint1.setTextSize(50);
        wpaint1.setTextAlign(Paint.Align.CENTER);
    }
    public void reSetCountSec(){
        countSec = 0;
    }
    public void setEnabled(boolean e){
        enbled = e;
    }
    public void setFindNum(int newNum){
        findNum = newNum;
    }
    public void rePaint(){

        if(enbled){
            countSec += 1;
            invalidate();
        }
    }
    public void addCountSec(){
        if (enbled){

        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(String.valueOf(countSec)+"S",getWidth() / 2, (getHeight() / 8)*3, mPaint);
        canvas.drawText("扫描到"+String.valueOf(findNum)+"个Beacon", getWidth() / 2, (getHeight() / 8) *7, wpaint1);

    }

}
