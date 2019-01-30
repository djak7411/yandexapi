
package com.yandex.disk.rest.djak741;

import android.app.ProgressDialog;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class IODialogFragment extends DialogFragment {

    protected static final String CREDENTIALS = "credentials";

    protected ProgressDialog dialog;

    public void sendException(final String message) {
        dialog.dismiss();
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
