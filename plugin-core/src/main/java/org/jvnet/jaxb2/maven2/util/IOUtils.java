package org.jvnet.jaxb2.maven2.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.build.BuildContext;
import org.xml.sax.InputSource;

public class IOUtils
{

  /**
   * Creates an input source for the given file.
   *
   * @param file
   *        file to create input source for.
   * @return Created input source object.
   */
  public static InputSource getInputSource (final File file)
  {
    try
    {
      final URL url = file.toURI ().toURL ();
      return getInputSource (url);
    }
    catch (final MalformedURLException e)
    {
      return new InputSource (file.getPath ());
    }
  }

  public static InputSource getInputSource (final URL url)
  {
    return new InputSource (StringUtils.escapeSpace (url.toExternalForm ()));
  }

  public static InputSource getInputSource (final URI uri)
  {
    return new InputSource (StringUtils.escapeSpace (uri.toString ()));
  }

  public static final Function <File, URL> GET_URL = file -> {
    try
    {
      return file.toURI ().toURL ();
    }
    catch (final MalformedURLException muex)
    {
      throw new RuntimeException (muex);
    }
  };

  /**
   * Scans given directory for files satisfying given inclusion/exclusion
   * patterns.
   *
   * @param buildContext
   *        Build context provided by the environment, used to scan for files.
   * @param directory
   *        Directory to scan.
   * @param includes
   *        inclusion pattern.
   * @param excludes
   *        exclusion pattern.
   * @param defaultExcludes
   *        default exclusion flag.
   * @param aLogger
   *        Logger
   * @return Files from the given directory which satisfy given patterns. The
   *         files are {@link File#getCanonicalFile() canonical}.
   * @throws IOException
   *         If an I/O error occurs, which is possible because the construction
   *         of the canonical pathname may require filesystem queries.
   */
  public static List <File> scanDirectoryForFiles (final BuildContext buildContext,
                                                   final File directory,
                                                   final String [] includes,
                                                   final String [] excludes,
                                                   final boolean defaultExcludes,
                                                   final Log aLogger) throws IOException
  {
    if (!directory.exists ())
    {
      return Collections.emptyList ();
    }
    final Scanner scanner;

    if (buildContext != null)
    {
      scanner = buildContext.newScanner (directory, true);
      if (aLogger != null)
      {
        aLogger.info ("Created scanner from buildContext: " + scanner);
      }
    }
    else
    {
      final DirectoryScanner directoryScanner = new DirectoryScanner ();
      directoryScanner.setBasedir (directory.getAbsoluteFile ());
      scanner = directoryScanner;
      if (aLogger != null)
        aLogger.info ("Created DirectoryScanner as scanner: " + scanner);
    }
    scanner.setIncludes (includes);
    scanner.setExcludes (excludes);
    if (defaultExcludes)
    {
      scanner.addDefaultExcludes ();
    }

    scanner.scan ();

    if (aLogger != null)
      aLogger.info ("Scanner included files: " + Arrays.toString (scanner.getIncludedFiles ()));

    final List <File> files = new ArrayList <> ();
    for (final String name : scanner.getIncludedFiles ())
    {
      files.add (new File (directory, name).getCanonicalFile ());
    }

    return reorderFiles (files, includes, aLogger);
  }

  private static boolean _isWildcard (final String s)
  {
    return s.indexOf ('*') >= 0 || s.indexOf ('?') >= 0;
  }

  /**
   * Reorder the result of "scanner.getIncludedFile" so that the order of the
   * source includes is maintained as good as possible. Source wildcard matches
   * are postponed to the end.<br>
   * Examples:<br>
   * If the includes contain [a, b, c] and the resulting list should be in that
   * order.<br>
   * If the includes contain [a, b*, c] the resulting list should be [a, c,
   * matches-of(b*)]
   *
   * @param resolvedFiles
   *        resolved files from scanner.getIncludedFiles. May not be
   *        <code>null</code>.
   * @param includes
   *        The source includes in the correct order. May be <code>null</code>
   *        or empty.
   * @param aLogger
   *        Loggert to use. Never <code>null</code>.
   * @return The ordered list of files, that tries to take the source order as
   *         good as possible
   */
  private static List <File> reorderFiles (final List <File> resolvedFiles, final String [] includes, final Log aLogger)
  {
    if (includes == null || includes.length == 0)
    {
      // return "as is"
      return resolvedFiles;
    }

    final List <File> ret = new ArrayList <> (resolvedFiles.size ());
    for (final String inc : includes)
    {
      // Only deal with fixed files
      if (!_isWildcard (inc))
      {
        // Ensure to use the system path separator
        final String sUnifiedInclude = inc.replace ('\\', File.separatorChar).replace ('/', File.separatorChar);

        // Find all matches in the resolved files list
        final List <File> matches = new ArrayList <> ();
        for (final File resFile : resolvedFiles)
          if (resFile.getAbsolutePath ().endsWith (sUnifiedInclude))
            matches.add (resFile);

        if (aLogger != null)
          aLogger.info ("Include '" + inc + "' was unified to '" + sUnifiedInclude + "' and matched to: " + matches);

        for (final File match : matches)
        {
          // Add all matches to the result list
          ret.add (match);

          // Remove from the main list
          resolvedFiles.remove (match);
        }
      }
    }

    // Add all remaining resolved files in the order "as is"
    ret.addAll (resolvedFiles);

    if (aLogger != null)
      aLogger.info ("The resulting List is therefore: " + ret);

    return ret;
  }
}
