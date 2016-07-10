package au.com.thoughtpatterns.djs.tag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Resources;
import au.com.thoughtpatterns.core.util.Util;
import au.com.thoughtpatterns.djs.util.RecordingDate;

public class Lltag implements ITag {

	private static final Logger log = Logger.get(Lltag.class);

	private static final String CMD = "lltag";

	static class Entry {
		String key;
		String value;

		public String toString() {
			return key + "=" + value;
		}
	}

	private List<Entry> entries = new ArrayList<>();

	private List<Entry> updated = new ArrayList<>();

	private File file;

	private String title;

	private String artist;

	private String album;

	private String comment;

	private String genre;

	private RecordingDate year;

	private int track;

	private double rating;

	private Double bpm;
	
	private String rgGain;
	
	private String rgPeak;

	private boolean toUpperCase = false;

	public Lltag(File aFile, boolean aToUpperCase) throws IOException {
		this(aFile);
		toUpperCase = true;
	}

	public Lltag(File aFile) throws IOException {
        file = aFile;

        List<String> cmd = new ArrayList<>();

        cmd.add(CMD);
        cmd.add("-S");
        cmd.add(file.getAbsolutePath());

        log.debug("execute " + Util.join(" ", cmd));
        
        ProcessBuilder b = new ProcessBuilder(cmd);

        Process p = b.start();

        InputStream in = p.getInputStream();
        InputStream err = p.getErrorStream();

        try {
            int result = p.waitFor();
            
            byte[] bytes = Resources.readByteArray(in);
            String input = new String(bytes);
            
            log.info("lltag stdout: " + input);

            if (result != 0) {
                String problem = new String(Resources.readByteArray(err));
                
                log.error("lltag stderr: " + problem);
                
                throw new IOException(file + ": lltag return value " + result);
            }


            String[] lines = input.split("\\n");

            Set<String> keys = new HashSet<>();
            
            for (String line : lines) {
                line = line.trim();
                int index = line.indexOf('=');
                if (index > 0 && ! line.startsWith("/")) {
                    String key = line.substring(0, index);
                    String value = line.substring(index + 1);

                    Entry e = new Entry();
                    e.key = key;
                    e.value = value;
                    
                    entries.add(e);
                    
                    if (keys.contains(key)) {
                        continue;
                    }
                    
                    keys.add(key);
                    
                    switch (key.toUpperCase()) {
                    case "ARTIST":
                        setArtist(value);
                        break;
                    case "TITLE":
                        setTitle(value);
                        break;
                    case "ALBUM":
                        setAlbum(value);
                        break;
                    case "NUMBER":
                        setTrack(Integer.parseInt(value));
                        break;
                    case "GENRE":
                        setGenre(value);
                        break;
                    case "DATE":
                        try {
                            setYear(RecordingDate.parse(value));
                        } catch (Exception ex) {
                        }
                        break;
                    case "FMPS_RATING":
                        try {
                            setRating(Double.parseDouble(value));
                        } catch (Exception ex) {
                            setRating(-1);
                        }
                        break;
                    case "BPM":
                		Double z = null;
                		if (value != null && ! "".equals(value)) {
                        	try {
                        		z = Double.parseDouble(value);
                        		if (z <= 0) {
                        			z = null;
                        		}
                        	} catch (Exception ex) {}
                		}
                		setBPM(z);
                		break;
                    	
                    case "DESCRIPTION":
                        setComment(value);
                        break;
                    case "REPLAYGAIN_TRACK_GAIN":
                    	setRGGain(value);
                    	break;
                    case "REPLAYGAIN_TRACK_PEAK":
                    	setRGPeak(value);
                    	break;
                    	
                    default:
                        break;
                    }

                }
            }

        } catch (InterruptedException ex) {
            throw new IOException(file + ": lltag interrupted");
        } finally {
            in.close();
            err.close();
            p.destroy();
        }
    }

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getArtist() {
		return artist;
	}

	@Override
	public String getAlbum() {
		return album;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public String getGenre() {
		return genre;
	}

	@Override
	public RecordingDate getYear() {
		return year;
	}

	@Override
	public int getTrack() {
		return track;
	}

	@Override
	public double getRating() {
		return rating;
	}

	@Override
	public void setTitle(String aTitle) {
		title = aTitle;
	}

	@Override
	public void setArtist(String aArtist) {
		artist = aArtist;
	}

	@Override
	public void setAlbum(String aAlbum) {
		album = aAlbum;
	}

	@Override
	public void setComment(String aComment) {
		comment = aComment;
	}

	@Override
	public void setGenre(String aGenre) {
		genre = aGenre;
	}

	@Override
	public void setYear(RecordingDate aYear) {
		year = aYear;
	}

	@Override
	public void setTrack(int aTrack) {
		track = aTrack;
	}

	@Override
	public void setRating(double aRating) {
		rating = aRating;
	}
	
	@Override
	public Double getBPM() {
		return bpm;
	}

	@Override
	public void setBPM(Double aBpm) {
		bpm = aBpm;
	}

	@Override
	public String getRGGain() {
		return rgGain;
	}

	@Override
	public String getRGPeak() {
		return rgPeak;
	}

	@Override
	public void setRGGain(String g) {
		rgGain = g;
	}

	@Override
	public void setRGPeak(String p) {
		rgPeak = p;
	}

	private void update(String key, Object value0) {
		if (value0 == null) {
			return;
		}
		String value = value0.toString();
		for (Entry e : entries) {
			if (e.key.toUpperCase().equals(key.toUpperCase())) {
				if (Util.equals(e.value, value)) {
					return;
				}
				e.value = value;
				updated.add(e);
				return;
			}
		}
		Entry e = new Entry();
		e.key = key;
		e.value = value;
		entries.add(0, e);
		updated.add(e);
	}

	@Override
	public void write() throws IOException {

		List<String> cmd = createCmd();

		log.debug("execute " + Util.join(" ", cmd));

		ProcessBuilder b = new ProcessBuilder(cmd);

		Process p = b.start();

		try {
			int result = p.waitFor();
			if (result != 0) {
				throw new IOException(file + ": lltag return value " + result);
			}

			file.getParentFile().setLastModified(System.currentTimeMillis());

		} catch (InterruptedException ex) {
			throw new IOException(file + ": lltag interrupted");
		}
	}

	public String getSignature() {
		List<String> cmd = createCmd();
		return Util.join(" ", cmd);
	}

	public void copyFrom(Lltag other) {
		setTitle(other.getTitle());
		setAlbum(other.getAlbum());
		setTrack(other.getTrack());
		setArtist(other.getArtist());
		setGenre(other.getGenre());
		setYear(other.getYear());
		setRating(other.getRating());
		setComment(other.getComment());
	}

	private List<String> createCmd() {
		List<String> cmd = new ArrayList<>();

		cmd.add(CMD);
		cmd.add("--yes");

		update("TITLE", getTitle());
		update("ALBUM", getAlbum());
		update("NUMBER", getTrack());
		update("ARTIST", getArtist());
		update("GENRE", getGenre());
		update("DATE", getYear());
		update("FMPS_RATING", getRating() != -1 ? "" + getRating() : null);
		update("DESCRIPTION", getComment());
		if (getBPM() != null) {
			update("BPM", getBPM().toString());
		}

		cmd.add("--clear");

		for (Entry e : entries) {
			if (e.value == null) {
				continue;
			}

			cmd.add("--tag");
			String key = e.key;
			if (toUpperCase) {
				key = key.toUpperCase();
			}
			cmd.add(key + "=" + e.value);
		}

		cmd.add(file.getAbsolutePath());

		return cmd;
	}

}
