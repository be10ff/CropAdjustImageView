package ru.be10ff.cropajustimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by abelov on 04.03.16.
 */
public class CropAdjustImageView extends ImageView {
    private int imageWidth;
    private int imageHeight;

    private float maxWidth;
    private float maxHeight;

    float scale;
    boolean isCutted;

    public CropAdjustImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);



    }
    public CropAdjustImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs){
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CropAdjustImageView,
                0, 0);

        try {
            maxWidth = a.getInteger(R.styleable.CropAdjustImageView_ratioHeight, 0);
            maxHeight = a.getInteger(R.styleable.CropAdjustImageView_ratioWidth, 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Matrix matrix = getImageMatrix();
        
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample);
        Bitmap bitmap = drawableToBitmap(getDrawable());
        RectF target = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
//        Rect source = new Rect(0, 0, imageWidth, (int)(canvas.getHeight() / scale));

//        canvas.drawBitmap(bitmap, source, target, null);

        RectF sourceF = new RectF(0, 0, imageWidth, (int)(canvas.getHeight() / scale));
        matrix.setRectToRect(sourceF, target, Matrix.ScaleToFit.START);
        setImageMatrix(matrix);
        super.onDraw(canvas);

        if(isCutted){
            Paint shaderpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Bitmap tile = BitmapFactory.decodeResource(getResources(), R.drawable.ic_cut_tile);
            Shader mShader1 = new BitmapShader(tile, Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
            shaderpaint.setShader(mShader1);
            Matrix matrixTile = new Matrix();
            matrixTile.setTranslate(0, canvas.getHeight()%tile.getHeight());
            mShader1.setLocalMatrix(matrixTile);
            RectF cutRect = new RectF(0, canvas.getHeight() - tile.getHeight(), canvas.getWidth(), canvas.getHeight());
            canvas.drawRect(cutRect, shaderpaint);
        }
    }


    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Drawable drawable = getDrawable();
        if(drawable == null){
            setMeasuredDimension(0, 0);
            return;
        }

        this.imageWidth = drawable.getIntrinsicWidth();
        this.imageHeight = drawable.getIntrinsicHeight();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size android:layout_width="280dp"
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than... android:layout_width="match_parent"

            width = Math.min(imageWidth, widthSize);
        } else {
            //Be whatever you want android:layout_width="wrap_content"
            width = imageWidth;
        }

        scale = (float) width / (float) imageWidth;
        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size android:layout_height="210dp"
            // ignored for now cuz width is priory
            //
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than... android:layout_height="match_parent"
            height = Math.min(Math.min((int)Math.ceil((imageHeight*scale)), heightSize), (int)Math.ceil(width*(maxHeight/maxWidth)));
        } else {
            //Be whatever you want android:layout_height="wrap_content"
            height = imageHeight;
        }

        int calcHeight;
        if (imageWidth * maxHeight >= imageHeight * maxWidth) {
//            scale = (float) width / (float) imageWidth;
            calcHeight = (int)Math.ceil((imageHeight*scale));
            isCutted = false;
        } else {
//            scale = (float) width / (float) imageWidth;
            calcHeight = height;
            isCutted = true;
        }
        setMeasuredDimension(width, calcHeight);
    }



    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }




}
