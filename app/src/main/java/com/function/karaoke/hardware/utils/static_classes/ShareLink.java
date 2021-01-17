package com.function.karaoke.hardware.utils.static_classes;

import android.net.Uri;

import com.function.karaoke.hardware.activities.Model.Recording;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class ShareLink {

    public static Task<ShortDynamicLink> createLink(Recording recording) {

        return FirebaseDynamicLinks.getInstance().createDynamicLink()
//                setLongLink(Uri.parse("https://singJewish.page.link/?link=https://www.example.com/&recId=" + recordingId + "&uid=" + authenticationDriver.getUserUid() + "&delay=" + delay))
                .setLink(Uri.parse("https://ashira2020.wixsite.com/website/?recId=" + recording.getRecordingId() +
                        "&uid=" + recording.getRecorderId() +
                        "&delay=" + recording.getDelay() + "&cameraOn=" + recording.isCameraOn() +
                        "&length=" + recording.getLength()))
                .setDomainUriPrefix("https://singjewish.page.link")

                // Set parameters
                // ...
                .buildShortDynamicLink();
    }

}