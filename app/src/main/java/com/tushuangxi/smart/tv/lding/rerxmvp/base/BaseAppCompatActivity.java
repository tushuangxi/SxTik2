package com.tushuangxi.smart.tv.lding.rerxmvp.base;

import android.app.Activity;
import android.content.Context;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.tushuangxi.smart.tv.R;
import com.tushuangxi.smart.tv.lding.eventbus.EventMessage;
import com.tushuangxi.smart.tv.lding.other.AppGlobalConsts;
import com.tushuangxi.smart.tv.lding.rerxmvp.service.navigationview.AbsMeanager;
import com.tushuangxi.smart.tv.lding.utils.StatusBarCompat;
import com.tushuangxi.smart.tv.lding.utils.network.NetworkManager;
import com.tushuangxi.smart.tv.lding.utils.network.NetworkObserver;
import com.tushuangxi.smart.tv.lding.widget.NoWorkDialog;
import com.tushuangxi.smart.tv.library.imageloaderfactory.cofig.MainActivity;
import com.tushuangxi.smart.tv.library.loading.conn.PreTaskManager;
import com.lky.toucheffectsmodule.factory.TouchEffectsFactory;
import com.lky.toucheffectsmodule.types.TouchEffectsWholeType;
import com.xiaomai.environmentswitcher.EnvironmentSwitcher;
import com.xiaomai.environmentswitcher.bean.EnvironmentBean;
import com.xiaomai.environmentswitcher.bean.ModuleBean;
import com.xiaomai.environmentswitcher.listener.OnEnvironmentChangeListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import butterknife.ButterKnife;

public abstract class BaseAppCompatActivity extends AppCompatActivity implements ActBaseView, View.OnClickListener, PreTaskManager.SwipeAction ,NetworkObserver, OnEnvironmentChangeListener {

    String TAG = BaseAppCompatActivity.class.getSimpleName()+"....";
    protected Context mContext;

    public enum TransitionMode {
        LEFT, RIGHT, TOP, BOTTOM, SCALE, FADE
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (toggleOverridePendingTransition()) {
            switch (getOverridePendingTransitionType()) {
                case LEFT:
                    overridePendingTransition(R.anim.left_in,R.anim.left_out);
                    break;
                case RIGHT:
                    overridePendingTransition(R.anim.right_in,R.anim.right_out);
                    break;
                case TOP:
                    overridePendingTransition(R.anim.top_in,R.anim.top_out);
                    break;
                case BOTTOM:
                    overridePendingTransition(R.anim.bottom_in,R.anim.bottom_out);
                    break;
                case SCALE:
                    overridePendingTransition(R.anim.scale_in,R.anim.scale_out);
                    break;
                case FADE:
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    break;
                default:
            }
        }
        //????????????  ??????????????? ??? AlertDialog ??????????????????????????????dialog ??????
        touchEffectsView();
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        initBundleData(bundle);
        doBeforeSetcontentView();
        if(getContentViewLayoutId()!= AppGlobalConsts.GLOBAL_ZERO){
            //??????????????????id
            setContentView(getContentViewLayoutId());
        }else{
            //??????templateUI??????
            setContentView(getContentViewLayoutView());
        }
        mContext=this;
        //???????????????  1. ??????android.support.v7.app.AppCompatActivity   ?????? if(){ getSupportActionBar().hide(); } ???????????????
        // 2.??????android.app.Activity??????android.support.v4.app.FragmentActivity  ?????? requestWindowFeature(Window.FEATURE_NO_TITLE);???????????????
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        ButterKnife.bind(this);

        initView();
        getHttpData(mContext);

        if(isBindEventBus()){
            EventBus.getDefault().register(this);
        }

        if (isResultOK()){
            setResult(Activity.RESULT_OK, null);
        }

        addOnClickListeners();
        setTranslucentStatus(isApplyStatusBarTranslucency());
        changeStatusBarTextColor(true);

        //???????????? ?????????
        NetworkManager.getInstance().initialized(this);
        NetworkManager.getInstance().registerNetworkObserver(this);

        EnvironmentSwitcher.addOnEnvironmentChangeListener(this);
    }

    @Override
    public void onEnvironmentChanged(ModuleBean module, EnvironmentBean oldEnvironment, EnvironmentBean newEnvironment) {
        Log.e(TAG, "Module=" + module.getName() + ",\nOldEnvironment=" + oldEnvironment.getName() + ",\noldUrl=" + oldEnvironment.getUrl()
                + ",\nnewEnvironment=" + newEnvironment.getName() + ",\nnewUrl=" + newEnvironment.getUrl());

        Toast.makeText(this, "Module=" + module.getName() + ",\nOldEnvironment=" + oldEnvironment.getName() + ",\noldUrl=" + oldEnvironment.getUrl()
                + ",\nnewEnvironment=" + newEnvironment.getName() + ",\nnewUrl=" + newEnvironment.getUrl(), Toast.LENGTH_SHORT).show();

        if (module.equals(EnvironmentSwitcher.MODULE_APP)) {
            // ???????????????????????????????????????????????? token??????????????? postDelay ?????????????????????????????????
            // if the request need token, you can send in postDelay.
            long delayTime = 1500;
//            findViewById(R.id.frame_layout).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    // ???????????? token ?????????????????????
//                    // send the request need token
//                    Log.e(TAG, "run: send request");
//
//                    Toast.makeText(MainActivity.this, "send request", Toast.LENGTH_SHORT).show();
//                }
//            }, delayTime);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBindEventBus()) {
            EventBus.getDefault().unregister(this);
        }
        EnvironmentSwitcher.removeOnEnvironmentChangeListener(this);
    }

    @Override
    public void onNetworkStateChanged(boolean networkConnected, NetworkInfo currentNetwork, NetworkInfo lastNetwork) {
        if(networkConnected) {
//            ViseLog.w(TAG,"????????????:" + (null == currentNetwork ? "" : ""+currentNetwork.getTypeName()+":"+currentNetwork.getState()));
//            TipUtil.newThreadToast("???????????????!");
        } else {
//            TipUtil.newThreadToast("???????????????!");
            NoWorkDialog csdf = NoWorkDialog.getInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            csdf.show(ft, "NoWorkDialog");
        }
//        ViseLog.w(TAG,null == currentNetwork ? "????????????:???????????????" : "????????????:"+currentNetwork.toString());
    }

    protected  void touchEffectsView(){
        //??????????????????
        TouchEffectsFactory.initTouchEffects(this,TouchEffectsWholeType.SCALE);
        //?????????????????????
//        TouchEffectsFactory.initTouchEffects(this, TouchEffectsWholeType.RIPPLE);
        //?????????1????????????
//        TouchEffectsFactory.initTouchEffects(this,TouchEffectsWholeType.RIPPLE_1);
        //??????????????????
//        TouchEffectsFactory.initTouchEffects(this,TouchEffectsWholeType.STATE);
    };

    protected void addOnClickListeners(@IdRes int... ids) {
        if (ids != null) {
            View viewId;
            for (int id : ids) {
                viewId = findViewById(id);
                if (viewId != null) {
                    viewId.setOnClickListener(this);
                }
            }
        }
    }

    /**
     * ?????????????????? Layout
     * */
    protected abstract int getContentViewLayoutId();

    /**
     * ??????templateUI?????? Layout
     * */
    protected abstract View getContentViewLayoutView();


    @Override
    protected void onStart() {
        if (isOpenFloatingAnimationService()) {
            AbsMeanager.getInstance().startFloatingAnimation();
        }else {
            AbsMeanager.getInstance().stopFloatingAnimation();
        }

        if (isOpenFloatingErWerMaService()) {
            AbsMeanager.getInstance().startErWerMaFloat();
        }else {
            AbsMeanager.getInstance().stopErWerMaFloat();
        }
        super.onStart();

    }


    @Override
    protected void onRestart() {
        super.onRestart();

    }

    //????????????  ????????????
    @Subscribe(threadMode= ThreadMode.MAIN, sticky=false)
    public void EventBusMessage(EventMessage eventMessage){
        switch (eventMessage.getCode()) {
//            case EventCode.SYN_CODE_CLOSE_DATA://
//
//                break;
//
//            case EventCode.SYN_CODE_CLOSE_CHECK_WORK_DATA://
//                if (!NetworkUtils.isConnected(LoadingApp.getContext())) {
//                    NoWorkDialog csdf = NoWorkDialog.getInstance();
//                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                    csdf.show(ft, "NoWorkDialog");
//                }else {
//                    onResume();
//                }

            default:
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);

    }
    @Override
    public void setContentView(View layoutResView) {
        super.setContentView(layoutResView);
        ButterKnife.bind(this);

    }
    /**
     * ??????layout?????????
     */
    private void doBeforeSetcontentView() {
        //????????????
//        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //??????????????????4.4??????4.4???????????????????????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //???????????????
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //???????????????
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * ??????????????????4.4?????????????????????
     */
    protected void SetStatusBarColor() {
//        StatusBarCompat.setStatusBarColor(this, ContextCompat.getColor(this, R.color.role_yellow_gray));

//        XStatusBar.setColor(this, getResources().getColor(R.color.colorPrimary),0);
    }
    /**
     * ??????????????????4.4?????????????????????
     */
    protected void SetStatusBarColor(int color) {
        StatusBarCompat.setStatusBarColor(this, color);
    }

    /**
     * ??????????????????
     */
    private long lastClick = 0;
    /**
     * ????????????????????????
     * @return {@code true}: ???<br>{@code false}: ???
     */
    public boolean isFastClick() {
        long now = System.currentTimeMillis();
        if (now - lastClick >= 1000) {
            lastClick = now;
            return false;
        }
        return true;
    }

    @Override
    public void onClick(final View view) {
        if (!isFastClick()){
            onWidgetClick(view);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (toggleOverridePendingTransition()) {
            switch (getOverridePendingTransitionType()) {
                case LEFT:
                    overridePendingTransition(R.anim.left_in,R.anim.left_out);
                    break;
                case RIGHT:
                    overridePendingTransition(R.anim.right_in,R.anim.right_out);
                    break;
                case TOP:
                    overridePendingTransition(R.anim.top_in,R.anim.top_out);
                    break;
                case BOTTOM:
                    overridePendingTransition(R.anim.bottom_in,R.anim.bottom_out);
                    break;
                case SCALE:
                    overridePendingTransition(R.anim.scale_in,R.anim.scale_out);
                    break;
                case FADE:
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    break;
            }
        }
    }


    /**
        ???????????????
     */
    protected void setTranslucentStatus(boolean on) {
        if (on) {
            // ?????????????????????
            SetStatusBarColor();
        }
    }
    /**
     * ??????????????????????????????
     *
     * @param isBlack
     */
    private void changeStatusBarTextColor(boolean isBlack) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (isBlack) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//???????????????????????????
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);//???????????????????????????
            }
        }
    }

    /**
     * activity????????????????????????????????????
     * */
    protected abstract boolean toggleOverridePendingTransition();

    /**
     * activity???????????????
     * */
    protected abstract TransitionMode getOverridePendingTransitionType();

    /**
     * ???????????????????????????
     *
     *  // ?????? ??????Activity???  isBindEventBus  true?????????  ??????Activity????????????????????????  ????????????
     *  Caused by: de.greenrobot.event.EventBusException: Subscriber cl    ProgramActivity and its super classes have no public methods with the @Subscribe annotation
     *      //????????????  ????????????
     *     @Subscribe(threadMode= ThreadMode.MAIN, sticky=true)
     *     public void myEventBusMessage(EventClientMessage eventMessage){
     *
     *     }
     * */
    protected abstract boolean isBindEventBus();

    /**
     * ???????????????????????????
     * */
    protected abstract boolean isApplyStatusBarTranslucency();

    /**
     * ??????????????????   setResult  ??? Result ?????????Ok  -1
     * */
    protected abstract boolean isResultOK();
    /**
     * ?????????????????? ?????????
     * */
    protected abstract boolean isOpenFloatingAnimationService();
    /**
     * ???????????? ???????????????
     * */
    protected abstract boolean isOpenFloatingErWerMaService();


}
