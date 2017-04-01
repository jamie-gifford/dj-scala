package au.com.thoughtpatterns.djs.lib;

import au.com.thoughtpatterns.core.json.AJsonyObject;


public class ScalaBugHelper {

	/**
	 * Work around scala compiler bug
	 * https://newfivefour.com/scala-ambiguous-reference-to-overloaded-defintion.html 
	 */
	public static void setJsony(AJsonyObject target, String key, int value) {
		target.set(key,  value);
	}
	
}
