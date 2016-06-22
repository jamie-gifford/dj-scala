package au.com.thoughtpatterns.dj.disco.tangodjat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.com.thoughtpatterns.core.util.CsvUtils;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Resources;
import au.com.thoughtpatterns.core.util.SimpleNamespaceContext;
import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.core.util.Util;
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo.TINT;

/**
 * Interface to tango-dj.at database.
 * 
 * Command to turn results into XML: tidy -utf8 -asxml -numeric
 * www.tango-dj.at.htm
 * 
 * 
 * 
 * http://www.tango-dj.at/database/?sortby1=&sortby2=&titlesearch=&albumsearch=&
 * orchestraselector
 * =Miguel+Cal%C3%B3&orquestrasearch=&vocalistselector=&vocalistsearch
 * =&genreselector
 * =&genresearch=&publisherselector=&publishersearch=&yearsearchselector
 * =&yearsearch=&advsearch=Search
 * 
 * <option value="Rodolfo Biagi">Biagi, Rodolfo</option> <option
 * value="Miguel Caló">Caló, Miguel</option> <option
 * value="Francisco Canaro">Canaro, Francisco</option> <option
 * value="Adolfo Carabelli">Carabelli, Adolfo</option> <option
 * value="Alberto Castillo">Castillo, Alberto</option> <option
 * value="Angel D'Agostino">D'Agostino, Angel</option> <option
 * value="Juan D'Arienzo">D'Arienzo Juan</option> <option
 * value="Alfredo De Angelis">De Angelis, Alfredo</option> <option
 * value="Julio De Caro">De Caro, Julio</option> <option
 * value="Lucio Demare">Demare, Lucio</option> <option
 * value="Carlos Di Sarli">Di Sarli, Carlos</option> <option
 * value="Edgardo Donato">Donato, Edgardo</option> <option
 * value="Roberto Firpo">Firpo, Roberto</option> <option
 * value="Francini - Pontier">Francini-Pontier</option> <option
 * value="Osvaldo Fresedo">Fresedo, Osvaldo</option> <option
 * value="Alfredo Gobbi">Gobbi, Alfredo</option> <option
 * value="Pedro Laurenz">Laurenz, Pedro</option> <option
 * value="Francisco Lomuto">Lomuto, Francisco</option> <option
 * value="Osmar Maderna">Maderna, Osmar</option> <option
 * value="Juan Maglio">Maglio, Juan &quot;Pacho&quot;</option> <option
 * value="Ricardo Malerba">Malerba, Ricardo</option> <option
 * value="Orquesta Típica Victor">Orquesta Típica Victor</option> <option
 * value="Astor Piazzolla">Piazzolla, Astor</option> <option
 * value="Osvaldo Pugliese">Pugliese, Osvaldo</option> <option
 * value="Enrique Rodríguez">Rodríguez, Enrique</option> <option
 * value="Francisco Rotundo">Rotundo, Francisco</option> <option
 * value="Horacio Salgán">Salgán, Horacio</option> <option
 * value="Florindo Sassone">Sassone, Florindo</option> <option
 * value="Ricardo Tanturi">Tanturi, Ricardo</option> <option
 * value="Aníbal Troilo">Troilo, Aníbal</option> <option
 * value="Héctor Varela">Varela, Héctor</option> <option
 * value="Miguel Villasboas">Villasboas, Miguel</option>
 */
public class TangoDJAt {

    private static final Logger log = Logger.get(TangoDJAt.class);

    public static final String Rodolfo_Biagi = "Rodolfo Biagi";

    public static final String Miguel_Caló = "Miguel Caló";

    public static final String Francisco_Canaro = "Francisco Canaro";

    public static final String Adolfo_Carabelli = "Adolfo Carabelli";

    public static final String Alberto_Castillo = "Alberto Castillo";

    public static final String Angel_DAgostino = "Angel D'Agostino";

    public static final String Juan_DArienzo = "Juan D'Arienzo";

    public static final String Alfredo_De_Angelis = "Alfredo De Angelis";

    public static final String Julio_De_Caro = "Julio De Caro";

    public static final String Lucio_Demare = "Lucio Demare";

    public static final String Carlos_Di_Sarli = "Carlos Di Sarli";

    public static final String Edgardo_Donato = "Edgardo Donato";

    public static final String Roberto_Firpo = "Roberto Firpo";

    public static final String Francini_Pontier = "Francini - Pontier";

    public static final String Osvaldo_Fresedo = "Osvaldo Fresedo";

    public static final String Alfredo_Gobbi = "Alfredo Gobbi";

    public static final String Pedro_Laurenz = "Pedro Laurenz";

    public static final String Francisco_Lomuto = "Francisco Lomuto";

    public static final String Osmar_Maderna = "Osmar Maderna";

    public static final String Juan_Maglio = "Juan Maglio";

    public static final String Ricardo_Malerba = "Ricardo Malerba";

    public static final String Orquesta_Típica_Victor = "Orquesta Típica Victor";

    public static final String Astor_Piazzolla = "Astor Piazzolla";

    public static final String Osvaldo_Pugliese = "Osvaldo Pugliese";

    public static final String Enrique_Rodríguez = "Enrique Rodríguez";

    public static final String Francisco_Rotundo = "Francisco Rotundo";

    public static final String Horacio_Salgán = "Horacio Salgán";

    public static final String Florindo_Sassone = "Florindo Sassone";

    public static final String Ricardo_Tanturi = "Ricardo Tanturi";

    public static final String Aníbal_Troilo = "Aníbal Troilo";

    public static final String Héctor_Varela = "Héctor Varela";

    public static final String Miguel_Villasboas = "Miguel Villasboas";

    public static final String[] ARTISTS = { Rodolfo_Biagi, Miguel_Caló, Francisco_Canaro,
            Adolfo_Carabelli, Alberto_Castillo, Angel_DAgostino, Juan_DArienzo, Alfredo_De_Angelis,
            Julio_De_Caro, Lucio_Demare, Carlos_Di_Sarli, Edgardo_Donato, Roberto_Firpo,
            Francini_Pontier, Osvaldo_Fresedo, Alfredo_Gobbi, Pedro_Laurenz, Francisco_Lomuto,
            Osmar_Maderna, Juan_Maglio, Ricardo_Malerba, Orquesta_Típica_Victor, Astor_Piazzolla,
            Osvaldo_Pugliese, Enrique_Rodríguez, Francisco_Rotundo, Horacio_Salgán,
            Florindo_Sassone, Ricardo_Tanturi, Aníbal_Troilo, Héctor_Varela, Miguel_Villasboas };

    private List<String[]> data = new ArrayList<String[]>();

    private XPathFactory xPathfactory;

    private XPath xpath;

    private XPathExpression rows;

    private XPathExpression cols;

    private XPathExpression text;

    public static class TDJKey {

        String album;

        int trackNumber;

        public int hashCode() {
            return album.hashCode() + trackNumber * 17;
        }

        public boolean equals(Object other) {
            if (!(other instanceof TDJKey)) {
                return false;
            }
            TDJKey that = (TDJKey) other;
            return album.equals(that.album) && trackNumber == that.trackNumber;
        }
    }

    public static class Metadata {

        public String tinp;

        public String album;

        public int discNumber;

        public int trackNumber;

        public String title;

        public String artist;

        public String genre;

        public String date;

        public TINT key() {
            return new TINT(tinp, discNumber, trackNumber);
        }
    }

    private Set<String> albumsFetched = new HashSet<String>();

    public TangoDJAt() {
        SimpleNamespaceContext nsContext = new SimpleNamespaceContext();

        nsContext.put("ns", "http://www.w3.org/1999/xhtml");

        xPathfactory = XPathFactory.newInstance();
        xpath = xPathfactory.newXPath();

        xpath.setNamespaceContext(nsContext);

        try {
            rows = xpath.compile("//ns:table[@id='searchresult']/ns:tbody/ns:tr");
            cols = xpath.compile("ns:td");
            text = xpath.compile("descendant::text()");
        } catch (XPathExpressionException ex) {
            throw new SystemException(ex);
        }

    }

    public void fetchAlbum(String album) {
        if (albumsFetched.contains(album)) {
            return;
        }
        
        Map<String, String> params = emptySearch();
        params.put("albumsearch", album);

        fetch(params);
    }

    public Metadata getMetadata(String album, int trackNumber) {
        
        for (String[] row : data) {
            
            try {
                int t = Integer.parseInt(row[1]);
                String a = row[8];
            
                if (album.equals(a) && trackNumber == t) {
                    return createMetadata(row);
                }
            } catch (Exception ignore) {}
        }
        
        return null;
    }
    
    private Metadata createMetadata(String[] row) {

        Metadata m = new Metadata();

        m.trackNumber = Integer.parseInt(row[1]);
        // title, artist, date, genre, duration, rating, album 
        m.title = row[2];
        m.artist = row[3];
        m.date = row[4];
        m.genre = row[5];
        m.album = row[8];

        return m;
    }
    
    public void fetchArtists() {
        for (String artist : ARTISTS) {
            fetchArtist(artist);
        }
    }

    public void fetchArtist(String artist) {
        log.info("Fetching data for " + artist);

        Map<String, String> params = emptySearch();
        params.put("orchestraselector", artist);

        fetch(params);
    }

    private Map<String, String> emptySearch() {
        Map<String, String> empty = new HashMap<>();

        empty.put("titlesearch", "");
        empty.put("albumsearch", "");
        empty.put("orquestrasearch", "");
        empty.put("vocalistselector", "");
        empty.put("vocalistsearch", "");
        empty.put("genreselector", "");
        empty.put("genresearch", "");
        empty.put("publisherselector", "");
        empty.put("publishersearch", "");
        empty.put("yearsearchselector", "");
        empty.put("yearsearch", "");
        empty.put("orchestraselector", "");
        empty.put("advsearch", "Search");

        return empty;
    }

    public void fetch(Map<String, String> params) {

        List<String> ps = new ArrayList<>();
        URL url = null;

        try {

            for (String key : params.keySet()) {
                String val = params.get(key);
                ps.add(key + "=" + URLEncoder.encode(val, "UTF-8"));
            }

            String psstr = Util.join("&", ps);

            String u = "http://www.tango-dj.at/database/?sortby1=&sortby2=&" + psstr;

            // String u =
            // "http://www.tango-dj.at/database/?sortby1=&sortby2=&titlesearch=&albumsearch=&orquestrasearch=&vocalistselector=&vocalistsearch=&genreselector=&genresearch=&publisherselector=&publishersearch=&yearsearchselector=&yearsearch=&advsearch=Search&orchestraselector="
            // + URLEncoder.encode(artist, "UTF-8");

            // u = "file:///tmp/canaro.html";

            url = new URL(u);

        } catch (Exception ex) {
            throw new SystemException(ex);
        }

        File outFile = null;

        try {
            InputStream in = url.openStream();

            byte[] bytes = Resources.readByteArray(in);

            File tmp = File.createTempFile("tangodjat", ".html");

            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write(bytes);
            fos.close();

            File dir = tmp.getParentFile();

            String filename = tmp.getName();
            String out = filename.replace(".html", ".xml");

            // LINUX
            ProcessBuilder b = new ProcessBuilder("tidy", "-utf8", "-asxml", "-numeric", filename);

            b.directory(dir);
            // LINUX
            b.redirectError(new File("/dev/null"));

            outFile = new File(dir, out);
            b.redirectOutput(outFile);

            log.info("Writing to " + outFile);

            Process p = b.start();

            int exit = p.waitFor();

            log.info("Got exit status " + exit + " for tidy");

        } catch (Exception ex) {
            throw new SystemException(ex);
        }

        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            DocumentBuilder b = f.newDocumentBuilder();

            Document doc = b.parse(outFile);

            List<Node> nl = toList((NodeList) rows.evaluate(doc, XPathConstants.NODESET));

            int length = nl.size();

            log.info("Got " + length + " items");

            int i = 0;
            for (i = 0; i < length; i++) {

                if (i % 100 == 0) {
                    log.debug("Done " + i + " of " + length);
                }

                Node node = nl.get(i);

                List<Node> cl = toList(extractTd(node));

                int colNumber = cl.size();

                String[] line = new String[colNumber];
                data.add(line);

                for (int j = 0; j < colNumber; j++) {

                    Node c = cl.get(j);

                    String str = extractText(c);
                    str = str.replaceAll("^\\s+", "");
                    str = str.replaceAll("\\s+", " ");
                    str = str.replaceAll(" $", "");

                    line[j] = str;

                    c = c.getNextSibling();
                }

                node = node.getNextSibling();

            }

        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    private NodeList extractTd(Node node) throws XPathExpressionException {
        return (NodeList) cols.evaluate(node, XPathConstants.NODESET);
    }

    private String extractText(Node node) throws XPathExpressionException {
        return (String) text.evaluate(node, XPathConstants.STRING);
    }

    private List<Node> toList(NodeList nl) {
        List<Node> list = new ArrayList<>();
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            list.add(nl.item(i));
        }
        return list;
    }

    public void dump(File csvFile) throws IOException {
        CsvUtils csv = new CsvUtils();

        String[][] arr = new String[data.size()][];

        data.toArray(arr);

        csv.toCsv(arr);

        String out = csv.getFormattedString();

        Writer w = new FileWriter(csvFile);
        w.write(out);
        w.close();

        log.info("Wrote " + arr.length + " records to " + csvFile);
    }

}
