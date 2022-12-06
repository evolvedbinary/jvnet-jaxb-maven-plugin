package org.jvnet.jaxb2.maven2.resolver.tools;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.xml.resolver.tools.CatalogResolver;

public class ClasspathCatalogResolver extends CatalogResolver
{

  public static final String URI_SCHEME_CLASSPATH = "classpath";

  @Override
  public String getResolvedEntity (final String publicId, final String systemId)
  {
    // System.out.println("Resolving [" + publicId + "], [" + systemId + "].");
    final String result = super.getResolvedEntity (publicId, systemId);
    // System.out.println("Resolved to [" + result+ "].");

    if (result == null)
    {
      if (false)
        System.err.println (MessageFormat.format ("Could not resolve publicId [{0}], systemId [{1}]",
                                                  publicId,
                                                  systemId));
      return null;
    }

    try
    {
      final URI uri = new URI (result);
      if (URI_SCHEME_CLASSPATH.equals (uri.getScheme ()))
      {
        final String schemeSpecificPart = uri.getSchemeSpecificPart ();
        if (false)
          System.out.println ("Resolve [" + schemeSpecificPart + "].");

        final URL resource = Thread.currentThread ().getContextClassLoader ().getResource (schemeSpecificPart);
        if (resource == null)
        {
          if (false)
            System.out.println ("Returning [" + null + "].");
          return null;
        }
        if (false)
          System.out.println ("Returning to [" + resource.toString () + "].");
        return resource.toString ();
      }
      if (false)
        System.out.println ("Returning to [" + result + "].");
      return result;
    }
    catch (final URISyntaxException urisex)
    {
      if (false)
        System.out.println ("Returning to [" + result + "].");
      return result;
    }
  }
}
