

package com.yandex.disk.rest.djak741;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

abstract class ContextMenuDialogFragment extends DialogFragment {

    protected static final String CREDENTIALS = "credentials";
    protected static final String LIST_ITEM = "list.item";

    protected Credentials credentials;
    protected ListItem listItem;

    protected static <T extends ContextMenuDialogFragment> T newInstance(T fragment, Credentials credentials, ListItem listItem) {
        Bundle args = new Bundle();
        args.putParcelable(CREDENTIALS, credentials);
        args.putParcelable(LIST_ITEM, listItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentials = getArguments().getParcelable(CREDENTIALS);
        listItem = getArguments().getParcelable(LIST_ITEM);
    }
}
