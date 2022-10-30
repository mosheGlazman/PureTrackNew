package com.supercom.puretrack.ui.call;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.supercom.puretrack.util.application.App;

public abstract class BaseCallView {

    private WindowManager windowManager;
    private LayoutParams createRingingViewParams;

    public void createCallView() {
        createRingingViewParams = new WindowManager.LayoutParams();
        createRingingViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        createRingingViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

                // this is to enable the notification to recieve touch
                // events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        createRingingViewParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        createRingingViewParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        initView();

        windowManager = ((WindowManager) App.getContext().getSystemService(Context.WINDOW_SERVICE));
        windowManager.addView(getView(), createRingingViewParams);
    }

    protected abstract void initView();

    protected abstract View getView();

    public void removeCallView() {

        if (getView() != null) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (getView().isAttachedToWindow()) {
                        windowManager.removeView(getView());
                    }
                }
            }, 5000);
        }

    }

}
