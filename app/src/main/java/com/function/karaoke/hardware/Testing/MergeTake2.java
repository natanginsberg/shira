package com.function.karaoke.hardware.Testing;

import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.function.karaoke.hardware.storage.DatabaseDriver;
import com.function.karaoke.hardware.storage.StorageAdder;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MergeTake2 extends AppCompatActivity implements Serializable {

    private List<String> urls = new ArrayList<>();
    private int recorderSamplerate;
    DatabaseDriver databaseDriver = new DatabaseDriver();
    long[] sizes;
    StorageAdder storageAdder = new StorageAdder();
    //    int prepared = 0;
    String mAudioUrl;
    private File mFolder;
    private String fileName;

    public MergeTake2(List<String> urls, long[] sizes) {
        this.urls = urls;
        this.sizes = sizes;
    }

    public void SSoftAudCombine() {
        combine1();
    }

    private void combine1() {
        try {

            InputStream is = new URL(urls.get(0)).openStream();
            InputStream is2 = new URL(urls.get(1)).openStream();
            SequenceInputStream sis = new SequenceInputStream(is, is2);

            createVideoFolder();
            createVideoFileName();
            FileOutputStream fos = new FileOutputStream(fileName);

            int temp;

            try {
                while ((temp = sis.read()) != -1) {

                    fos.write(temp);

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getParent(), "finished", Toast.LENGTH_SHORT);
    }



//    private void getAudioSizes(String url, int i) {
//        final Observer<Long> searchObserver = size -> {
//            if (sizes[i] != 0) {
//                sizes[i] = (size - 44) / 2;
//                prepared++;
//            }
//            if (prepared == 2) {
//                combine();
//            }
//        };
//        databaseDriver.getStorageReferenceSize(url).observe(this, searchObserver);
//    }


//    private class CreateObserver implements LifecycleObserver {
//        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//        public void connectListener() {
//            sizes[i] = (size - 44) / 2;
//        }
//    }

    private void combine() {
        try {

            DataInputStream[] mergeFilesStream = new DataInputStream[urls.size()];

            for (int i = 0; i < urls.size(); i++) {
                URLConnection is = new URL(urls.get(i)).openConnection();
                mergeFilesStream[i] = new DataInputStream(is.getInputStream());

                if (i == urls.size() - 1) {
                    mergeFilesStream[i].skip(24);
                    byte[] sampleRt = new byte[4];
                    mergeFilesStream[i].read(sampleRt);
                    ByteBuffer bbInt = ByteBuffer.wrap(sampleRt).order(ByteOrder.LITTLE_ENDIAN);
                    recorderSamplerate = bbInt.getInt();
                    mergeFilesStream[i].skip(16);
                } else {
                    mergeFilesStream[i].skip(44);
                }

            }
            createOutputFile();

            DataOutputStream amplifyOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
            for (int b = 0; b < urls.size(); b++) {
                for (int i = 0; i < (int) sizes[b]; i++) {
                    byte[] dataBytes = new byte[2];
                    try {
                        dataBytes[0] = mergeFilesStream[b].readByte();
                        dataBytes[1] = mergeFilesStream[b].readByte();
                    } catch (EOFException e) {
                        amplifyOutputStream.close();
                        e.printStackTrace();
                    }
                    short dataInShort = ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    float dataInFloat = (float) dataInShort / 37268.0f;


                    short outputSample = (short) (dataInFloat * 37268.0f);
                    byte[] dataFin = new byte[2];
                    dataFin[0] = (byte) (outputSample & 0xff);
                    dataFin[1] = (byte) ((outputSample >> 8) & 0xff);
//                    storageAdder.uploadAudio(dataFin);
                    amplifyOutputStream.write(dataFin, 0, 2);
//                    uploadAudio(dataFin);
                }
            }
            amplifyOutputStream.close();
            for (int i = 0; i < urls.size(); i++) {
                mergeFilesStream[i].close();
            }
            secondPart();

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void createOutputFile() {
        createVideoFolder();
        try {
            createVideoFileName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createVideoFolder() {
//        File moviewFile = Environment.getDataDirectory();
        File moviewFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        mFolder = new File(moviewFile, "mergedAudio");
        if (!mFolder.exists()) {
            mFolder.mkdirs();
        }
    }

    private void createVideoFileName() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMM_HHmmss").format(new Date());
        String prepend = "VIDEO" + timeStamp + "_";
        File videoFile = File.createTempFile(prepend, ".wav", mFolder);
        fileName = videoFile.getAbsolutePath();
        return;
    }

    private void secondPart() {

        long size = 0;
        try {
            FileInputStream fileSize = new FileInputStream(fileName);
            size = fileSize.getChannel().size();
            fileSize.close();
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        final int RECORDER_BPP = 16;

        long datasize = size + 36;
        long byteRate = (RECORDER_BPP * recorderSamplerate) / 8;
        long longSampleRate = recorderSamplerate;
        byte[] header = new byte[44];


        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (datasize & 0xff);
        header[5] = (byte) ((datasize >> 8) & 0xff);
        header[6] = (byte) ((datasize >> 16) & 0xff);
        header[7] = (byte) ((datasize >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) 1;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) ((RECORDER_BPP) / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (size & 0xff);
        header[41] = (byte) ((size >> 8) & 0xff);
        header[42] = (byte) ((size >> 16) & 0xff);
        header[43] = (byte) ((size >> 24) & 0xff);
        // out.write(header, 0, 44);

        try {
            RandomAccessFile rFile = new RandomAccessFile(fileName, "rw");
            rFile.seek(0);
            rFile.write(header);
            rFile.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    private void uploadAudio(byte[] bytes) {
//        final Observer<AudioUploaded> urlObserver = new Observer<AudioUploaded>() {
//            @Override
//            public void onChanged(AudioUploaded audioUploaded) {
//                secondPart(audioUploaded.getSize());
//            }
//        };
//        storageAdder.uploadAudio(bytes).observe(this, urlObserver);
//    }
}
