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

import org.clapper.util.logging.Logger;

import org.clapper.util.io.AndFileFilter;
import org.clapper.util.io.OrFileFilter;
import org.clapper.util.io.RegexFileFilter;
import org.clapper.util.io.DirectoryFilter;
import org.clapper.util.io.FileOnlyFilter;
import org.clapper.util.io.FileFilterMatchType;

import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.ClassModifiersClassFilter;
import org.clapper.util.classutil.AndClassFilter;
import org.clapper.util.classutil.OrClassFilter;
import org.clapper.util.classutil.AbstractClassFilter;
import org.clapper.util.classutil.NotClassFilter;
import org.clapper.util.classutil.RegexClassFilter;
import org.clapper.util.classutil.SubclassClassFilter;

import java.lang.reflect.Modifier;

import java.io.FileFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import java.util.regex.PatternSyntaxException;
import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.classutil.ClassUtilException;

/**
 * Responsible for loading the plug-ins and creating the {@link MetaPlugIn}
 * singleton that's used to run the loaded plug-ins. This functionality is
 * isolated in a separate class to permit implementing a future feature
 * that allows run-time substitution of different implementations of
 * <tt>PlugInManager</tt>.
 *
 * @see PlugIn
 * @see PlugInManager
 * @see MetaPlugIn
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public class PlugInManager
{
    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For log messages
     */
    private static final Logger log = new Logger (PlugInManager.class);

    /**
     * The located PlugIns
     */
    private static Collection<PlugIn> plugIns =
        new TreeSet<PlugIn>(new PlugInComparator());

    /**
     * Whether or not plug-ins are loaded
     */
    private static boolean plugInsLoaded = false;

    /**
     * File filter to use when looking for jars, zip files, and directories.
     */
    private static FileFilter plugInLocFilter = null;

    static
    {
        try
        {
            plugInLocFilter = new OrFileFilter
                (
                 // must be a directory ...

                 new DirectoryFilter(),

                 // or, must be a file ending in .jar

                 new AndFileFilter (new RegexFileFilter
                                        ("\\.jar$",
                                         FileFilterMatchType.FILENAME),
                                    new FileOnlyFilter()),

                 // or, must be a file ending in .zip

                 new AndFileFilter (new RegexFileFilter
                                        ("\\.zip$",
                                         FileFilterMatchType.FILENAME),
                                    new FileOnlyFilter())
                );
        }

        catch (PatternSyntaxException ex)
        {
            // Should not happen.

            assert (false);
        }
    }

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Cannot be instantiated.
     */
    private PlugInManager()
    {
        // Cannot be instantiated.
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the list of loaded plug-ins and output handlers. The plug-ins,
     * which are already instantiated, are storing in the supplied
     * <tt>Map</tt>. The map is indexed by plug-in displayable name; each
     * value is the corresponding <tt>PlugIn</tt> class. The plug-in
     * <tt>OutputHandler</tt> classes are not loaded, so they're stored
     * in a <tt>Map</tt> that's indexed by short class name.
     *
     * @param plugInMap        the map for plug-ins
     *
     * @throws CurnException on error
     */
    static void listPlugIns (final Map<String,Class> plugInMap)
        throws CurnException
    {
        loadPlugIns();

        for (PlugIn plugIn : plugIns)
        {
            String plugInName = plugIn.getPlugInName();
            if (plugInName != null)
                plugInMap.put(plugInName, plugIn.getClass());
        }
    }

    /**
     * Load the plug-ins and create the {@link MetaPlugIn} singleton.
     *
     * @throws CurnException on error
     */
    static void loadPlugIns()
        throws CurnException
    {
        if (! plugInsLoaded)
        {
            MetaPlugIn.createMetaPlugIn();

            ClassFinder classFinder = new ClassFinder();

            // Assumes CLASSPATH has been set appropriately by the
            // Bootstrap class. It's necessary to do it this way to support
            // the alternate class loader.

            classFinder.addClassPath();

            // Configure the ClassFinder's filter for plug-in classes and
            // output handler classes. Note that the criteria for both are
            // slightly different, but we search for them at the same time
            // for performance reasons.

            ClassFilter classFilter =
                new OrClassFilter
                (
                    // Plug-ins

                    new AndClassFilter
                    (
                        // Must implement org.clapper.curn.PlugIn

                        new SubclassClassFilter (PlugIn.class),

                        // Must be concrete

                        new NotClassFilter (new AbstractClassFilter()),

                        // Must be public

                        new ClassModifiersClassFilter (Modifier.PUBLIC),

                        // Weed out certain things

                        new NotClassFilter (new RegexClassFilter ("^java\\.")),
                        new NotClassFilter (new RegexClassFilter ("^javax\\."))
                    ),

                    // Output handlers

                    new AndClassFilter
                    (
                        // Must implement org.clapper.curn.OutputHandler

                        new SubclassClassFilter (OutputHandler.class),

                        // Must be concrete

                        new NotClassFilter (new AbstractClassFilter()),

                        // Must be public

                        new ClassModifiersClassFilter (Modifier.PUBLIC),

                        // Make sure not to include any of the packaged curn
                        // output handlers.

                        new NotClassFilter
                            (new RegexClassFilter ("^org\\.clapper\\.curn"))
                    )
                );

            Collection<ClassInfo> classes = new ArrayList<ClassInfo>();
            classFinder.findClasses (classes, classFilter);

            // Load any found plug-ins.

            if (classes.size() == 0)
                log.info ("No plug-ins found.");
            else
                loadPlugInClasses (classes);

            plugInsLoaded = true;
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Load the plug-ins. For classes implementing PlugIn, the class is
     * loaded and instantiating. For OutputHandler classes, the class is
     * just loaded; instantiation is done by curn itself (via the
     * OutputHandlerFactory class), if the output handler is actually used
     * in the configuration.
     *
     * @param classes  classes to load
     */
    private static void loadPlugInClasses (final Collection<ClassInfo> classes)
    {
        MetaPlugIn metaPlugIn = MetaPlugIn.getMetaPlugIn();

        int totalPlugInsLoaded = 0;

        for (ClassInfo classInfo : classes)
        {
            String className = classInfo.getClassName();
            try
            {
                // Instantite the plug-in via the default constructor and
                // add it to the meta-plug-in.

                Object obj = ClassUtil.instantiateClass(className);
                if (obj instanceof PlugIn)
                {
                    PlugIn plugIn = (PlugIn) obj;
                    String plugInName = plugIn.getPlugInName();
                    if (plugInName == null)
                        plugInName = className;
                    log.info("Loaded \"" + plugInName + "\" plug-in");
                    metaPlugIn.addPlugIn(plugIn);
                    plugIns.add(plugIn);
                    totalPlugInsLoaded++;
                }
            }

            catch (ClassUtilException ex)
            {
                Throwable cause = ex.getCause();
                if (cause instanceof IllegalAccessException)
                {
                    // Not a big deal. Might be one of ours (e.g., MetaPlugIn).

                    log.info("Plug-in " +
                             classInfo.getClassLocation().getPath() +
                             "(" + className +
                             ") has no accessible default constructor.");
                }

                else
                {
                    log.error("Cannot instantiate plug-in \"" +
                              classInfo.getClassLocation().getPath() +
                              "(" + className + ")",
                              ex);
                }
            }

            catch (ExceptionInInitializerError ex)
            {
                log.error ("Default constructor for plug-in \"" +
                           classInfo.getClassLocation().getPath() +
                           "(" + className + ") threw an exception.",
                           ex.getException());
            }
        }

        if (log.isInfoEnabled())
        {
            int totalClasses = classes.size();
            log.info ("Loaded " +
                      totalPlugInsLoaded +
                      " plug-in" +
                      ((totalPlugInsLoaded == 1) ? "" : "s") +
                      " of " +
                      totalClasses +
                      " plug-in class" +
                      ((totalClasses == 1) ? "" : "es") +
                      " found.");
        }
    }
}
