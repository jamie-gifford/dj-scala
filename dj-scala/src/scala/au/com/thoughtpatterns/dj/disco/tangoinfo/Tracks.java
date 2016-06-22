package au.com.thoughtpatterns.dj.disco.tangoinfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import au.com.thoughtpatterns.core.util.CsvUtils;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.djs.lib.Performance;

public class Tracks implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.get(Tracks.class);
    
    private static final String BASE_URL = "https://tango.info/tracks/";

    public static final Pattern TINT = Pattern.compile("([0-9]+)-([0-9]+)-([0-9]+)");
    

    // ------------------
    // Primary structure
    
    private List<Track> tracks = new ArrayList<>();
    
    // ------------------
    // Derived structures
    
    private Map<String, List<Track>> byTin = null;

    private List<Performance> songs = null;
    
    // ------------------
    
    public static Tracks loadFromWeb(File file) {
        
        Tracks perfs = new Tracks();
        
        for (char i = 'A'; i <= 'Z'; i++) {
            
            String letter = Character.toString(i);
            List<Track> letters = loadFromWeb(letter);
            
            log.info("Read " + letters.size() + " performances for " + letter);
            
            perfs.tracks.addAll(letters);
            
        }
        
        perfs.save(file);
        
        return perfs;
    }
    
    static List<Track> loadFromWeb(String letter) {

        List<Track> ps = new ArrayList<>();
        
        try {
            String url = BASE_URL;
            
            Connection conn = Jsoup.connect(url + letter);
            conn.maxBodySize(20 * 1000 * 1000);
            conn.timeout(60 * 1000);
            
            conn.method(Method.GET);
            
            Document doc = conn.get();

            // log.debug("Read " + doc);
            Elements elts = doc.select("table.listing tbody tr");
            
            log.debug("Got " + elts.size() + " rows for letter " + letter);

            for (int j = 0; j < elts.size(); j++) {
                
                Element elt = elts.get(j);
                Elements cells = elt.select("td");
                
                if (cells.size() != 9) {
                    log.error("unexpected size " + cells.size() + " on row " + elt.html());
                    continue;
                }
                
                Track p = new Track();
                int i = 1;
                p.title = cells.get(i++).text();
                p.tiwc = cells.get(i++).text();
                p.genre = cells.get(i++).text();
                p.orchestra = cells.get(i++).text();
                p.vocalists = cells.get(i++).text();
                p.perfDate = cells.get(i++).text();
                p.duration = cells.get(i++).text();
                p.tint = cells.get(i++).text();
                
                ps.add(p);
                
                // log.info(j + " of " + elts.size() + " : " + p.title);
                
            }

        } catch (Exception ex) {
            throw new SystemException(ex);
        }

        return ps;
    }

    public static Tracks load(File file) {

        try {
            CsvUtils csv = new CsvUtils();
            List<String[]> rows = csv.fromCsv(new FileReader(file));

            Tracks perfs = new Tracks();
            
            for (String[] row : rows) {
                Track p = new Track();
                int i = 0;
                p.title = row[i++];
                p.tiwc = row[i++];
                p.genre = row[i++];
                p.orchestra = row[i++];
                p.vocalists = row[i++];
                p.perfDate = row[i++];
                p.duration = row[i++];
                p.tint = row[i++];
                perfs.tracks.add(p);
            }
            
            return perfs;
            
        } catch (Exception ex) {
            throw new SystemException(ex);
        }

    }
    
    public void save(File file) {
        
        CsvUtils csv = new CsvUtils();
        
        String[][] data = new String[tracks.size()][];
        
        int i = 0;
        
        for (Track p : tracks) {
            
            String[] row = new String[] {
                    p.title,
                    p.tiwc,
                    p.genre,
                    p.orchestra,
                    p.vocalists,
                    p.perfDate,
                    p.duration,
                    p.tint
            };
            
            data[i++] = row;
        }
        
        csv.toCsv(data);
        String contents = csv.getFormattedString();
        
        try {
            FileWriter w = new FileWriter(file);
            w.write(contents);
            w.close();
        } catch (IOException ex) {
            throw new SystemException(ex);
        }
        
    }
    
    private List<Track> getByTin(String aTin) {
        
        if (byTin == null) {
            
            Pattern TINT = Pattern.compile("([0-9]+)-([0-9]+)-([0-9]+)");
            
            byTin = new HashMap<>();
            
            for (Track t : tracks) {
                
                String tint = t.tint;
                Matcher m = TINT.matcher(tint);
                if (m.matches()) {
                    String tin = m.group(1);
                    
                    if (! byTin.containsKey(tin)) {
                        byTin.put(tin, new ArrayList<Track>());
                    }

                    byTin.get(tin).add(t);
                }
            }
        }
        
        List<Track> l = byTin.get(aTin);
        if (l == null) {
            l = new ArrayList<>();
        }
        
        return l;
    }
    
    public List<Performance> tin(String tin) {
        
        List<Track> ts = getByTin(tin);
        
        List<Performance> songs = new ArrayList<>();
        
        for (Track t : ts) {
        	Performance s = t.toSong();
            songs.add(s);
        }
        
        return songs;
    }
    
    public List<Performance> toSongs() {
        
        if (songs == null) {

            Set<Performance> tmp = new HashSet<Performance>();
            
            for (Track t : tracks) {
            	Performance s = t.toSong();
                tmp.add(s);
            }
            
            songs = new ArrayList<>(tmp);

        }
        
        return songs;
    }
    
    public Collection<Album> getAlbums() {

        Map<String, Album> albums = new HashMap<>();
        
        for (Track t : tracks) {

            Matcher m = TINT.matcher(t.tint);
            if (! m.matches()) {
                continue;
            }
            String tin = m.group(1);
            int side = Integer.parseInt(m.group(2));
            int track = Integer.parseInt(m.group(3));

            Album a = albums.get(tin);
            if (albums.get(tin) == null) {
                a = new Album(tin);
                albums.put(tin, a);
            }
            
            a.addTrack(t, tin, side, track);
        }
        
        return albums.values();
    }

}
