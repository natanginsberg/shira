package com.function.karaoke.interaction.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.function.karaoke.interaction.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShowPrivacyPolicy {

    public static void showPolicy(Context context, View view, AgreeListener agreeListener) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout viewGroup = view.findViewById(R.id.policy_alert);
        View popupView = layoutInflater.inflate(R.layout.privacy_policy_alert, viewGroup);
        addWordsToPopup((TextView) popupView.findViewById(R.id.policy_words), context);

        PopupWindow popupWindow = placeIndicationOnScreen(context, popupView, view);
        popupView.findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    createEmptyFileForPolicy(context.getCacheDir());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                agreeListener.agreed(context);
                popupWindow.dismiss();
            }
        });
    }

    private static void addWordsToPopup(TextView view, Context context) {

        StringBuilder text = new StringBuilder();
        text.append('\n');
        text.append('\n');
        text.append('\n');
        try {
            BufferedReader
                    reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("PrivacyPolicy")));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                text.append(mLine);
                text.append('\n');
            }
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("Contract")));
            while ((mLine = reader.readLine()) != null) {
                text.append(mLine);
                text.append('\n');
            }
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');
            text.append('\n');

        } catch (IOException e) {
            Toast.makeText(context, "Error reading file!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            //log the exception

            view.setText((CharSequence) text);

        }

    }

    private static PopupWindow placeIndicationOnScreen(Context context, View popupView, View view) {
        PopupWindow popup = new PopupWindow(context);
        setSharePopupAttributes(context, popup, popupView, view);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        return popup;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setSharePopupAttributes(Context context, PopupWindow popup, View layout, View view) {
        int width = (int) (view.getWidth());
        int height = (int) (view.getHeight());
        popup.setContentView(layout);
        popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.unclicked_recording_background));
        popup.setWidth(width);
        popup.setHeight(height);
    }


    private static void createEmptyFileForPolicy(File folder) throws IOException {
        File artistFile = new File(folder, "policy1.txt");
        FileWriter writer = new FileWriter(artistFile);
        writer.write("32");
        writer.close();
    }

    public interface AgreeListener {
        void agreed(Context context);
    }
}
