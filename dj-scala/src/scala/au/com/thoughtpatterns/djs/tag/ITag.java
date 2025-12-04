package au.com.thoughtpatterns.djs.tag;

import java.io.IOException;

import au.com.thoughtpatterns.djs.util.RecordingDate;

public interface ITag {

    String getTitle();
    String getArtist();
    String getAlbum();
    String getComment();
    String getGenre();
    Double getBPM();
    String getRGGain();
    String getRGPeak();
    Double getTuning();
    String getComposer();
    String getGroup();
    
    RecordingDate getYear();
    int getTrack();

    double getRating();
    
    void setTitle(String title);
    void setArtist(String artist);
    void setAlbum(String album);
    void setComment(String comment);
    void setGenre(String genre);
    void setYear(RecordingDate year);
    void setTrack(int track);
    void setRating(double rating);
    void setBPM(Double bpm);
    void setRGGain(String g);
    void setRGPeak(String p);
    void setComposer(String composer);
    void setTuning(Double cents);
    void setGroup(String group);
    
    String getSignature();
    
    void write() throws IOException;
}
