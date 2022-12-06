package org.jvnet.jaxb2.maven2.resolver.tools;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.plugin.logging.Log;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.jvnet.jaxb2.maven2.DependencyResource;
import org.jvnet.jaxb2.maven2.IDependencyResourceResolver;
import org.jvnet.jaxb2.maven2.plugin.logging.NullLog;

public class MavenCatalogResolver extends CatalogResolver
{
  public static final String URI_SCHEME_MAVEN = "maven";

  private final IDependencyResourceResolver dependencyResourceResolver;
  private final CatalogManager catalogManager;
  private final Log log;

  public MavenCatalogResolver (final CatalogManager catalogManager,
                               final IDependencyResourceResolver dependencyResourceResolver)
  {
    this (catalogManager, dependencyResourceResolver, NullLog.INSTANCE);
  }

  public MavenCatalogResolver (final CatalogManager catalogManager,
                               final IDependencyResourceResolver dependencyResourceResolver,
                               final Log log)
  {
    super (catalogManager);
    this.catalogManager = catalogManager;
    if (dependencyResourceResolver == null)
    {
      throw new IllegalArgumentException ("Dependency resource resolver must not be null.");
    }
    this.dependencyResourceResolver = dependencyResourceResolver;
    this.log = log != null ? log : NullLog.INSTANCE;
  }

  protected CatalogManager getCatalogManager ()
  {
    return catalogManager;
  }

  protected Log getLog ()
  {
    return log;
  }

  @Override
  public String getResolvedEntity (final String publicId, final String origSystemId)
  {
    String systemId = origSystemId;
    getLog ().debug ("Resolving publicId [" + publicId + "], systemId [" + systemId + "].");

    final String superResolvedEntity = super.getResolvedEntity (publicId, systemId);
    if (superResolvedEntity != null)
      systemId = superResolvedEntity;
    getLog ().debug ("  Parent resolver has resolved publicId [" +
                     publicId +
                     "], systemId [" +
                     systemId +
                     "] to [" +
                     superResolvedEntity +
                     "].");

    if (systemId == null)
    {
      return null;
    }

    try
    {
      final URI uri = new URI (systemId);
      if (URI_SCHEME_MAVEN.equals (uri.getScheme ()))
      {
        getLog ().debug ("  Resolving systemId [" + systemId + "] as Maven dependency resource.");
        final String schemeSpecificPart = uri.getSchemeSpecificPart ();
        try
        {
          final DependencyResource dependencyResource = DependencyResource.valueOf (schemeSpecificPart);
          try
          {
            final URL url = dependencyResourceResolver.resolveDependencyResource (dependencyResource);
            final String resolved = url.toString ();
            getLog ().debug ("  Resolved systemId [" + systemId + "] to [" + resolved + "].");
            return resolved;
          }
          catch (final Exception ex)
          {
            getLog ().error ("  Error resolving dependency resource [" + dependencyResource + "].");
          }
        }
        catch (final IllegalArgumentException iaex)
        {
          getLog ().error ("  Error parsing dependency descriptor [" + schemeSpecificPart + "].");

        }
        getLog ().error ("  Failed to resolve systemId [" +
                         systemId +
                         "] as dependency resource. " +
                         "Returning parent resolver result [" +
                         superResolvedEntity +
                         "].");
        return superResolvedEntity;
      }
      getLog ().debug ("  SystemId [" +
                       systemId +
                       "] is not a Maven dependency resource URI. " +
                       "Returning parent resolver result [" +
                       superResolvedEntity +
                       "].");
      return superResolvedEntity;
    }
    catch (final URISyntaxException urisex)
    {
      getLog ().debug ("  Could not parse the systemId [" +
                       systemId +
                       "] as URI. " +
                       "Returning parent resolver result [" +
                       superResolvedEntity +
                       "].");
      return superResolvedEntity;
    }
  }

  @Override
  public String toString ()
  {
    return "[" +
           getClass ().getName () +
           " - " +
           "dependencyResourceResolver=" +
           dependencyResourceResolver +
           "; " +
           "catalogManager=" +
           catalogManager +
           "; " +
           "log=" +
           log +
           "]";
  }
}
