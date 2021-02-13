package com.function.karaoke.hardware;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class DialogBox extends DialogFragment {

    private static final int EARPHONES = 121;
    private static final int NO_AUDIO_CODE = 103;
    private final int BACK_CODE = 101;
    private final int INTERNET_CODE = 102;
    CallbackListener callbackListener;

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
        switch (getArguments().getInt("code")) {
            case BACK_CODE:
                builder.setMessage(R.string.back_to_home_dialog)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                callbackListener.callback("yes");
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                callbackListener.callback("no");
                            }
                        });
                break;
            case INTERNET_CODE:
                builder.setMessage(R.string.no_internet_alert)
                        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                callbackListener.callback("ok");
                            }
                        });
                break;
            case NO_AUDIO_CODE:
                builder.setMessage(R.string.no_audio_code)
                        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                callbackListener.callback("ok");
                            }
                        });
                break;
            case EARPHONES:
                builder.setMessage(R.string.attach_earphones)
                        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                callbackListener.callback("got it");
                            }
                        });
                break;
        }
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public interface CallbackListener {
        void callback(String result);
    }
}
