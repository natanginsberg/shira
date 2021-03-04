package com.function.karaoke.interaction.utils.static_classes;

import android.net.Uri;

import com.function.karaoke.interaction.activities.Model.Recording;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class ShareLink {

    public static Task<ShortDynamicLink> createLink(Recording recording, String password, boolean video) {


        String passwordText = "";
        if (password != null)
            passwordText = "&password=" + password;
        return FirebaseDynamicLinks.getInstance().createDynamicLink()
//                setLongLink(Uri.parse("https://singJewish.page.link/?link=https://www.example.com/&recId=" + recordingId + "&uid=" + authenticationDriver.getUserUid() + "&delay=" + delay))
                .setLink(Uri.parse("https://ashira-music.com/?recId=" + recording.getRecordingId() +
                        "&uid=" + recording.getRecorderId() +
                        "&delay=" + recording.getDelay() + "&cameraOn=" + (recording.isCameraOn() && video) +
                        "&length=" + recording.getLength() + passwordText + "&isi=com.function.karaoke.interaction"))
                .setDomainUriPrefix("https://singjewish.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.function.karaoke.interaction")
                        .build())

                // Set parameters
                // ...
                .buildShortDynamicLink();
    }

}
