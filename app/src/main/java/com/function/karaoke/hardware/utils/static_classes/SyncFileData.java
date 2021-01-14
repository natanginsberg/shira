package com.function.karaoke.hardware.utils.static_classes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.List;

public class SyncFileData {

    public static File parseVideo(File mFilePath, File file) throws IOException {
        boolean isError = false;
        DataSource channel = new FileDataSourceImpl(mFilePath.getAbsolutePath());
        IsoFile isoFile = new IsoFile(channel);
        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        for (TrackBox trackBox : trackBoxes) {
            TimeToSampleBox.Entry firstEntry = trackBox.getMediaBox().getMediaInformationBox().getSampleTableBox().getTimeToSampleBox().getEntries().get(1);
            if (firstEntry.getDelta() > 5000) {
                isError = true;
                firstEntry.setDelta(3000);
            }
        }
        if (file != null) {
            String filePath = file.getAbsolutePath();
            if (isError) {
                Movie movie = new Movie();
                for (TrackBox trackBox : trackBoxes) {
                    movie.addTrack(new Mp4TrackImpl(channel.toString() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox));
                }
                movie.setMatrix(isoFile.getMovieBox().getMovieHeaderBox().getMatrix());
                Container out = new DefaultMp4Builder().build(movie);

                //delete file first!
                FileChannel fc = new RandomAccessFile(filePath, "rw").getChannel();
                out.writeContainer(fc);
                fc.close();
//                isFileInSync(file);
                return file;
            }
        }
        return mFilePath;
    }

    private static void isFileInSync(File mFilePath) throws IOException {
        DataSource channel = new FileDataSourceImpl(mFilePath.getAbsolutePath());
        IsoFile isoFile = new IsoFile(channel);
        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        for (TrackBox trackBox : trackBoxes) {
            TimeToSampleBox.Entry firstEntry = trackBox.getMediaBox().getMediaInformationBox().getSampleTableBox().getTimeToSampleBox().getEntries().get(0);
            if (firstEntry.getDelta() > 10000) {
//                throw new RuntimeException("the song is not in sync");
//                firstEntry.setDelta(3000);
//            }
            }
        }
    }
}
