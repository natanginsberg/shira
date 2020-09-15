package com.function.karaoke.hardware;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class DialogBox extends DialogFragment {

    CallbackListener callbackListener;

    private final int BACK_CODE = 101;
    private final int INTERNET_CODE = 102;

    public DialogBox() {
    }

    public DialogBox(CallbackListener cl) {
        this.callbackListener = cl;
    }

    public static DialogBox newInstance(CallbackListener cl, int code) {
        DialogBox frag = new DialogBox(cl);
        Bundle args = new Bundle();
        args.putInt("code", code);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (getArguments().getInt("code") == BACK_CODE) {
            builder.setMessage(R.string.back_to_home_dialog)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            callbackListener.callback("yes");
//                        Intent intent = new Intent(getActivity(), SongsActivity.class);
//                        getActivity().finish();
//                        startActivity(intent);

                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            callbackListener.callback("no");
                        }
                    });
        } else {
            builder.setMessage(R.string.no_internet_alert)
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            callbackListener.callback("ok");
                        }
                    });
        }
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public interface CallbackListener {
        public void callback(String result);
    }
}
