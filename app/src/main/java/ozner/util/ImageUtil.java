package ozner.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.ozner.xvidoeplayer.R;

/**
 * Created by xinde on 2016/4/25.
 */
public class ImageUtil {

    ImageLoadingListener listener;
    private Context mContext;

    public ImageUtil(Context context) {
        this.mContext = context;
    }

    public void setImageLoadingListener(ImageLoadingListener listener) {
        this.listener = listener;
    }

    private static DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheInMemory(true) // 加载图片时会在内存中加载缓存
            .cacheOnDisc(true) // 加载图片时会在磁盘中加载缓
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
            .build();

    public void loadImage(ImageView iv_view, String url) {
        if (iv_view != null) {
            ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(mContext);
            ImageLoader.getInstance().init(configuration);
            ImageLoader.getInstance().displayImage(url, iv_view, options, listener);
        }
    }


//    public Uri pathToUri(String path) {
//
//        Uri mUri = Uri.parse("content://media/external/images/media");
//        Uri mImageUri = null;
//
//        Cursor cursor = managedQuery(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
//                null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
//        cursor.moveToFirst();
//
//        while (!cursor.isAfterLast()) {
//            String data = cursor.getString(cursor
//                    .getColumnIndex(MediaStore.MediaColumns.DATA));
//            if (path.equals(data)) {
//                int ringtoneID = cursor.getInt(cursor
//                        .getColumnIndex(MediaStore.MediaColumns._ID));
//                mImageUri = Uri.withAppendedPath(mUri, ""
//                        + ringtoneID);
//                break;
//            }
//            cursor.moveToNext();
//        }
//
//        return mImageUri;
//    }


    public static Bitmap toRoundBitmap(Context context, Bitmap bitmap) {
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float roundPx;
            float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
            if (width <= height) {
                roundPx = width / 2;
                left = 0;
                top = 0;
                right = width;
                bottom = width;
                height = width;
                dst_left = 0;
                dst_top = 0;
                dst_right = width;
                dst_bottom = width;
            } else {
                roundPx = height / 2;
                float clip = (width - height) / 2;
                left = clip;
                right = width - clip;
                top = 0;
                bottom = height;
                width = height;
                dst_left = 0;
                dst_top = 0;
                dst_right = height;
                dst_bottom = height;
            }

            Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
            final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
            final RectF rectF = new RectF(dst);

            paint.setAntiAlias(true);// 设置画笔无锯齿

            canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas
            paint.setColor(color);

            // 以下有两种方法画圆,drawRounRect和drawCircle
            // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
            canvas.drawCircle(roundPx, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
            canvas.drawBitmap(bitmap, src, dst, paint); //以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

            return output;
        } else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        }
    }

    public static Bitmap getLocalBitmap(String url) {
        try {
//            FileInputStream fis = new FileInputStream(url);
            BitmapFactory.Options options = new BitmapFactory.Options();
            return BitmapFactory.decodeFile(url);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Drawable getLocalDrawable(String url) {
        try {
            return Drawable.createFromPath(url);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
