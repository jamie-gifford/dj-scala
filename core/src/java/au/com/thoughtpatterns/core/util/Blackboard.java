package au.com.thoughtpatterns.core.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a "shared blackboard" on which notes can be posted. This allows a simple sort of subscriber
 * pattern to be implemented.
 */
public class Blackboard implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Note> currentNotes = new HashMap<String, Note>();
    
    public static class Note implements Serializable {

        private static final long serialVersionUID = 1L;

        private String contents;
        
        private long timestamp;
        
        private String subject;
        
        public Note(String subject, String message) {
            this.subject = subject;
            contents = message;
            timestamp = System.currentTimeMillis();
        }
        
        public String getContents() {
            return contents;
        }
        
        public void setContents(String contents) {
            this.contents = contents;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
        
    }
    
    public void post(Note note) {
        currentNotes.put(note.subject, note);
    }
    
    /**
     * Get current note for given subject, if it is newer than the last note. Return null otherwise
     */
    public Note get(String subject, Note last) {
        Note current = currentNotes.get(subject);
        if (last == null || current == null) {
            return current;
        }
        if (current.timestamp > last.timestamp) {
            return current;
        }
        return null;
    }
    
    public class Monitor implements Serializable {
        
        private String subject;
        
        private Note last;
        
        private Monitor(String aSubject) {
            subject = aSubject;
            last = get(subject, null);
        }
        
        /**
         * Check for new notes on the monitored subject
         * @return true if there is a new note, false otherwise.
         */
        public boolean checkForUpdate() {
            Note next = get(subject, last);
            if (next != null) {
                last = next;
                return true;
            } else {
                return false;
            }
        }
        
    }
    
    /**
     * Obtain a monitor that watches for new notes on a given subject.
     */
    public Monitor monitor(String subject) {
        return new Monitor(subject);
    }
    
}
