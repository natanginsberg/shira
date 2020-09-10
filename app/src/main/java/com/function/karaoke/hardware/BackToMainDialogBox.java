package com.function.karaoke.hardware;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class BackToMainDialogBox extends DialogFragment {

    CallbackListener callbackListener;

    public BackToMainDialogBox(CallbackListener cl){
        this.callbackListener = cl;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public interface CallbackListener {
        public void callback(String result);
    }
}
