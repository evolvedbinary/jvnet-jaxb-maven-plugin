package org.jvnet.jaxb2.maven2;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.jvnet.jaxb2.maven2.resolver.tools.MavenCatalogResolver;

public final class DependencyResource extends Dependency
{
  private static final long serialVersionUID = -7680130645800522100L;
  private String resource;
  private String systemId;

  public DependencyResource ()
  {
    setScope (Artifact.SCOPE_RUNTIME);
  }

  public String getResource ()
  {
    return resource;
  }

  public void setResource (final String resource)
  {
    this.resource = resource;
  }

  public void setSystemId (final String systemId)
  {
    this.systemId = systemId;
  }

  public String getSystemId ()
  {
    if (this.systemId != null)
      return this.systemId;

    // maven:groupId:artifactId:type:classifier:version!/resource/path/in/jar/schema.xsd
    final StringBuilder sb = new StringBuilder ();
    sb.append (MavenCatalogResolver.URI_SCHEME_MAVEN).append (':');
    sb.append (getGroupId ()).append (':');
    sb.append (getArtifactId ()).append (':');
    sb.append (getType () == null ? "" : getType ()).append (':');
    sb.append (getClassifier () == null ? "" : getClassifier ()).append (':');
    sb.append (getVersion () == null ? "" : getVersion ());
    sb.append ("!/");
    sb.append (getResource ());
    return sb.toString ();
  }

  @Override
  public String toString ()
  {
    return "Dependency {groupId=" +
           getGroupId () +
           ", artifactId=" +
           getArtifactId () +
           ", version=" +
           getVersion () +
           ", type=" +
           getType () +
           ", classifier=" +
           getClassifier () +
           ", resource=" +
           getResource () +
           "}";
  }

  private static String _getNotEmpty (final String s, final String def)
  {
    return s == null || s.isEmpty () ? def : s;
  }

  public static DependencyResource valueOf (final String value) throws IllegalArgumentException
  {
    final String resourceDelimiter = "!/";
    final int resourceDelimiterPosition = value.indexOf (resourceDelimiter);

    final String dependencyPart;
    final String resource;
    if (resourceDelimiterPosition == -1)
    {
      dependencyPart = value;
      resource = "";
    }
    else
    {
      dependencyPart = value.substring (0, resourceDelimiterPosition);
      resource = value.substring (resourceDelimiterPosition + resourceDelimiter.length ());
    }

    final List <String> dependencyParts = Arrays.asList (dependencyPart.split (":"));

    if (dependencyParts.size () < 2)
    {
      throw new IllegalArgumentException (MessageFormat.format ("Error parsing dependency descriptor [{0}], both groupId and artifactId must be specified.",
                                                                dependencyPart));
    }

    if (dependencyParts.size () > 5)
    {
      throw new IllegalArgumentException (MessageFormat.format ("Error parsing dependency descriptor [{0}], it contains too many parts.",
                                                                dependencyPart));
    }

    final String groupId = dependencyParts.get (0);
    final String artifactId = dependencyParts.get (1);

    final String type;
    if (dependencyParts.size () > 2)
      type = _getNotEmpty (dependencyParts.get (2), (String) null);
    else
      type = null;

    final String classifier;
    if (dependencyParts.size () > 3)
      classifier = _getNotEmpty (dependencyParts.get (3), (String) null);
    else
      classifier = null;

    final String version;
    if (dependencyParts.size () > 4)
      version = _getNotEmpty (dependencyParts.get (4), (String) null);
    else
      version = null;

    final DependencyResource dependencyResource = new DependencyResource ();
    dependencyResource.setGroupId (groupId);
    dependencyResource.setArtifactId (artifactId);
    if (version != null)
      dependencyResource.setVersion (version);
    if (type != null)
      dependencyResource.setType (type);
    if (classifier != null)
      dependencyResource.setClassifier (classifier);
    if (resource != null)
      dependencyResource.setResource (resource);
    dependencyResource.setSystemId (value);
    return dependencyResource;
  }
}
