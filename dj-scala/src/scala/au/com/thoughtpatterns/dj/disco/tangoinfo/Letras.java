package au.com.thoughtpatterns.dj.disco.tangoinfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Resources;
import au.com.thoughtpatterns.core.util.Util;
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo.Work;

public class Letras {

	private static final Logger log = Logger.get(Letras.class);
	
	private TangoInfo ti = new TangoInfo();
	
	private List<Work> works;
	
	private Map<String, List<Work>> byTitle = new HashMap<>();
	
	private void init() {
		if (works != null) {
			return;
		}
		works = ti.fetchWorks();
		
		for (Work w : works) {
			String title = w.title;
			if (! byTitle.keySet().contains(title)) {
				byTitle.put(title, new ArrayList<>());
			}
			List<Work> l = byTitle.get(title);
			l.add(w);
		}
		
	}
	
	public String getLetra(String title, String genre, String composer, String letrista) throws IOException {
		
		init();
		List<Work> candidates = byTitle.get(title);
		
		if (candidates == null) {
			return null;
		}
		
		Work g = null;
		for (Work w : candidates) {
			
			if (Util.equals(title, w.title) 
					&& Util.equals(genre, w.genre) 
					&& (composer == null || Util.equals(composer, w.composer)) 
					&& (letrista == null || Util.equals(letrista, w.letrista))) {
				g = w;
				break;
			}
		}
		
		if (g == null || g.tiwc == null) {
			return null;
		}
		
		File file = new File("letras/tiwc/" + g.tiwc);
		
		file.getParentFile().mkdirs();
		if (file.exists()) {
			ensureLinks(g);
			Reader r = new FileReader(file);
			String str = Resources.readString(r);
			r.close();
			return str;
		}

		String url = ti.toTodoTangoWorkUrl(g.tiwc);
		
		if (url == null) {
			return null;
		}
		
		Connection conn = Jsoup.connect(url);
		conn.timeout(30000);
		
		Document doc = conn.get();

		Elements elts = doc.select("#main_Tema1_lbl_Letra");
		if (elts.size() < 1) {
			return null;
		}
		
		Element elt = elts.get(0);
		
		Formatter f = new Formatter();
		
		NodeTraversor.traverse(f, elt);
		
		f.flush();
		
		String letra = f.toString();
		
		FileWriter writer = new FileWriter(file);
		writer.write(letra);
		writer.close();
		
		ensureLinks(g);
		
		return letra;
	}
	
	private static String slashToBar(String x) {
		return x.replace('/', '|');
	}
	
	public String getLetraFile(String filename0) {
		String filename = slashToBar(filename0);
		File file = new File("letras/title/" + slashToBar(filename));
		
		String trunc = filename.replaceAll(", let\\..*", "");
		if (! file.exists() && ! filename.equals(trunc)) {
			file = new File("letras/title/" + trunc);
		}
		
		if (! file.exists()) {
			return null;
		}
		try {
			Reader r = new FileReader(file);
			String out = Resources.readString(r);
			r.close();
			return out;
		} catch (IOException ex) {
			return null;
		}
	}
	
	private void ensureLinks(Work g) throws IOException {
		Path p = Paths.get("..", "tiwc", g.tiwc);
		if (g.composer != null && g.letrista != null) {
			File t = new File("letras/title/" + slashToBar(g.title + ", com. " + g.composer + ", let. " + g.letrista));
			if (! t.exists()) {
				t.getParentFile().mkdirs();
				Files.createSymbolicLink(t.toPath(), p);
			}
		}
		if (g.composer != null ) {
			File t = new File("letras/title/" + slashToBar(g.title + ", com. " + g.composer));
			if (! t.exists()) {
				t.getParentFile().mkdirs();
				Files.createSymbolicLink(t.toPath(), p);
			}
		}
	}
	
	class Formatter implements NodeVisitor {

		List<String> lines = new ArrayList<>();
		
		StringBuffer accum = new StringBuffer();
		
		@Override
		public void head(Node node, int depth) {
		}

		@Override
		public void tail(Node node, int depth) {
			if (node instanceof TextNode) {
				accum.append(((TextNode) node).text());
			}
			if (node instanceof Element) {
				Element e = (Element) node;
				if ("br".equalsIgnoreCase(e.nodeName())) {
					lines.add(accum.toString());
					accum.delete(0, accum.length());
				}
			}
		}
	
		void flush() {
			if (accum.length() > 0) {
				lines.add(accum.toString());
				accum.delete(0, accum.length());
			}
		}
		
		public String toString() {
	
			return Util.join("\n", lines);
		}
		
	}
	
}
