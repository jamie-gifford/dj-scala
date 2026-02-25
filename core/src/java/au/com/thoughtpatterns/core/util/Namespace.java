package au.com.thoughtpatterns.core.util;

/**
 * Defines the base URI for namespaces (XML namespaces or other URI-based
 * namespaces) in use by ThoughtPatterns.
 * 
 * The URI scheme is deliberately *not* chosen to be http, because of the 
 * great confusion it causes to have what appear to be http URLs appearing as
 * opaque namespaces. Instead, the recommendation of Norman Walsh 
 * (http://ietfreport.isoc.org/all-ids/draft-walsh-urn-publicid-01.txt-16849.txt)
 * is followed, informally, by using the publicid namespace of the urn scheme.
 * 
 * 
 * More specific namespaces can be created subordinate to this namespace by adding
 * components separated by colons.
 *
 */
public class Namespace {

    public static final String NAMESPACE = "urn:publicid:thoughtpatterns.com.au";
    
}
