package au.com.thoughtpatterns.dj.disco.tangoinfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Album {

    private String tin;
    
    private Set<Track> trackSet = new HashSet<>();
    
    private List<List<Track>> tracks = new ArrayList<>();
    
    public Album(String aTin) {
        tin = aTin;
    }
    
    public String getTin() {
        return tin;
    }
    
    //1-based
    public List<Track> getTracks(int side) {
        return tracks.get(side - 1);
    }
    
    public Track getTrack(int side, int track) {
        List<Track> tmp = getTracks(side);
        if (tmp.size() <= track) {
            return null;
        }
        return tmp.get(track);
    }
    
    public int getSides() {
        return tracks.size();
    }
    
    public Set<Track> getAllTracks() {
        return trackSet;
    }
    
    void addTrack(Track t, String tin, int side, int track) {
        
        // TODO side support
        side = 1;
        
        while (tracks.size() < side) {
            tracks.add(new ArrayList<Track>());
        }
        
        List<Track> sideTracks = getTracks(side);
        
        while (sideTracks.size() <= track) {
            sideTracks.add(null);
        }
        sideTracks.set(track, t);
        
        trackSet.add(t);
    }
    
    public int hashCode() {
        return tin.hashCode();
    }
    
    public boolean equals(Object other) {
        if (!(other instanceof Album)) {
            return false;
        }
        return ((Album)other).tin.equals(tin);
    }
    
    public String toString() {
        return tin;
    }
    
}
