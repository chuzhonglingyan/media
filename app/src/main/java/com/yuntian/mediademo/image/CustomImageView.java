package com.yuntian.mediademo.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.io.File;

public class CustomImageView extends View {

    //设置默认的宽和高
    private   int DEFUALT_VIEW_WIDTH = 100;
    private   int DEFUALT_VIEW_HEIGHT = 100;

    private Paint paint = new Paint();
    private Bitmap bitmap;


    public CustomImageView(Context context) {
        this(context, null);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        File file=context.getExternalFilesDir("image");
        if (file!=null){
            bitmap = BitmapFactory.decodeFile(file.getPath() + File.separator + "u=2891948847,1197284497&fm=26&gp=0.jpg");
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 不建议在onDraw做任何分配内存的操作
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, paint);
            DEFUALT_VIEW_WIDTH=bitmap.getWidth();
            DEFUALT_VIEW_HEIGHT=bitmap.getHeight();
        }else {
            DEFUALT_VIEW_WIDTH = 100;
            DEFUALT_VIEW_HEIGHT = 100;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureDimension(DEFUALT_VIEW_WIDTH, widthMeasureSpec);
        int height = measureDimension(DEFUALT_VIEW_HEIGHT, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    /**
     * @param defualtSize 设置的默认大小 * @param measureSpec 父控件传来的widthMeasureSpec，heightMeasureSpec * @return 结果
     */
    public int measureDimension(int defualtSize, int measureSpec) {
        int result;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        //1,layout中自定义组件给出来确定的值，比如100dp
        // 2,layout中自定义组件使用的是match_parent，但父控件的size已经可以确定了，比如设置的具体的值或者match_parent
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } //layout中自定义组件使用的wrap_content
        else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defualtSize, specSize);//建议：result不能大于specSize
        } else {//UNSPECIFIED,没有任何限制，所以可以设置任何大小
            result = defualtSize;
        }
        return result;
    }

}


