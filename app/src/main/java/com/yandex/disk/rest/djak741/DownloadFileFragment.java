

package com.yandex.disk.rest.djak741;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.http.HttpCodeException;

import java.io.File;
import java.io.IOException;

public class DownloadFileFragment extends IODialogFragment {

    private static final String TAG = "LoadFileFragment";

    private static final String WORK_FRAGMENT_TAG = "LoadFileFragment.Background";

    private static final String FILE_ITEM = "file.item";

    private static final int PROGRESS_DIV = 1024 * 1024;

    private Credentials credentials;
    private ListItem item;

    private DownloadFileRetainedFragment workFragment;

    public static DownloadFileFragment newInstance(Credentials credentials, ListItem item) {
        DownloadFileFragment fragment = new DownloadFileFragment();

        Bundle args = new Bundle();
        args.putParcelable(CREDENTIALS, credentials);
        args.putParcelable(FILE_ITEM, item);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentials = getArguments().getParcelable(CREDENTIALS);
        item = getArguments().getParcelable(FILE_ITEM);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fragmentManager = getFragmentManager();
        workFragment = (DownloadFileRetainedFragment) fragmentManager.findFragmentByTag(WORK_FRAGMENT_TAG);
        if (workFragment == null || workFragment.getTargetFragment() == null) {
            workFragment = new DownloadFileRetainedFragment();
            fragmentManager.beginTransaction().add(workFragment, WORK_FRAGMENT_TAG).commit();
            workFragment.loadFile(getActivity(), credentials, item);
        }
        workFragment.setTargetFragment(this, 0);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (workFragment != null) {
            workFragment.setTargetFragment(null, 0);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.example_loading_file_title);
        dialog.setMessage(item.getName());
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(true);
        dialog.setButton(ProgressDialog.BUTTON_NEUTRAL, getString(R.string.example_loading_file_cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onCancel();
            }
        });
        dialog.setOnCancelListener(this);
        dialog.show();
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        onCancel();
    }

    private void onCancel() {
        workFragment.cancelDownload();
    }

    public void onDownloadComplete(File file) {
        dialog.dismiss();
        makeWorldReadableAndOpenFile(file);
    }

    private void makeWorldReadableAndOpenFile(File file) {
        file.setReadable(true, false);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), item.getContentType());
        startActivity(Intent.createChooser(intent, getText(R.string.example_loading_file_chooser_title)));
    }

    public void setDownloadProgress(long loaded, long total) {
        if (dialog != null) {
            if (dialog.isIndeterminate()) {
                dialog.setIndeterminate(false);
            }
            if (total > Integer.MAX_VALUE) {
                dialog.setProgress((int)(loaded / PROGRESS_DIV));
                dialog.setMax((int)(total / PROGRESS_DIV));
            } else {
                dialog.setProgress((int)loaded);
                dialog.setMax((int)total);
            }
        }
    }

    public static class DownloadFileRetainedFragment extends IODialogRetainedFragment implements ProgressListener {

        private boolean cancelled;
        private File result;

        public void loadFile(final Context context, final Credentials credentials, final ListItem item) {
            result = new File("/storage/emulated/0/"+item.getName());

            new Thread(new Runnable() {
                @Override
                public void run () {
                    try {
                        RestClient client = RestClientUtil.getInstance(credentials);
                        client.downloadFile(item.getPath(), result, DownloadFileRetainedFragment.this);
                        downloadComplete();
                    } catch (HttpCodeException ex) {
                        Log.d(TAG, "loadFile", ex);
                        sendException(ex.getResponse().getDescription());
                    } catch (IOException | ServerException ex) {
                        Log.d(TAG, "loadFile", ex);
                        sendException(ex);
                    }
                }
            }).start();
        }

        @Override
        public void updateProgress (final long loaded, final long total) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    DownloadFileFragment targetFragment = (DownloadFileFragment) getTargetFragment();
                    if (targetFragment != null) {
                        targetFragment.setDownloadProgress(loaded, total);
                    }
                }
            });
        }

        @Override
        public boolean hasCancelled () {
            return cancelled;
        }

        public void downloadComplete() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    DownloadFileFragment targetFragment = (DownloadFileFragment) getTargetFragment();
                    if (targetFragment != null) {
                        targetFragment.onDownloadComplete(result);
                    }
                }
            });
        }

        public void cancelDownload() {
            cancelled = true;
        }
    }
}
