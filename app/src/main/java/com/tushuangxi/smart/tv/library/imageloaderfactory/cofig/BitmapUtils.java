package com.tushuangxi.smart.tv.library.imageloaderfactory.cofig;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class BitmapUtils {

    public static Bitmap toBitmap(Context context, int resId, Integer maxWidth, Integer maxHeight) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resId);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight,
                true);
        bitmap.recycle();
        return scaled;
    }

    public static Bitmap toBitmap(InputStream input, Integer maxWidth,
                                  Integer maxHeight) {

        Options options = new Options();
        if ((maxWidth != null) && (maxHeight != null)) {
            // Set the scaling options.
            float scale = Math.min(maxWidth.floatValue() / options.outWidth,
                    maxHeight.floatValue() / options.outHeight);
            options.inSampleSize = Math.round(1 / scale);
        }
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(input, null, options);
    }

    /**
     * Gets a {@link Bitmap} from a {@link Uri}. Resizes the image to a
     * determined width and height.
     *
     * @param uri       The {@link Uri} from which the image is obtained.
     * @param maxWidth  The maximum width of the image used to scale it. If null, the
     *                  image won't be scaled
     * @param maxHeight The maximum height of the image used to scale it. If null, the
     *                  image won't be scaled
     * @return {@link Bitmap} The resized image.
     */
    public static Bitmap toBitmap(Context context, Uri uri, Integer maxWidth, Integer maxHeight) {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            Options options = new Options();
            options.inJustDecodeBounds = true;
            InputStream openInputStream = context.getContentResolver()
                    .openInputStream(uri);
            BitmapFactory.decodeStream(openInputStream, null, options);
            openInputStream.close();

            // Calculate inSampleSize
            if ((maxWidth != null) && (maxHeight != null)) {
                float scale = Math.min(
                        maxWidth.floatValue() / options.outWidth,
                        maxHeight.floatValue() / options.outHeight);
                options.inSampleSize = Math.round(1 / scale);
            }

            // Decode bitmap with inSampleSize set
            openInputStream = context.getContentResolver().openInputStream(uri);
            options.inJustDecodeBounds = false;
            Bitmap result = BitmapFactory.decodeStream(openInputStream, null,
                    options);
            openInputStream.close();
            return result;
        } catch (Exception e) {
            Log.e(BitmapUtils.class.getSimpleName(), e.getMessage());
            return null;
        }
    }

    public static ByteArrayInputStream toPNGInputStream(Context context, Uri uri,
                                                        Integer maxWidth, Integer maxHeight) {
        Bitmap bitmap = BitmapUtils.toBitmap(context, uri, maxWidth, maxHeight);
        return BitmapUtils.toPNGInputStream(bitmap);
    }

    /**
     * Compress the bitmap to a PNG and return its {@link ByteArrayInputStream}
     *
     * @param bitmap The {@link Bitmap} to compress
     * @return The {@link ByteArrayInputStream}
     */
    public static ByteArrayInputStream toPNGInputStream(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        return new ByteArrayInputStream(bytes.toByteArray());
    }

    public static int getExifRotation(String imageFilePath) {
        if (imageFilePath == null)
            return 0;
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            // We only recognize a subset of orientation tag values
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return ExifInterface.ORIENTATION_UNDEFINED;
            }
        } catch (IOException e) {
            return 0;
        }
    }

    public static int calculateInSampleSize(Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        return calculateInSampleSize(width, height, reqWidth, reqHeight);
    }

    public static int calculateInSampleSize(int width, int height,
                                            int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth,
                                                     int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public static int getExifRotation(File tempFile, Bitmap bitmap) {
        try {
            // ???????????????
            FileOutputStream b = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);
            b.flush();
            b.close();
            return getExifRotation(tempFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static Bitmap decodeSampledBitmapFromBitmap(Bitmap bitmap,
                                                       int reqWidth, int reqHeight) {
        // Calculate inSampleSize
        final Options options = new Options();
        options.inSampleSize = calculateInSampleSize(bitmap.getWidth(),
                bitmap.getHeight(), reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        byte[] data = bitmap2Bytes(bitmap);
        if (data != null) {
            bitmap.recycle();
            return BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } else {
            return bitmap;
        }
    }

    public static Bitmap rotaingBitmap(int angle, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    private static byte[] bitmap2Bytes(Bitmap bm) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bytes = baos.toByteArray();
            baos.close();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final Bitmap grey(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap greyBitmap = Bitmap.createBitmap(width, height,
                Config.ARGB_8888);
        Canvas canvas = new Canvas(greyBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
                colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return greyBitmap;
    }

    public static Bitmap circular(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        // ????????????????????????????????????
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int w;
        int deltaX = 0;
        int deltaY = 0;
        if (width <= height) {
            w = width;
            deltaY = height - w;
        } else {
            w = height;
            deltaX = width - w;
        }
        final Rect rect = new Rect(deltaX, deltaY, w, w);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        // ???????????????????????????
        int radius = (int) (Math.sqrt(w * w * 2.0d) / 2);
        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static final Bitmap alpha(Bitmap bitmap, int alpha) {
        float[] matrixItems = new float[]{1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
                1, 0, 0, 0, 0, 0, alpha / 255f, 0, 0, 0, 0, 0, 1};
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap alphaBitmap = Bitmap.createBitmap(width, height,
                Config.ARGB_8888);
        Canvas canvas = new Canvas(alphaBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix(matrixItems);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
                colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return alphaBitmap;
    }

    /**
     * ???Bitmap??????????????????????????????
     *
     * @author Huyf Email:my519820363@gmail.com
     */
    public static Bitmap fastblur(Context context, Bitmap sentBitmap, int radius) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }


    /**
     * ????????????
     * @param image
     * @return
     */
    private static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options =  80;
        image.compress(Bitmap.CompressFormat.JPEG, options, baos);//???????????????????????????100????????????????????????????????????????????????baos???
        while ( baos.toByteArray().length / 1024 > 150) {    //?????????????????????????????????????????????80kb,??????????????????
            baos.reset();//??????baos?????????baos
            options -= 10;//???????????????10
            Log.i("test", "???????????????");
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//????????????options%?????????????????????????????????baos???
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//?????????????????????baos?????????ByteArrayInputStream???
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//???ByteArrayInputStream??????????????????
        return bitmap;
    }


    /**
     * ??????????????????
     * @param srcPath
     * @return
     */
    public static File getCompressImage(String srcPath) {
        Options newOpts = new Options();
        //??????????????????????????????options.inJustDecodeBounds ??????true???
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//????????????bm??????
        newOpts.inPreferredConfig = Config.RGB_565;
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //??????????????????????????????800*480??????????????????????????????????????????
        float hh = 800f;//?????????????????????800f
        float ww = 480f;//?????????????????????480f
        //????????????????????????????????????????????????????????????????????????????????????????????????
        int be = 1;//be=1???????????????
        if (w > h && w > ww) {//???????????????????????????????????????????????????
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//???????????????????????????????????????????????????
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//??????????????????
        //??????????????????????????????????????????options.inJustDecodeBounds ??????false???
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        return saveBitmap2file(compressImage(bitmap));//?????????????????????????????????????????????
    }

    /**
     * bitmap???file
     * @param bmp
     * @return
     */
    public static File saveBitmap2file(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        File imageFile = null;
        try {
            imageFile = File.createTempFile("tempImage" + System.currentTimeMillis(), ".png");
        }  catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream fstream = null;
        try {
            fstream = new FileOutputStream(imageFile);
            BufferedOutputStream bStream = new BufferedOutputStream(fstream);
            bStream.write(byteArray);
            if (bStream != null) {
                bStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;

    }

    /**
     * ??????????????????
     * @param image
     * @return
     */
    public static Bitmap getCompressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if( baos.toByteArray().length / 1024>1024) {//????????????????????????1M,????????????????????????????????????BitmapFactory.decodeStream????????????
            baos.reset();//??????baos?????????baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//????????????50%?????????????????????????????????baos???
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Options newOpts = new Options();
        //??????????????????????????????options.inJustDecodeBounds ??????true???
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //??????????????????????????????800*480??????????????????????????????????????????
        float hh = 800f;//?????????????????????800f
        float ww = 480f;//?????????????????????480f
        //????????????????????????????????????????????????????????????????????????????????????????????????
        int be = 1;//be=1???????????????
        if (w > h && w > ww) {//???????????????????????????????????????????????????
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//???????????????????????????????????????????????????
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//??????????????????
        //??????????????????????????????????????????options.inJustDecodeBounds ??????false???
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);//?????????????????????????????????????????????
    }


}
