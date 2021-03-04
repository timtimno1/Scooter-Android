package tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import androidx.annotation.RequiresApi;
import java.io.ByteArrayOutputStream;

public class NotificationCatchForGoogleMap
{
    private static Icon bitmapIcon;
    private static String string;//儲存包名、標題、內容文字
    private static final byte[] resolution={48,72,90,95,113,120,126};
    private static final byte[] value={0,0,0,0,113,0,0};
    private static final byte[][] feature={{30,30},{28,29},{28,31},{32,33},{6,9},{33,30},{28,39}};

    //接收資料
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void show(String packageName, String title, String text, Icon large, Context ct)
    {

        int Cont=0;
        String direction="無";
        bitmapIcon = large;
        Bitmap bi;
        int index;
        byte[] biArray;
        byte bitmapW=0;
        try {
            bi=drawableToBitmap (bitmapIcon.loadDrawable(ct));
            bitmapW=(byte)bi.getWidth();
            index=find(bitmapW);
            biArray=getBytesByBitmap(bi);
            for(byte i:biArray)
                if(i==value[index])Cont++;
            if(Cont==feature[index][0])
                direction="右";
            else if (Cont==feature[index][1])
                direction="左";
        }
        catch (Exception e)
        {
            System.out.println("wait");
        }


        string = "\n\n" +
                "距離:" + title.replaceAll("-.*", "") + "\n\n" +
                "下個轉彎方向:" + direction + "轉" + "Cont:" + Cont + " Resolution:" + bitmapW + "\n\n" +
                "到達時間:" + text + "\n\n";


        /*new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                handler.sendMessage(msg);
            }
        }).start();*/
    }

    /*private static Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                //將資料顯示，更新至畫面
                textView.setText(string);
                largeIcon.setImageIcon(bitmapIcon);
            } catch (Exception e) {
            }
        }
    };*/

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0)
        {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        }
        else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    public static byte[] getBytesByBitmap(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getAllocationByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }
    public static byte find(byte i)
    {
        byte j;
        for(j=0;j<resolution.length;j++)
            if(i==resolution[j])
                break;
        return j;
    }
}
