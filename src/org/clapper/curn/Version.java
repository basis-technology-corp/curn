/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M. Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.io.PrintWriter;
import java.io.PrintStream;
import java.util.Locale;

import org.clapper.util.misc.BuildInfo;
import org.clapper.util.misc.BundleUtil;

/**
 * <p>Contains the software version for the <i>org.clapper.util</i>
 * library. Also contains a main program which, invoked, displays the
 * name of the API and the version on standard output.</p>
 *
 * @version <tt>$Revision$</tt>
 *
 * @author Copyright &copy; 2004-2007 Brian M. Clapper
 */
public final class Version
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * The name of the resource bundle containing the build info.
     */
    public static final String BUILD_INFO_BUNDLE_NAME =
        "org.clapper.curn.BuildInfoBundle";

    /**
     * This class's bundle
     */
    public static final String VERSION_BUNDLE_NAME = Constants.BUNDLE_NAME;

    /*----------------------------------------------------------------------*\
                                Static Data
    \*----------------------------------------------------------------------*/

    private static BuildInfo buildInfo = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private Version()
    {
        // Cannot be instantiated.
    }

    /*----------------------------------------------------------------------*\
                               Main Program
    \*----------------------------------------------------------------------*/

    /**
     * Get just the version number string.
     *
     * @return the version number string
     *
     * @see #getFullVersion
     */
    public static String getVersionNumber()
    {
        return BundleUtil.getString (VERSION_BUNDLE_NAME, "curn.version", "?");
    }

    /**
     * Get <i>curn</i>'s official name, for display purposes.
     *
     * @return the official name
     */
    public static String getUtilityName()
    {
        return BundleUtil.getString (VERSION_BUNDLE_NAME, "curn.name", "curn");
    }

    /**
     * Get the web site where <i>curn</i> can be found.
     *
     * @return the web site string
     */
    public static String getWebSite()
    {
        return BundleUtil.getString (VERSION_BUNDLE_NAME, "curn.website", "?");
    }

    /**
     * Get the copyright.
     *
     * @return the copyright string
     */
    public static String getCopyright()
    {
        return BundleUtil.getString (VERSION_BUNDLE_NAME,
                                     "curn.copyright",
                                     "?");
    }

    /**
     * Get the full program version string, which contains the program
     * name, the version number and the build ID string. This is the string
     * that the {@link #showVersion()} method displays.
     *
     * @param locale the locale to use, or null for the default
     *
     * @return the full version string
     *
     * @see #showVersion()
     * @see #showVersion(PrintWriter)
     * @see #showVersion(PrintStream)
     * @see #getVersionNumber
     */
    public static String getFullVersion (final Locale locale)
    {
        String    name = getUtilityName();
        String    version = getVersionNumber();
        BuildInfo buildInfo = getBuildInfo();

        return BundleUtil.getMessage (VERSION_BUNDLE_NAME, locale,
                                      "curn.fullVersion",
                                      "{0}, version {1} (build ID {2})",
                                      new Object[]
                                      {
                                          name,
                                          version,
                                          buildInfo.getBuildID()
                                      });
    }

    /**
     * Get the full program version string, which contains the program
     * name, the version number and the build ID string. This is the string
     * that the {@link #showVersion()} method displays. This method assumes
     * the default locale.
     *
     * @return the full version string
     *
     * @see #showVersion()
     * @see #showVersion(PrintWriter)
     * @see #showVersion(PrintStream)
     * @see #getVersionNumber
     */
    public static String getFullVersion()
    {
        return getFullVersion (null);
    }

    /**
     * Get the build ID. Calling this method is equivalent to:
     * <pre>getBuildInfo().getBuildID();</pre>
     *
     * @return the build ID string
     *
     * @see #getBuildInfo
     */
    public static String getBuildID()
    {
        return getBuildInfo().getBuildID();
    }

    /**
     * Display version information only to standard output.
     *
     * @see #showVersion(PrintWriter)
     * @see #showVersion(PrintStream)
     */
    public static void showVersion()
    {
        showVersion (System.out);
    }

    /**
     * Display version information to the specified <tt>PrintWriter</tt>.
     *
     * @param out  where to write the version string
     *
     * @see #showVersion()
     * @see #showVersion(PrintStream)
     */
    public static void showVersion (final PrintWriter out)
    {
        out.println (getFullVersion (null));
    }

    /**
     * Display version information to the specified <tt>PrintStream</tt>.
     *
     * @param out  where to write the version string
     *
     * @see #showVersion()
     * @see #showVersion(PrintWriter)
     */
    public static void showVersion (final PrintStream out)
    {
        out.println (getFullVersion (null));
    }

    /**
     * Display build information to standard output.
     *
     * @see #showBuildInfo(PrintWriter)
     * @see #showBuildInfo(PrintStream)
     * @see #getBuildInfo
     */
    public static void showBuildInfo()
    {
        showBuildInfo (System.out);
    }

    /**
     * Display build information to the specified <tt>PrintStream</tt>.
     *
     * @param out  where to write the build information
     *
     * @see #showBuildInfo()
     * @see #showBuildInfo(PrintWriter)
     * @see #getBuildInfo
     */
    public static void showBuildInfo (final PrintStream out)
    {
        showBuildInfo (new PrintWriter (out));
    }

    /**
     * Display build information to the specified <tt>PrintWriter</tt>.
     *
     * @param out  where to write the build information
     *
     * @see #showBuildInfo()
     * @see #showBuildInfo(PrintStream)
     * @see #getBuildInfo
     */
    public static void showBuildInfo (final PrintWriter out)
    {
        BuildInfo buildInfo = getBuildInfo();

        showVersion(out);
        out.println(getCopyright());
        out.println();
        out.println("Build:          " + buildInfo.getBuildID());
        out.println("Build date:     " + buildInfo.getBuildDate());
        out.println("Built by:       " + buildInfo.getBuildUserID());
        out.println("Built on:       " + buildInfo.getBuildOperatingSystem());
        out.println("Build Java VM:  " + buildInfo.getBuildJavaVM());
        out.println("Build compiler: " + buildInfo.getBuildJavaCompiler());
        out.println("Ant version:    " + buildInfo.getBuildAntVersion());
        out.flush();
    }

    /**
     * Get the <tt>BuildInfo</tt> object that holds the build information
     * data.
     *
     * @return the <tt>BuildInfo</tt> object.
     *
     * @see #showBuildInfo()
     * @see #showBuildInfo(PrintStream)
     * @see #showBuildInfo(PrintWriter)
     */
    public static BuildInfo getBuildInfo()
    {
        synchronized (Version.class)
        {
            if (buildInfo == null)
                buildInfo = new BuildInfo (BUILD_INFO_BUNDLE_NAME);
        }

        return buildInfo;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

}
