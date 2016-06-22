package au.com.thoughtpatterns.djs.tag;

import java.io.File;
import java.io.IOException;

public class TagFactory {

    public ITag getTag(File aFile) throws IOException {
        return new Lltag(aFile);
    }

}
