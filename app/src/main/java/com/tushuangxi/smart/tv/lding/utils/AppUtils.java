package com.tushuangxi.smart.tv.lding.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.tushuangxi.smart.tv.library.loading.conn.LoadingApp;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


/**
 * 工具类
 */
public class AppUtils {
    /**
     * 退出登录
     *
     * @param context
     * @return
     */
    private static long mExitTime;
    public static boolean doubleClickExit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            mExitTime = System.currentTimeMillis();
            TipUtil.newThreadToast("再按一次退出");
            return false;
        }
        return true;
    }

    /**
     * 避免快速重复点击
     * @return
     */
    private static long lastClickTime = 0;//上次点击的时间
    private static int spaceTime = 1000;//时间间隔
    public static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();//当前系统时间
        boolean isAllowClick;//是否允许点击
        if (currentTime - lastClickTime > spaceTime) {
            isAllowClick = true;
        } else {
            isAllowClick = false;
        }
        lastClickTime = currentTime;
        return isAllowClick;
    }

    //--------------------------MD5加密-------------------------------
    /**
     * 生成MD5加密32位字符串
     *
     * @param MStr :需要加密的字符串
     * @return
     */
    public static String Md5(String MStr) {
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(MStr.getBytes());
            return bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(MStr.hashCode());
        }
    }

    /**
     * MD5内部算法-------不能修改!
     * @param bytes
     * @return
     */
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    //--------------------------MD5加密-------------------------------

    /**
     * 倒计时
     *
     * @param textView 控件
     * @param waitTime 倒计时总时长
     * @param interval 倒计时的间隔时间
     */
    public static void countDown(final TextView textView, long waitTime, long interval) {
        textView.setEnabled(false);
        android.os.CountDownTimer timer = new android.os.CountDownTimer(waitTime, interval) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                textView.setText(millisUntilFinished / 1000+"s");
            }

            @Override
            public void onFinish() {
                textView.setEnabled(true);
                textView.setText("重新获取");
            }
        };
        timer.start();
    }

//--------------------------处理EditText显示隐藏输入法-------------------------------

    /**
     * 处理EditText显示隐藏输入法
     * 点击EditText显示输入法  点击其他View隐藏输入法
     * @param activity
     * @param event
     */
    public static void dispatchEditText(Activity activity, MotionEvent event) {
        View v = activity.getCurrentFocus();
        if (event.getAction() == MotionEvent.ACTION_DOWN && isShouldHideKeyboard(v, event)) {
            hideSoftInput(v);
        }
    }

    /**
     * 是否隐藏软键盘
     *
     * @param v
     * @param event
     * @return
     */
    public static boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    /**
     * 隐藏输入法
     *
     * @param v
     */
    public static void hideSoftInput(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    //--------------------------处理EditText显示隐藏输入法-------------------------------

    /**
     * 字符串判空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 数组判空
     *
     * @param list
     * @return
     */
    public static boolean isEmpty(List list) {
        return list == null || list.size() == 0;
    }

    /**
     * 对象判空
     *
     * @param ob
     * @return
     */
    public static boolean isEmpty(Object ob) {
        return ob == null || isEmpty(ob.toString());
    }

    public static String getError(int code, String msg) {
        return msg + "：" + code;
    }

    public static int getColor(int color) {
        return LoadingApp.getContext().getResources().getColor(color);
    }

    public static String getString(int res) {
        return LoadingApp.getContext().getString(res);
    }
}
