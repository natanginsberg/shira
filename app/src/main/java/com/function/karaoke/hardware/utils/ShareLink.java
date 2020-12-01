package com.function.karaoke.hardware.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class ShareLink {

    public static void shareLink(Recording recording, Activity activity){

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
//                setLongLink(Uri.parse("https://singJewish.page.link/?link=https://www.example.com/&recId=" + recordingId + "&uid=" + authenticationDriver.getUserUid() + "&delay=" + delay))
                .setLink(Uri.parse("https://www.example.com/?recId=" + recording.getRecordingId() + "&uid=" + recording.getRecorderId() + "&delay=" + recording.getDelay()))
                .setDomainUriPrefix("https://singjewish.page.link")
                // Set parameters
                // ...
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            String link = shortLink.toString();
                            sendDataThroughIntent(link, activity);


                        } else {
                            throw new IndexOutOfBoundsException();
                            // Error
                            // ...
                        }
                    }
                });
    }

    private static void sendDataThroughIntent(String link, Activity activity) {
        String data = "Listen to me sing\n" + link;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(
                Intent.EXTRA_TEXT, data);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }
}
