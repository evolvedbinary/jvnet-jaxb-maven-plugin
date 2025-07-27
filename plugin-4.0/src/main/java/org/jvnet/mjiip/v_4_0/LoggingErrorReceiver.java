/**
 *
 */
package org.jvnet.mjiip.v_4_0;

import org.apache.maven.plugin.logging.Log;
import org.xml.sax.SAXParseException;

import com.sun.tools.xjc.ErrorReceiver;

public class LoggingErrorReceiver extends ErrorReceiver
{

  private final Log log;
  private final boolean verbose;
  private final String messagePrefix;

  public LoggingErrorReceiver (final String messagePrefix, final Log log, final boolean verbose)
  {
    this.log = log;
    this.verbose = verbose;
    this.messagePrefix = messagePrefix;
  }

  @Override
  public void warning (final SAXParseException saxex)
  {
    log.warn (getMessage (saxex), saxex);
  }

  @Override
  public void error (final SAXParseException saxex)
  {
    log.error (getMessage (saxex), saxex);
  }

  @Override
  public void fatalError (final SAXParseException saxex)
  {
    log.error (getMessage (saxex), saxex);
  }

  @Override
  public void info (final SAXParseException saxex)
  {
    if (verbose)
      log.info (getMessage (saxex));
  }

  private String getMessage (final SAXParseException ex)
  {
    final int row = ex.getLineNumber ();
    final int col = ex.getColumnNumber ();
    final String sys = ex.getSystemId ();
    final String pub = ex.getPublicId ();

    return messagePrefix +
           "Location [" +
           (sys != null ? " " + sys : "") +
           (pub != null ? " " + pub : "") +
           (row > 0 ? "{" + row + (col > 0 ? "," + col : "") + "}" : "") +
           "].";
  }
}
