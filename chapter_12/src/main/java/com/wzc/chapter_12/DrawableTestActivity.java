package com.wzc.chapter_12;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

/**
 * @author wangzhichao
 * @since 2022/3/28
 */
public class DrawableTestActivity extends Activity {
    private static final String TAG = "DrawableTestActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawable_test_activity);
        ImageView iv = (ImageView) findViewById(R.id.image);
        Drawable drawable = iv.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Log.d(TAG, "onCreate: bitmap width=" + bitmap.getWidth() +
                    ", height=" + bitmap.getHeight() +
                    ", byteCount=" + bitmap.getByteCount());
        }
        printDrawableFolderDensity();
    }

    private void printDrawableFolderDensity(){
        TypedValue typedValue = new TypedValue();
        Resources resources= getResources();
        resources.openRawResource(R.drawable.water, typedValue);
        int density=typedValue.density;
        Log.d(TAG, "printDrawableFolderDensity: density=" + density);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, DrawableTestActivity.class);
        context.startActivity(starter);
    }
}
