/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms are permitted provided
  that: (1) source distributions retain this entire copyright notice and
  comment; and (2) modifications made to the software are prominently
  mentioned, and a copy of the original software (or a pointer to its
  location) are included. The name of the author may not be used to endorse
  or promote products derived from this software without specific prior
  written permission.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
  WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

  Effectively, this means you can do what you want with the software except
  remove this notice or take advantage of the author's name. If you modify
  the software and redistribute your modified version, you must indicate that
  your version is a modification of the original, and you must provide either
  a pointer to or a copy of the original.
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
 * @author Copyright &copy; 2004-2006 Brian M. Clapper
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

        showVersion (out);
        out.println ();
        out.println ("Build:          " + buildInfo.getBuildID());
        out.println ("Build date:     " + buildInfo.getBuildDate());
        out.println ("Built by:       " + buildInfo.getBuildUserID());
        out.println ("Built on:       " + buildInfo.getBuildOperatingSystem());
        out.println ("Build Java VM:  " + buildInfo.getBuildJavaVM());
        out.println ("Build compiler: " + buildInfo.getBuildJavaCompiler());
        out.println ("Ant version:    " + buildInfo.getBuildAntVersion());
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
