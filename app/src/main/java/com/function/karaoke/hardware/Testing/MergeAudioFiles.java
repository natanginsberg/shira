package com.function.karaoke.hardware.Testing;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MergeAudioFiles {

    private short[] target;
    private List<String> urls = new ArrayList<>();
    private int idx = 0;

    public MergeAudioFiles(int length, String url1, String url2) {
        urls.add(url1);
        urls.add(url2);
        target = new short[length];
    }

    public void combineFiles() {
        for (String url : urls) {
            // see where we find a suitable audioTrack
            MediaExtractor extractor = new MediaExtractor();
            try {
                extractor.setDataSource(url);
            } catch (IOException e) {
                extractor.release();
                return;
            }

            int trackIndex = selectTrack(extractor, "audio/");
            if (trackIndex >= 0)
                extractor.selectTrack(trackIndex);


            String fileType = typeForFile(url);
            if (fileType == null) {
//            out.release();
                extractor.release();
                return;
            }

            MediaCodec codec = null;
            try {
                codec = MediaCodec.createDecoderByType("audio/mpeg");
            } catch (IOException e) {
                e.printStackTrace();
            }
            MediaFormat wantedFormat = extractor.getTrackFormat(0);
            codec.configure(wantedFormat, null, null, 0);
            codec.start();

            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();

            // Allocate our own buffer
            int maximumBufferSizeBytes = 0;
            for (ByteBuffer bb : outputBuffers) {
                int c = bb.capacity();
                if (c > maximumBufferSizeBytes) maximumBufferSizeBytes = c;
            }
            setupBufferSizes(maximumBufferSizeBytes / 4);

            final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            MediaFormat format = null;
            while (true) {
                long timeoutUs = 1000000;
                int inputBufferIndex = codec.dequeueInputBuffer(timeoutUs);
                if (inputBufferIndex >= 0) {
                    ByteBuffer targetBuffer = inputBuffers[inputBufferIndex];
                    int read = extractor.readSampleData(targetBuffer, 0);
                    int flags = extractor.getSampleFlags();
                    if (read > 0)
                        codec.queueInputBuffer(inputBufferIndex, 0, read, 0, flags);
                    else
                        codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    extractor.advance();
                }

                int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs);
                if (outputBufferIndex >= 0) {
                    final boolean last = bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM;

                    int s = bufferInfo.size / 4;
                    ByteBuffer bytes = outputBuffers[outputBufferIndex];
                    short[] shorts = new short[s * 2];
                    ((ByteBuffer) bytes.position(bufferInfo.offset)).asShortBuffer().get(shorts, 0, s * 2);
                    process(shorts, s * 2);

                    codec.releaseOutputBuffer(outputBufferIndex, false);
                    if (last)
                        break;
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    outputBuffers = codec.getOutputBuffers();
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    format = codec.getOutputFormat();
                }
            }

            extractor.release();
            codec.stop();
            codec.release();
        }
    }

    private void setupBufferSizes(int i) {
    }

    private String typeForFile(String url) {
//        for (int i = url.length() - 1; i >= 0; i++) {
//            if (url.charAt(i) == '.')
//                return url.substring(i);
//        }
//        return null;
        return "audio/mpeg";
    }


    private void process(short[] audio, int l) {
        for (int i = 0; i < l; i++)
            target[idx++] += audio[i] / 2;
    }

    /**
     * search first track index matched specific MIME
     *
     * @param extractor
     * @param mimeType  "video/" or "audio/"
     * @return track index, -1 if not found
     */
    protected static final int selectTrack(final MediaExtractor extractor, final String mimeType) {
        final int numTracks = extractor.getTrackCount();
        MediaFormat format;
        String mime;
        for (int i = 0; i < numTracks; i++) {
            format = extractor.getTrackFormat(i);
            mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(mimeType)) {
                return i;
            }
        }
        return -1;
    }
}
