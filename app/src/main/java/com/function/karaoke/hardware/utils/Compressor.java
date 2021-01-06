package com.function.karaoke.hardware.utils;

import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;

public class Compressor {

    public static void compress(String filePath, String destPath){
        VideoCompressor.start(filePath, destPath, new CompressionListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String s) {

            }

            @Override
            public void onProgress(float v) {

            }

            @Override
            public void onCancelled() {

            }
        }, VideoQuality.HIGH);
    }
}
