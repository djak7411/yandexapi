

package com.yandex.disk.rest.djak741;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

public class IODialogRetainedFragment extends Fragment {

    protected Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        handler = new Handler();
    }

    protected void sendException(final Exception ex) {
        sendException(ex.getMessage());
    }

    protected void sendException(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run () {
                IODialogFragment targetFragment = (IODialogFragment) getTargetFragment();
                if (targetFragment != null) {
                    targetFragment.sendException(message);
                }
            }
        });
    }
}
