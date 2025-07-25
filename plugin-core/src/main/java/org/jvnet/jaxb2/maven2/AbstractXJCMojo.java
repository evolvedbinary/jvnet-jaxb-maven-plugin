package org.jvnet.jaxb2.maven2;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.apache.maven.settings.Settings;
import org.jvnet.jaxb2.maven2.util.ArtifactUtils;
import org.jvnet.jaxb2.maven2.util.IOUtils;
import org.codehaus.plexus.build.BuildContext;
import org.codehaus.plexus.build.DefaultBuildContext;

public abstract class AbstractXJCMojo<O> extends AbstractMojo implements IDependencyResourceResolver
{

  @Parameter (defaultValue = "${settings}", readonly = true)
  private Settings settings;

  public Settings getSettings ()
  {
    return settings;
  }

  public void setSettings (final Settings settings)
  {
    this.settings = settings;
  }

  /**
   * If set to <code>true</code>, passes Maven's active proxy settings to XJC.
   * Default value is <code>false</code>. Proxy settings are passed using the
   * <code>-httpproxy</code> argument in the form
   * <code>[user[:password]@]proxyHost[:proxyPort]</code>. This sets both HTTP
   * as well as HTTPS proxy.
   */
  @Parameter (property = "maven.xjc2.useActiveProxyAsHttpproxy", defaultValue = "false")
  private boolean useActiveProxyAsHttpproxy = false;

  public boolean isUseActiveProxyAsHttpproxy ()
  {
    return this.useActiveProxyAsHttpproxy;
  }

  public void setUseActiveProxyAsHttpproxy (final boolean useActiveProxyAsHttpproxy)
  {
    this.useActiveProxyAsHttpproxy = useActiveProxyAsHttpproxy;
  }

  @Parameter (property = "maven.xjc2.proxyHost")
  private String proxyHost;

  public void setProxyHost (final String proxyHost)
  {
    this.proxyHost = proxyHost;
  }

  public String getProxyHost ()
  {
    return this.proxyHost;
  }

  @Parameter (property = "maven.xjc2.proxyPort")
  private int proxyPort;

  public void setProxyPort (final int proxyPort)
  {
    this.proxyPort = proxyPort;
  }

  public int getProxyPort ()
  {
    return this.proxyPort;
  }

  @Parameter (property = "maven.xjc2.proxyUsername")
  private String proxyUsername;

  public void setProxyUsername (final String proxyUsername)
  {
    this.proxyUsername = proxyUsername;
  }

  public String getProxyUsername ()
  {
    return this.proxyUsername;
  }

  @Parameter (property = "maven.xjc2.proxyPassword")
  private String proxyPassword;

  public void setProxyPassword (final String proxyPassword)
  {
    this.proxyPassword = proxyPassword;
  }

  public String getProxyPassword ()
  {
    return this.proxyPassword;
  }

  /**
   * Encoding for the generated sources, defaults to
   * ${project.build.sourceEncoding}.
   */
  @Parameter (property = "encoding", defaultValue = "${project.build.sourceEncoding}")
  private String encoding;

  public String getEncoding ()
  {
    return encoding;
  }

  public void setEncoding (final String encoding)
  {
    this.encoding = encoding;
  }

  /**
   * Locale for the generated sources.
   */
  @Parameter (property = "locale")
  private String locale;

  public String getLocale ()
  {
    return locale;
  }

  public void setLocale (final String locale)
  {
    this.locale = locale;
  }

  /**
   * Type of input schema language. One of: DTD, XMLSCHEMA, RELAXNG,
   * RELAXNG_COMPACT, WSDL, AUTODETECT. If unspecified, it is assumed
   * AUTODETECT.
   */
  @Parameter (property = "maven.xjc2.schemaLanguage")
  private String schemaLanguage;

  public String getSchemaLanguage ()
  {
    return schemaLanguage;
  }

  public void setSchemaLanguage (final String schemaLanguage)
  {
    this.schemaLanguage = schemaLanguage;
  }

  /**
   * The source directory containing *.xsd schema files. Notice that binding
   * files are searched by default in this directory.
   */
  @Parameter (defaultValue = "src/main/resources", property = "maven.xjc2.schemaDirectory", required = true)
  private File schemaDirectory;

  public File getSchemaDirectory ()
  {
    return schemaDirectory;
  }

  public void setSchemaDirectory (final File schemaDirectory)
  {
    this.schemaDirectory = schemaDirectory;
  }

  /**
   * <p>
   * A list of regular expression file search patterns to specify the schemas to
   * be processed. Searching is based from the root of
   * <code>schemaDirectory</code>.
   * </p>
   * <p>
   * If left undefined, then all <code>*.xsd</code> files in
   * <code>schemaDirectory</code> will be processed.
   * </p>
   */
  @Parameter
  private String [] schemaIncludes = new String [] { "*.xsd" };

  public String [] getSchemaIncludes ()
  {
    return schemaIncludes;
  }

  public void setSchemaIncludes (final String [] schemaIncludes)
  {
    this.schemaIncludes = schemaIncludes;
  }

  /**
   * A list of regular expression file search patterns to specify the schemas to
   * be excluded from the <code>schemaIncludes</code> list. Searching is based
   * from the root of schemaDirectory.
   */
  @Parameter
  private String [] schemaExcludes;

  public String [] getSchemaExcludes ()
  {
    return schemaExcludes;
  }

  public void setSchemaExcludes (final String [] schemaExcludes)
  {
    this.schemaExcludes = schemaExcludes;
  }

  /**
   * A list of schema resources which could includes file sets, URLs, Maven
   * artifact resources.
   */
  @Parameter
  private ResourceEntry [] schemas = new ResourceEntry [0];

  public ResourceEntry [] getSchemas ()
  {
    return schemas;
  }

  public void setSchemas (final ResourceEntry [] schemas)
  {
    this.schemas = schemas;
  }

  /**
   * <p>
   * The source directory containing the *.xjb binding files.
   * </p>
   * <p>
   * If left undefined, then the <code>schemaDirectory</code> is assumed.
   * </p>
   */
  @Parameter (property = "maven.xjc2.bindingDirectory")
  private File bindingDirectory;

  public void setBindingDirectory (final File bindingDirectory)
  {
    this.bindingDirectory = bindingDirectory;
  }

  public File getBindingDirectory ()
  {
    return bindingDirectory != null ? bindingDirectory : getSchemaDirectory ();
  }

  /**
   * The source directory containing <code>*.cat</code> catalog files. Defaults
   * to the <code>schemaDirectory</code>.
   */
  @Parameter (property = "maven.xjc2.catalogDirectory")
  private File catalogDirectory;

  public void setCatalogDirectory (final File catalogDirectory)
  {
    this.catalogDirectory = catalogDirectory;
  }

  public File getCatalogDirectory ()
  {
    return catalogDirectory != null ? catalogDirectory : getSchemaDirectory ();
  }

  /**
   * <p>
   * A list of regular expression file search patterns to specify the binding
   * files to be processed. Searching is based from the root of
   * <code>bindingDirectory</code>.
   * </p>
   * <p>
   * If left undefined, then all *.xjb files in schemaDirectory will be
   * processed.
   * </p>
   */
  @Parameter
  private String [] bindingIncludes = new String [] { "*.xjb" };

  public String [] getBindingIncludes ()
  {
    return bindingIncludes;
  }

  public void setBindingIncludes (final String [] bindingIncludes)
  {
    this.bindingIncludes = bindingIncludes;
  }

  /**
   * A list of regular expression file search patterns to specify the binding
   * files to be excluded from the <code>bindingIncludes</code>. Searching is
   * based from the root of bindingDirectory.
   */
  @Parameter
  private String [] bindingExcludes;

  public String [] getBindingExcludes ()
  {
    return bindingExcludes;
  }

  public void setBindingExcludes (final String [] bindingExcludes)
  {
    this.bindingExcludes = bindingExcludes;
  }

  /**
   * A list of binding resources which could includes file sets, URLs, Maven
   * artifact resources.
   */
  @Parameter
  private ResourceEntry [] bindings = new ResourceEntry [0];

  public ResourceEntry [] getBindings ()
  {
    return bindings;
  }

  public void setBindings (final ResourceEntry [] bindings)
  {
    this.bindings = bindings;
  }

  /**
   * If 'true', maven's default exludes are NOT added to all the excludes lists.
   */
  @Parameter (defaultValue = "false", property = "maven.xjc2.disableDefaultExcludes")
  private boolean disableDefaultExcludes;

  public boolean getDisableDefaultExcludes ()
  {
    return disableDefaultExcludes;
  }

  public void setDisableDefaultExcludes (final boolean disableDefaultExcludes)
  {
    this.disableDefaultExcludes = disableDefaultExcludes;
  }

  /**
   * Specify the catalog file to resolve external entity references (xjc's
   * -catalog option)
   * </p>
   * <p>
   * Support TR9401, XCatalog, and OASIS XML Catalog format. See the
   * catalog-resolver sample and this article for details.
   * </p>
   */
  @Parameter (property = "maven.xjc2.catalog")
  private File catalog;

  public File getCatalog ()
  {
    return catalog;
  }

  public void setCatalog (final File catalog)
  {
    this.catalog = catalog;
  }

  /**
   * <p>
   * A list of regular expression file search patterns to specify the catalogs
   * to be processed. Searching is based from the root of
   * <code>catalogDirectory</code>.
   * </p>
   * <p>
   * If left undefined, then all <code>*.cat</code> files in
   * <code>catalogDirectory</code> will be processed.
   * </p>
   */
  @Parameter
  private String [] catalogIncludes = new String [] { "*.cat" };

  public String [] getCatalogIncludes ()
  {
    return catalogIncludes;
  }

  public void setCatalogIncludes (final String [] catalogIncludes)
  {
    this.catalogIncludes = catalogIncludes;
  }

  /**
   * A list of regular expression file search patterns to specify the catalogs
   * to be excluded from the <code>catalogIncludes</code> list. Searching is
   * based from the root of <code>catalogDirectory</code>.
   */
  @Parameter
  private String [] catalogExcludes;

  public String [] getCatalogExcludes ()
  {
    return catalogExcludes;
  }

  public void setCatalogExcludes (final String [] catalogExcludes)
  {
    this.catalogExcludes = catalogExcludes;
  }

  /**
   * A list of catalog resources which could includes file sets, URLs, Maven
   * artifact resources.
   */
  @Parameter
  private ResourceEntry [] catalogs = new ResourceEntry [0];

  public ResourceEntry [] getCatalogs ()
  {
    return catalogs;
  }

  public void setCatalogs (final ResourceEntry [] catalogs)
  {
    this.catalogs = catalogs;
  }

  protected List <URI> createCatalogURIs () throws MojoExecutionException
  {
    final File catalog = getCatalog ();
    final ResourceEntry [] catalogs = getCatalogs ();
    final List <URI> catalogUris = new ArrayList <> ((catalog == null ? 0 : 1) + catalogs.length);
    if (catalog != null)
    {
      catalogUris.add (getCatalog ().toURI ());
    }
    for (final ResourceEntry resourceEntry : catalogs)
    {
      catalogUris.addAll (createResourceEntryUris (resourceEntry,
                                                   getCatalogDirectory ().getAbsolutePath (),
                                                   getCatalogIncludes (),
                                                   getCatalogExcludes ()));
    }
    return catalogUris;
  }

  /**
   * Provides the class name of the catalog resolver.
   */
  @Parameter (property = "maven.xjc2.catalogResolver")
  protected String m_catalogResolver = null;

  public String getCatalogResolver ()
  {
    return m_catalogResolver;
  }

  public void setCatalogResolver (final String catalogResolver)
  {
    this.m_catalogResolver = catalogResolver;
  }

  /**
   * <p>
   * The generated classes will all be placed under this Java package (xjc's -p
   * option), unless otherwise specified in the schemas.
   * </p>
   * <p>
   * If left unspecified, the package will be derived from the schemas only.
   * </p>
   */
  @Parameter (property = "maven.xjc2.generatePackage")
  private String generatePackage;

  public String getGeneratePackage ()
  {
    return generatePackage;
  }

  public void setGeneratePackage (final String generatePackage)
  {
    this.generatePackage = generatePackage;
  }

  /**
   * <p>
   * Generated code will be written under this directory.
   * </p>
   * <p>
   * For instance, if you specify <code>generateDirectory="doe/ray"</code> and
   * <code>generatePackage="org.here"</code>, then files are generated to
   * <code>doe/ray/org/here</code>.
   * </p>
   */
  @Parameter (defaultValue = "${project.build.directory}/generated-sources/xjc",
              property = "maven.xjc2.generateDirectory",
              required = true)
  private File generateDirectory;

  public File getGenerateDirectory ()
  {
    return generateDirectory;
  }

  public void setGenerateDirectory (final File generateDirectory)
  {
    this.generateDirectory = generateDirectory;

    if (getEpisodeFile () == null)
    {
      final File episodeFile = new File (getGenerateDirectory (), "META-INF" + File.separator + "sun-jaxb.episode");
      setEpisodeFile (episodeFile);
    }
  }

  /**
   * If set to true (default), adds target directory as a compile source root of
   * this Maven project.
   */
  @Parameter (defaultValue = "true", property = "maven.xjc2.addCompileSourceRoot", required = false)
  private boolean addCompileSourceRoot = true;

  public boolean getAddCompileSourceRoot ()
  {
    return addCompileSourceRoot;
  }

  public void setAddCompileSourceRoot (final boolean addCompileSourceRoot)
  {
    this.addCompileSourceRoot = addCompileSourceRoot;
  }

  /**
   * If set to true, adds target directory as a test compile source root of this
   * Maven project. Default value is false.
   */
  @Parameter (defaultValue = "false", property = "maven.xjc2.addTestCompileSourceRoot", required = false)
  private boolean addTestCompileSourceRoot = false;

  public boolean getAddTestCompileSourceRoot ()
  {
    return addTestCompileSourceRoot;
  }

  public void setAddTestCompileSourceRoot (final boolean addTestCompileSourceRoot)
  {
    this.addTestCompileSourceRoot = addTestCompileSourceRoot;
  }

  /**
   * If 'true', the generated Java source files are set as read-only (xjc's
   * -readOnly option).
   */
  @Parameter (defaultValue = "false", property = "maven.xjc2.readOnly")
  private boolean readOnly;

  public boolean getReadOnly ()
  {
    return readOnly;
  }

  public void setReadOnly (final boolean readOnly)
  {
    this.readOnly = readOnly;
  }

  /**
   * If 'false', suppresses generation of package level annotations
   * (package-info.java), xjc's -npa option.
   */
  @Parameter (defaultValue = "true", property = "maven.xjc2.packageLevelAnnotations")
  private boolean packageLevelAnnotations = true;

  public boolean getPackageLevelAnnotations ()
  {
    return packageLevelAnnotations;
  }

  public void setPackageLevelAnnotations (final boolean packageLevelAnnotations)
  {
    this.packageLevelAnnotations = packageLevelAnnotations;
  }

  /**
   * If 'true', suppresses generation of a file header with timestamp, xjc's
   * -no-header option.
   */
  @Parameter (defaultValue = "false", property = "maven.xjc2.noFileHeader")
  private boolean noFileHeader = false;

  public boolean getNoFileHeader ()
  {
    return noFileHeader;
  }

  public void setNoFileHeader (final boolean noFileHeader)
  {
    this.noFileHeader = noFileHeader;
  }

  /**
   * If 'true', enables correct generation of Boolean getters/setters to enable
   * Bean Introspection apis; xjc's -enableIntrospection option.
   */
  @Parameter (defaultValue = "false", property = "maven.xjc2.enableIntrospection")
  private boolean enableIntrospection = false;

  public boolean getEnableIntrospection ()
  {
    return enableIntrospection;
  }

  public void setEnableIntrospection (final boolean enableIntrospection)
  {
    this.enableIntrospection = enableIntrospection;
  }

  /**
   * If 'true', disables XML security features when parsing XML documents; xjc's
   * -disableXmlSecurity option.
   */
  @Parameter (defaultValue = "true", property = "maven.xjc2.disableXmlSecurity")
  private boolean disableXmlSecurity = true;

  public boolean getDisableXmlSecurity ()
  {
    return disableXmlSecurity;
  }

  public void setDisableXmlSecurity (final boolean disableXmlSecurity)
  {
    this.disableXmlSecurity = disableXmlSecurity;
  }

  /**
   * Restrict access to the protocols specified for external reference set by
   * the schemaLocation attribute, Import and Include element. Value: a list of
   * protocols separated by comma. A protocol is the scheme portion of a
   * {@link java.net.URI}, or in the case of the JAR protocol, "jar" plus the
   * scheme portion separated by colon. The keyword "all" grants permission to
   * all protocols.
   */
  @Parameter (defaultValue = "all", property = "maven.xjc2.accessExternalSchema")
  private String accessExternalSchema = "all";

  public String getAccessExternalSchema ()
  {
    return accessExternalSchema;
  }

  public void setAccessExternalSchema (final String accessExternalSchema)
  {
    this.accessExternalSchema = accessExternalSchema;
  }

  /**
   * Restricts access to external DTDs and external Entity References to the
   * protocols specified. Value: a list of protocols separated by comma. A
   * protocol is the scheme portion of a {@link java.net.URI}, or in the case of
   * the JAR protocol, "jar" plus the scheme portion separated by colon. The
   * keyword "all" grants permission to all protocols.
   */
  @Parameter (defaultValue = "all", property = "maven.xjc2.accessExternalDTD")
  private String accessExternalDTD = "all";

  public String getAccessExternalDTD ()
  {
    return accessExternalDTD;
  }

  public void setAccessExternalDTD (final String accessExternalDTD)
  {
    this.accessExternalDTD = accessExternalDTD;
  }

  /**
   * Enables external entity processing.
   */
  @Parameter (defaultValue = "true", property = "maven.xjc2.enableExternalEntityProcessing")
  private boolean enableExternalEntityProcessing;

  public boolean isEnableExternalEntityProcessing ()
  {
    return enableExternalEntityProcessing;
  }

  public void setEnableExternalEntityProcessing (final boolean enableExternalEntityProcessing)
  {
    this.enableExternalEntityProcessing = enableExternalEntityProcessing;
  }

  /**
   * If <code>true</code>, generates content property for types with multiple
   * <code>xs:any</code> derived elements; corresponds to the XJC
   * <code>-contentForWildcard</code> option.
   */
  @Parameter (defaultValue = "false")
  private boolean contentForWildcard;

  public boolean getContentForWildcard ()
  {
    return contentForWildcard;
  }

  public void setContentForWildcard (final boolean contentForWildcard)
  {
    this.contentForWildcard = contentForWildcard;
  }

  /**
   * If 'true', the XJC binding compiler will run in the extension mode (xjc's
   * -extension option). Otherwise, it will run in the strict conformance mode.
   */
  @Parameter (defaultValue = "true", property = "maven.xjc2.extension")
  private boolean extension;

  public boolean getExtension ()
  {
    return extension;
  }

  public void setExtension (final boolean extension)
  {
    this.extension = extension;
  }

  /**
   * If 'true' (default), Perform strict validation of the input schema
   * (disabled by the xjc's -nv option).
   */
  @Parameter (defaultValue = "true", property = "maven.xjc2.strict")
  private boolean strict = true;

  public boolean getStrict ()
  {
    return strict;
  }

  public void setStrict (final boolean strict)
  {
    this.strict = strict;
  }

  /**
   * If 'false', the plugin will not write the generated code to disk.
   */
  @Parameter (defaultValue = "true", property = "maven.xjc2.writeCode")
  private boolean writeCode = true;

  public boolean getWriteCode ()
  {
    return writeCode;
  }

  public void setWriteCode (final boolean writeCode)
  {
    this.writeCode = writeCode;
  }

  /**
   * <p>
   * If 'true', the plugin and the XJC compiler are both set to verbose mode
   * (xjc's -verbose option).
   * </p>
   * <p>
   * It is automatically set to 'true' when maven is run in debug mode (mvn's -X
   * option).
   * </p>
   */
  @Parameter (defaultValue = "false", property = "maven.xjc2.verbose")
  private boolean verbose;

  public boolean getVerbose ()
  {
    return verbose;
  }

  public void setVerbose (final boolean verbose)
  {
    this.verbose = verbose;
  }

  /**
   * <p>
   * If 'true', the XJC compiler is set to debug mode (xjc's -debug option).
   * </p>
   * <p>
   * It is automatically set to 'true' when maven is run in debug mode (mvn's -X
   * option).
   * </p>
   */
  @Parameter (defaultValue = "false", property = "maven.xjc2.debug")
  private boolean debug;

  public boolean getDebug ()
  {
    return debug;
  }

  public void setDebug (final boolean debug)
  {
    this.debug = debug;
  }

  /**
   * <p>
   * A list of extra XJC's command-line arguments (items must include the dash
   * '-'). Use this argument to enable the JAXB2 plugins you want to use.
   * </p>
   * <p>
   * Arguments set here take precedence over other mojo parameters.
   * </p>
   */
  @Parameter
  private final List <String> args = new LinkedList <> ();

  public List <String> getArgs ()
  {
    return args;
  }

  public void setArgs (final List <String> args)
  {
    this.args.addAll (args);
  }

  /**
   * If true, no up-to-date check is performed and the XJC always re-generates
   * the sources. Otherwise schemas will only be recompiled if anything has
   * changed.
   */
  @Parameter (defaultValue = "false", property = "maven.xjc2.forceRegenerate")
  private boolean forceRegenerate;

  public boolean getForceRegenerate ()
  {
    return forceRegenerate;
  }

  public void setForceRegenerate (final boolean forceRegenerate)
  {
    this.forceRegenerate = forceRegenerate;
  }

  /**
   * <p>
   * If 'true', the generateDirectory will be deleted before the XJC binding
   * compiler recompiles the source files. Default is false.
   * </p>
   * <p>
   * Note that if set to 'false', the up-to-date check might not work, since XJC
   * does not regenerate all files (i.e. files for "any" elements under
   * 'xjc/org/w3/_2001/xmlschema' directory).
   * </p>
   */
  @Parameter (defaultValue = "false", property = "maven.xjc2.removeOldOutput")
  private boolean removeOldOutput;

  public boolean getRemoveOldOutput ()
  {
    return removeOldOutput;
  }

  public void setRemoveOldOutput (final boolean removeOldOutput)
  {
    this.removeOldOutput = removeOldOutput;
  }

  /**
   * <p>
   * If 'true', package directories will be cleaned before the XJC binding
   * compiler generates the source files.
   * </p>
   */
  @Parameter (defaultValue = "true", property = "maven.xjc2.removeOldPackages")
  private boolean cleanPackageDirectories = true;

  public boolean getCleanPackageDirectories ()
  {
    return cleanPackageDirectories;
  }

  public void setCleanPackageDirectories (final boolean removeOldPackages)
  {
    this.cleanPackageDirectories = removeOldPackages;
  }

  /**
   * Specifies patterns of files produced by this plugin. This is used to check
   * if produced files are up-to-date. Default value is ** /*.*, ** /*.java, **
   * /bgm.ser, ** /jaxb.properties.
   */
  @Parameter
  private String [] produces = new String [] { "**/*.*", "**/*.java", "**/bgm.ser", "**/jaxb.properties" };

  public String [] getProduces ()
  {
    return produces;
  }

  public void setProduces (final String [] produces)
  {
    this.produces = produces;
  }

  /**
   * A list of of input files or URLs to consider during the up-to-date. By
   * default it always considers: 1. schema files, 2. binding files, 3. catalog
   * file, and 4. the pom.xml file of the project executing this plugin.
   * Deprecated, use {@link #otherDependsIncludes} and
   * {@link #otherDependsExcludes} instead.
   */
  @Deprecated
  @Parameter
  private File [] otherDepends;

  @Deprecated
  public File [] getOtherDepends ()
  {
    return otherDepends;
  }

  @Deprecated
  public void setOtherDepends (final File [] otherDepends)
  {
    this.otherDepends = otherDepends;
  }

  @Parameter
  private String [] otherDependsIncludes;

  public String [] getOtherDependsIncludes ()
  {
    return otherDependsIncludes;
  }

  public void setOtherDependsIncludes (final String [] otherDependsIncludes)
  {
    this.otherDependsIncludes = otherDependsIncludes;
  }

  @Parameter
  private String [] otherDependsExcludes;

  public String [] getOtherDependsExcludes ()
  {
    return otherDependsExcludes;
  }

  public void setOtherDependsExcludes (final String [] otherDependsExcludes)
  {
    this.otherDependsExcludes = otherDependsExcludes;
  }

  /**
   * Target location of the episode file. By default it is
   * target/generated-sources/xjc/META-INF/sun-jaxb.episode so that the episode
   * file will appear as META-INF/sun-jaxb.episode in the JAR - just as XJC
   * wants it.
   */
  @Parameter (property = "maven.xjc2.episodeFile")
  private File episodeFile;

  public File getEpisodeFile ()
  {
    return episodeFile;
  }

  public void setEpisodeFile (final File episodeFile)
  {
    this.episodeFile = episodeFile;
  }

  /**
   * If true, the episode file (describing mapping of elements and types to
   * classes for the compiled schema) will be generated.
   */
  @Parameter (property = "maven.xjc2.episode", defaultValue = "true")
  private boolean episode = true;

  public boolean getEpisode ()
  {
    return episode;
  }

  public void setEpisode (final boolean episode)
  {
    this.episode = episode;
  }

  /**
   * If <code>true</code> (default), adds <code>if-exists="true"</code>
   * attributes to the <code>bindings</code> elements associated with schemas
   * (via <code>scd="x-schema::..."</code>) in the generated episode files. This
   * is necessary to avoid the annoying `SCD "x-schema::tns" didn't match any
   * schema component` errors.
   */
  @Parameter (property = "maven.xjc2.addIfExistsToEpisodeSchemaBindings", defaultValue = "true")
  private boolean addIfExistsToEpisodeSchemaBindings = true;

  public boolean isAddIfExistsToEpisodeSchemaBindings ()
  {
    return this.addIfExistsToEpisodeSchemaBindings;
  }

  public void setAddIfExistsToEpisodeSchemaBindings (final boolean addIfExistsToEpisodeSchemaBindings)
  {
    this.addIfExistsToEpisodeSchemaBindings = addIfExistsToEpisodeSchemaBindings;
  }

  /**
   * If true, marks generated classes using a @Generated annotation - i.e. turns
   * on XJC -mark-generated option. Default is false.
   */
  @Parameter (property = "maven.xjc2.markGenerated", defaultValue = "false")
  private boolean markGenerated = false;

  public boolean getMarkGenerated ()
  {
    return markGenerated;
  }

  public void setMarkGenerated (final boolean markGenerated)
  {
    this.markGenerated = markGenerated;
  }

  /**
   * XJC plugins to be made available to XJC. They still need to be activated by
   * using &lt;args/&gt; and enable plugin activation option.
   */
  @Parameter
  protected Dependency [] plugins;

  public Dependency [] getPlugins ()
  {
    return plugins;
  }

  public void setPlugins (final Dependency [] plugins)
  {
    this.plugins = plugins;
  }

  @Component
  private ArtifactResolver artifactResolver;

  @Component
  private ArtifactMetadataSource artifactMetadataSource;

  @Component
  private ArtifactFactory artifactFactory;

  /**
   * Location of the local repository.
   */
  @Parameter (defaultValue = "${localRepository}", required = true)
  private ArtifactRepository localRepository;

  /**
   * Artifact factory, needed to download source jars.
   */
  @Component (role = org.apache.maven.project.MavenProjectBuilder.class)
  private MavenProjectBuilder mavenProjectBuilder;

  @Component
  private BuildContext buildContext = new DefaultBuildContext (new org.sonatype.plexus.build.incremental.DefaultBuildContext());

  /**
   * Plugin artifacts.
   */
  @Parameter (defaultValue = "${plugin.artifacts}", required = true)
  private List <org.apache.maven.artifact.Artifact> pluginArtifacts;

  /**
   * If you want to use existing artifacts as episodes for separate compilation,
   * configure them as episodes/episode elements. It is assumed that episode
   * artifacts contain an appropriate META-INF/sun-jaxb.episode resource.
   */
  @Parameter
  private Dependency [] episodes;

  public Dependency [] getEpisodes ()
  {
    return episodes;
  }

  public void setEpisodes (final Dependency [] episodes)
  {
    this.episodes = episodes;
  }

  /**
   * Use all of the compile-scope project dependencies as episode artifacts. It
   * is assumed that episode artifacts contain an appropriate
   * META-INF/sun-jaxb.episode resource. Default is false.
   */
  @Parameter
  private boolean useDependenciesAsEpisodes = false;

  public boolean getUseDependenciesAsEpisodes ()
  {
    return useDependenciesAsEpisodes;
  }

  public void setUseDependenciesAsEpisodes (final boolean useDependenciesAsEpisodes)
  {
    this.useDependenciesAsEpisodes = useDependenciesAsEpisodes;
  }

  /**
   * Scan all compile-scoped project dependencies for XML binding files.
   */
  @Parameter (defaultValue = "false")
  private boolean scanDependenciesForBindings = false;

  public boolean getScanDependenciesForBindings ()
  {
    return scanDependenciesForBindings;
  }

  public void setScanDependenciesForBindings (final boolean scanDependenciesForBindings)
  {
    this.scanDependenciesForBindings = scanDependenciesForBindings;
  }

  /**
   * Version of the JAXB specification (ex. 2.0, 2.1 or 2.2).
   */
  @Parameter (defaultValue = "2.2")
  private String specVersion = "2.2";

  public String getSpecVersion ()
  {
    return specVersion;
  }

  public void setSpecVersion (final String specVersion)
  {
    this.specVersion = specVersion;
  }

  protected void logConfiguration ()
  {

    logApiConfiguration ();

    getLog ().info ("pluginArtifacts:" + getPluginArtifacts ());
    getLog ().info ("specVersion:" + getSpecVersion ());
    getLog ().info ("encoding:" + getEncoding ());
    getLog ().info ("locale:" + getLocale ());
    getLog ().info ("schemaLanguage:" + getSchemaLanguage ());
    getLog ().info ("schemaDirectory:" + getSchemaDirectory ());
    getLog ().info ("schemaIncludes:" + Arrays.toString (getSchemaIncludes ()));
    getLog ().info ("schemaExcludes:" + Arrays.toString (getSchemaExcludes ()));
    getLog ().info ("schemas:" + Arrays.toString (getSchemas ()));
    getLog ().info ("bindingDirectory:" + getBindingDirectory ());
    getLog ().info ("bindingIncludes:" + Arrays.toString (getBindingIncludes ()));
    getLog ().info ("bindingExcludes:" + Arrays.toString (getBindingExcludes ()));
    getLog ().info ("bindings:" + Arrays.toString (getBindings ()));
    getLog ().info ("disableDefaultExcludes:" + getDisableDefaultExcludes ());
    getLog ().info ("catalog:" + getCatalog ());
    getLog ().info ("catalogResolver:" + getCatalogResolver ());
    getLog ().info ("generatePackage:" + getGeneratePackage ());
    getLog ().info ("generateDirectory:" + getGenerateDirectory ());
    getLog ().info ("readOnly:" + getReadOnly ());
    getLog ().info ("extension:" + getExtension ());
    getLog ().info ("strict:" + getStrict ());
    getLog ().info ("writeCode:" + getWriteCode ());
    getLog ().info ("verbose:" + getVerbose ());
    getLog ().info ("debug:" + getDebug ());
    getLog ().info ("args:" + getArgs ());
    getLog ().info ("forceRegenerate:" + getForceRegenerate ());
    getLog ().info ("removeOldOutput:" + getRemoveOldOutput ());
    getLog ().info ("produces:" + Arrays.toString (getProduces ()));
    getLog ().info ("otherDepends:" + getOtherDepends ());
    getLog ().info ("otherDependIncludes:" + getOtherDependsIncludes ());
    getLog ().info ("otherDependExcludes:" + getOtherDependsExcludes ());
    getLog ().info ("episodeFile:" + getEpisodeFile ());
    getLog ().info ("episode:" + getEpisode ());
    getLog ().info ("plugins:" + Arrays.toString (getPlugins ()));
    getLog ().info ("episodes:" + Arrays.toString (getEpisodes ()));
    getLog ().info ("useDependenciesAsEpisodes:" + getUseDependenciesAsEpisodes ());
    getLog ().info ("scanDependenciesForBindings:" + getScanDependenciesForBindings ());
    getLog ().info ("xjcPlugins:" + Arrays.toString (getPlugins ()));
    getLog ().info ("episodes:" + Arrays.toString (getEpisodes ()));
  }

  private static final String XML_SCHEMA_CLASS_NAME = "XmlSchema";

  @Parameter (defaultValue = "${project}", readonly = true)
  private MavenProject project;

  public MavenProject getProject ()
  {
    return project;
  }

  public void setProject (final MavenProject project)
  {
    this.project = project;
  }

  private static final String XML_SCHEMA_CLASS_QNAME = "javax.xml.bind.annotation." + XML_SCHEMA_CLASS_NAME;

  public ArtifactResolver getArtifactResolver ()
  {
    return artifactResolver;
  }

  public void setArtifactResolver (final ArtifactResolver artifactResolver)
  {
    this.artifactResolver = artifactResolver;
  }

  private static final String XML_SCHEMA_RESOURCE_NAME = XML_SCHEMA_CLASS_NAME + ".class";

  public ArtifactMetadataSource getArtifactMetadataSource ()
  {
    return artifactMetadataSource;
  }

  public void setArtifactMetadataSource (final ArtifactMetadataSource artifactMetadataSource)
  {
    this.artifactMetadataSource = artifactMetadataSource;
  }

  private static final String XML_SCHEMA_RESOURCE_QNAME = "/javax/xml/bind/annotation/" + XML_SCHEMA_RESOURCE_NAME;

  public ArtifactFactory getArtifactFactory ()
  {
    return artifactFactory;
  }

  public void setArtifactFactory (final ArtifactFactory artifactFactory)
  {
    this.artifactFactory = artifactFactory;
  }

  private static final String XML_ELEMENT_REF_CLASS_NAME = "XmlElementRef";

  public ArtifactRepository getLocalRepository ()
  {
    return localRepository;
  }

  public void setLocalRepository (final ArtifactRepository localRepository)
  {
    this.localRepository = localRepository;
  }

  private static final String XML_ELEMENT_REF_CLASS_QNAME = "javax.xml.bind.annotation." + XML_ELEMENT_REF_CLASS_NAME;

  public MavenProjectBuilder getMavenProjectBuilder ()
  {
    return mavenProjectBuilder;
  }

  public void setMavenProjectBuilder (final MavenProjectBuilder mavenProjectBuilder)
  {
    this.mavenProjectBuilder = mavenProjectBuilder;
  }

  public BuildContext getBuildContext ()
  {
    return buildContext;
  }

  public void setBuildContext (final BuildContext buildContext)
  {
    this.buildContext = buildContext;
  }

  protected void logApiConfiguration ()
  {

    try
    {
      final Class <?> xmlSchemaClass = Class.forName (XML_SCHEMA_CLASS_QNAME);

      final URL resource = xmlSchemaClass.getResource (XML_SCHEMA_RESOURCE_NAME);

      final String draftLocation = resource.toExternalForm ();
      final String location;
      if (draftLocation.endsWith (XML_SCHEMA_RESOURCE_QNAME))
      {
        location = draftLocation.substring (0, draftLocation.length () - XML_SCHEMA_RESOURCE_QNAME.length ());
      }
      else
      {
        location = draftLocation;
      }
      getLog ().info ("JAXB API is loaded from the [" + location + "].");

      try
      {
        xmlSchemaClass.getMethod ("location");

        final Class <?> xmlElementRefClass = Class.forName (XML_ELEMENT_REF_CLASS_QNAME);

        try
        {
          xmlElementRefClass.getMethod ("required");
          getLog ().info ("Detected JAXB API version [2.2].");
        }
        catch (final NoSuchMethodException nsmex2)
        {
          getLog ().info ("Detected JAXB API version [2.1].");
        }
      }
      catch (final NoSuchMethodException nsmex1)
      {
        getLog ().info ("Detected JAXB API version [2.0].");

      }
    }
    catch (final ClassNotFoundException cnfex)
    {
      getLog ().error ("Could not find JAXB 2.x API classes. Make sure JAXB 2.x API is on the classpath.");
    }
  }

  public List <org.apache.maven.artifact.Artifact> getPluginArtifacts ()
  {
    return pluginArtifacts;
  }

  public void setPluginArtifacts (final List <org.apache.maven.artifact.Artifact> plugingArtifacts)
  {
    this.pluginArtifacts = plugingArtifacts;
  }

  public List <Dependency> getProjectDependencies ()
  {

    final Set <Artifact> artifacts = getProject ().getArtifacts ();

    if (artifacts == null)
    {
      return Collections.emptyList ();
    }
    final List <Dependency> dependencies = new ArrayList <> (artifacts.size ());
    for (final Artifact artifact : artifacts)
    {
      final Dependency dependency = new Dependency ();
      dependency.setGroupId (artifact.getGroupId ());
      dependency.setArtifactId (artifact.getArtifactId ());
      dependency.setVersion (artifact.getVersion ());
      dependency.setClassifier (artifact.getClassifier ());
      dependency.setScope (artifact.getScope ());
      dependency.setType (artifact.getType ());
      dependencies.add (dependency);
    }
    return dependencies;
  }

  protected List <URI> createResourceEntryUris (final ResourceEntry resourceEntry,
                                                final String defaultDirectory,
                                                final String [] defaultIncludes,
                                                final String [] defaultExcludes) throws MojoExecutionException
  {
    if (resourceEntry == null)
    {
      return Collections.emptyList ();
    }

    final List <URI> uris = new LinkedList <> ();
    if (resourceEntry.getFileset () != null)
    {
      final FileSet fileset = resourceEntry.getFileset ();
      uris.addAll (_createFileSetUris (fileset, defaultDirectory, defaultIncludes, defaultExcludes));
    }
    if (resourceEntry.getUrl () != null)
    {
      final String urlDraft = resourceEntry.getUrl ();
      uris.add (_createUri (urlDraft));
    }
    if (resourceEntry.getDependencyResource () != null)
    {
      final String systemId = resourceEntry.getDependencyResource ().getSystemId ();
      try
      {
        final URI uri = new URI (systemId);
        uris.add (uri);
      }
      catch (final URISyntaxException e)
      {
        throw new MojoExecutionException (MessageFormat.format ("Could not create the resource entry URI from the following system id: [{0}].",
                                                                systemId),
                                          e);
      }
    }
    return uris;
  }

  public URL resolveDependencyResource (final DependencyResource dependencyResource) throws MojoExecutionException
  {
    if (dependencyResource.getGroupId () == null)
    {
      throw new MojoExecutionException (MessageFormat.format ("Dependency resource [{0}] does define the groupId.",
                                                              dependencyResource));
    }

    if (dependencyResource.getArtifactId () == null)
    {
      throw new MojoExecutionException (MessageFormat.format ("Dependency resource [{0}] does not define the artifactId.",
                                                              dependencyResource));
    }

    if (dependencyResource.getType () == null)
    {
      throw new MojoExecutionException (MessageFormat.format ("Dependency resource [{0}] does not define the type.",
                                                              dependencyResource));
    }

    if (getProject ().getDependencyManagement () != null)
    {
      final List <Dependency> dependencies = getProject ().getDependencyManagement ().getDependencies ();
      _merge (dependencyResource, dependencies);
    }

    final List <Dependency> dependencies = getProjectDependencies ();
    if (dependencies != null)
    {
      _merge (dependencyResource, dependencies);
    }

    if (dependencyResource.getVersion () == null)
    {
      throw new MojoExecutionException (MessageFormat.format ("Dependency resource [{0}] does not define the version.",
                                                              dependencyResource));
    }

    try
    {
      final Set <Artifact> artifacts = MavenMetadataSource.createArtifacts (getArtifactFactory (),
                                                                            Arrays.asList (dependencyResource),
                                                                            Artifact.SCOPE_RUNTIME,
                                                                            null,
                                                                            getProject ());

      if (artifacts.size () != 1)
      {
        getLog ().error ("Resolved dependency resource [" +
                         dependencyResource +
                         "] to " +
                         artifacts.size () +
                         " artifacts [" +
                         artifacts +
                         "].");
        throw new MojoExecutionException ("Could not create artifact for dependency [" + dependencyResource + "].");
      }

      final Artifact artifact = artifacts.iterator ().next ();

      getArtifactResolver ().resolve (artifact, getProject ().getRemoteArtifactRepositories (), getLocalRepository ());

      final String resource = dependencyResource.getResource ();
      if (resource == null)
      {
        throw new MojoExecutionException ("Dependency resource [" +
                                          dependencyResource +
                                          "] does not define the resource.");
      }
      final URL resourceURL = _createArtifactResourceUrl (artifact, resource);
      getLog ().debug ("Resolved dependency resource [" +
                       dependencyResource +
                       "] to resource URL [" +
                       resourceURL +
                       "].");
      return resourceURL;
    }
    catch (final ArtifactNotFoundException anfex)
    {
      throw new MojoExecutionException (MessageFormat.format ("Could not find artifact for dependency [{0}].",
                                                              dependencyResource));

    }
    catch (final InvalidDependencyVersionException e)
    {
      throw new MojoExecutionException (MessageFormat.format ("Invalid version of dependency [{0}].",
                                                              dependencyResource));
    }
    catch (final ArtifactResolutionException e)
    {
      throw new MojoExecutionException (MessageFormat.format ("Could not resolver artifact for dependency [{0}].",
                                                              dependencyResource));
    }
  }

  private URL _createArtifactResourceUrl (final Artifact artifact, final String resource) throws MojoExecutionException
  {
    final File artifactFile = artifact.getFile ();

    if (artifactFile.isDirectory ())
    {
      final File resourceFile = new File (artifactFile, resource);
      try
      {
        return resourceFile.toURI ().toURL ();
      }
      catch (final MalformedURLException murlex)
      {
        throw new MojoExecutionException (MessageFormat.format ("Could not create an URL for dependency directory [{0}] and resource [{1}].",
                                                                artifactFile,
                                                                resource));
      }
    }
    try
    {
      return new URL ("jar:" + artifactFile.toURI ().toURL ().toExternalForm () + "!/" + resource);
    }
    catch (final MalformedURLException murlex)
    {
      throw new MojoExecutionException (MessageFormat.format ("Could not create an URL for dependency file [{0}] and resource [{1}].",
                                                              artifactFile,
                                                              resource));

    }
  }

  private URI _createUri (final String uriString) throws MojoExecutionException
  {
    try
    {
      final URI uri = new URI (uriString);
      return uri;
    }
    catch (final URISyntaxException urisex)
    {
      throw new MojoExecutionException (MessageFormat.format ("Could not create the URI from string [{0}].", uriString),
                                        urisex);
    }
  }

  private List <URI> _createFileSetUris (final FileSet fileset,
                                         final String defaultDirectory,
                                         final String [] defaultIncludes,
                                         final String defaultExcludes[]) throws MojoExecutionException
  {
    final String draftDirectory = fileset.getDirectory ();
    final String directory = draftDirectory == null ? defaultDirectory : draftDirectory;
    final List <String> includes;
    final List <String> draftIncludes = fileset.getIncludes ();
    if (draftIncludes == null || draftIncludes.isEmpty ())
    {
      includes = defaultIncludes == null ? Collections.<String> emptyList () : Arrays.asList (defaultIncludes);
    }
    else
    {
      includes = draftIncludes;
    }

    final List <String> excludes;
    final List <String> draftExcludes = fileset.getExcludes ();
    if (draftExcludes == null || draftExcludes.isEmpty ())
    {
      excludes = defaultExcludes == null ? Collections.<String> emptyList () : Arrays.asList (defaultExcludes);
    }
    else
    {
      excludes = draftExcludes;
    }
    final String [] includesArray = includes.toArray (new String [includes.size ()]);
    final String [] excludesArray = excludes.toArray (new String [excludes.size ()]);
    try
    {
      final List <File> files = IOUtils.scanDirectoryForFiles (getBuildContext (),
                                                               new File (directory),
                                                               includesArray,
                                                               excludesArray,
                                                               !getDisableDefaultExcludes (),
                                                               null);

      final List <URI> uris = new ArrayList <> (files.size ());

      for (final File file : files)
      {
        // try {
        final URI uri = file.toURI ();
        uris.add (uri);
        // } catch (MalformedURLException murlex) {
        // throw new MojoExecutionException(
        // MessageFormat.format(
        // "Could not create an URL for the file [{0}].",
        // file), murlex);
        // }
      }
      return uris;
    }
    catch (final IOException ioex)
    {
      throw new MojoExecutionException (MessageFormat.format ("Could not scan directory [{0}] for files with inclusion [{1}]  and exclusion [{2}].",
                                                              directory,
                                                              includes,
                                                              excludes));
    }
  }

  private void _merge (final Dependency dependency, final List <Dependency> managedDependencies)
  {

    for (final Dependency managedDependency : managedDependencies)
    {
      if (dependency.getManagementKey ().equals (managedDependency.getManagementKey ()))
      {
        ArtifactUtils.mergeDependencyWithDefaults (dependency, managedDependency);
      }
    }
  }

  protected abstract IOptionsFactory <O> getOptionsFactory ();

  protected void cleanPackageDirectory (final File packageDirectory)
  {
    final File [] files = packageDirectory.listFiles ((FileFilter) aPathname -> aPathname.isFile ());
    if (files != null)
    {
      for (final File file : files)
      {
        file.delete ();
      }
    }
  }

}
