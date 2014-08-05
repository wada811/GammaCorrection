/*
 * Copyright 2014 ssl001<at.wada811@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.wada811.gammacorrection;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import at.wada811.utils.BitmapUtils;
import at.wada811.utils.LogUtils;

public class MainActivity extends FragmentActivity {

    final MainActivity self = this;
    private ImageView mImageView;
    private SeekBar mSeekBar;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null){
            initCursorLoader();
        }

        mImageView = (ImageView)findViewById(R.id.image);
        mSeekBar = (SeekBar)findViewById(R.id.seek);
        mSeekBar.setMax(25);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){
                int progress = seekBar.getProgress();
                double gamma = progress / 10.0;
                LogUtils.d("progress: " + progress);
                LogUtils.d("gamma: " + gamma);
                Bitmap bitmap = gamma(gamma);
                mImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

            }
        });
    }

    protected Bitmap gamma(double gamma){
        int[] lut = new int[256];
        for(int i = 0; i < lut.length; i++){
            lut[i] = Math.min((int)Math.round(255 * Math.pow(i / 255.0, 1.0 / gamma)), 255);
        }
        Bitmap bitmap = BitmapUtils.copy(mBitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for(int w = 0; w < width; w++){
            for(int h = 0; h < height; h++){
                int r = 0;
                int g = 0;
                int b = 0;
                int color = pixels[w + h * width];
                r = Color.red(color);
                g = Color.green(color);
                b = Color.blue(color);
                // ガンマ補正
                r = lut[r];
                g = lut[g];
                b = lut[b];
                //
                color = Color.rgb(r, g, b);
                pixels[w + h * width] = color;
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private void initCursorLoader(){
        getSupportLoaderManager().initLoader(0, null, new LoaderCallbacks<Cursor>(){

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle data){
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                return new CursorLoader(self, uri, null, null, null, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
                LogUtils.d("Position: " + cursor.getPosition());
                boolean moveToFirst = cursor.moveToFirst();
                LogUtils.d("moveToFirst: " + moveToFirst);
                LogUtils.d("Position: " + cursor.getPosition());
                cursor.moveToNext();
                mBitmap = loadBitmap(cursor);
                mImageView.setImageBitmap(mBitmap);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader){

            }
        });
    }

    private Bitmap loadBitmap(Cursor cursor){
        LogUtils.d("Position: " + cursor.getPosition());
        String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
        LogUtils.d(filePath);
        Bitmap bitmap = BitmapUtils.createBitmapFromFile(filePath, 640, 480);
        return bitmap;
    }

}
