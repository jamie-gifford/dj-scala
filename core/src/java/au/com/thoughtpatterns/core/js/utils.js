// Thoughtpatterns javascript utilities.
// All functionality is contained as properties of the global ThoughtPatterns object.

// Global ThoughtPatterns object
var ThoughtPatterns = new Object();

ThoughtPatterns.init = function() {
	var tp = ThoughtPatterns;
	
	function publish (name, obj) {
		tp[name]= obj;
	}
	
	function version() {
		return 3;
	}
	
	var tp_namespace = "urn:publicid:thoughtpatterns.com.au";
	var js_namespace = tp_namespace + ":core:js";
	
	// Visit the elements of the DOM tree, starting at the given element, by calling
	// the visitor with the elt as argument. If the visitor returns false, stop the walk.
	function walk(elt, visitor) {
		var stop = false;
		if (! visitor(elt)) {
			stop = true;
		}
		if (stop) {
			return;
		}
		var length = elt.childNodes.length;
		for (var i = 0; i < length; i++) {
			var e = elt.childNodes[i];
			if (e.nodeType == 1) { // elt
				walk(e, visitor);
			}
		}
	}
	
	// Given a DOM element, find the first child whose tpjs:relid attribute has the given value
	function findRelId(elt, relid) {
		var found = null;
		function visit(e) {
			try {
				if (e.getAttribute("tpjs:relid") == relid) {
					found = e;
					return false;
				}
			} catch (iebug) {}
			return true;
		}
		walk(elt, visit);
		return found;		
	}

	// Given a DOM element, find the first ancestor whose tpjs:relid attribute has the 
	// given value
	function findRelIdAncestor(elt, relid) {
		while (elt) {
			try {
				if (elt.getAttribute("tpjs:relid") == relid) {
					return elt;
				}
			} catch (iebug) {}
			elt = elt.parentNode;
		}
		return null;
	}
	
	function addOnWindowLoad(f) {
		var already = window.onload;
		if (! already) {
			window.onload = f;
		} else {
			var chain = function() {
				already();
				f();
			}
			window.onload = chain;
		}
	}

	// Determine if element has a given class
	function hasClass(elt, clas) {
		var already = elt.getAttribute("class");
		if (! already) {
			return false;
		} else {
			// May contain class
			var classes = already.split(/ +/);
			var found = false;
			for (var i = 0; i < classes.length; i++) {
				if (classes[i] == clas) {
					found = true;
				}
			}
			return found;
		}
	}
	
	// Add a class to the given element
	function addClass(elt, clas) {
		var already = elt.getAttribute("class");
		var v;
		if (! already) {
			v = clas;
		} else {
			if (! hasClass(elt, clas)) {
				v = already + " " + clas;
			}
		}
		elt.setAttribute("class", v);
	}

	
	function removeClass(elt, clas) {
		var already = elt.getAttribute("class");
		if (! already) {
			return;
		}
		var expr = "^" + clas + "$|^" + clas + " | " + clas + "$| " + clas + " ";
		var re = new RegExp(expr);
		var now = already.replace(re, "");
		elt.setAttribute("class", now);
	}

	publish("version", version);
	publish("publish", publish);
	publish("walk", walk);
	publish("xmlns:tpjs", js_namespace);
	publish("findRelId", findRelId);
	publish("findRelIdAncestor", findRelIdAncestor);
	publish("addOnWindowLoad", addOnWindowLoad);
	publish("hasClass", hasClass);
	publish("addClass", addClass);
	publish("removeClass", removeClass);
}

ThoughtPatterns.init();
