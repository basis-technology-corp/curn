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

import org.clapper.curn.Curn;
import org.clapper.curn.CurnException;

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
import org.clapper.util.classutil.InterfaceOnlyClassFilter;
import org.clapper.util.classutil.NotClassFilter;
import org.clapper.util.classutil.RegexClassFilter;
import org.clapper.util.classutil.SubclassClassFilter;
import org.clapper.util.classutil.ClassLoaderBuilder;

import java.lang.reflect.Modifier;

import java.io.File;
import java.io.FileFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import java.util.regex.PatternSyntaxException;

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
                                 Constants
    \*----------------------------------------------------------------------*/

    private static final String CURN_HOME_ENV_VAR  = "CURN_HOME";
    private static final String CURN_HOME_PROPERTY = "org.clapper.curn.home";
    private static final String BUNDLE_NAME = "org.clapper.curn.PlugInManager";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For log messages
     */
    private static Logger log = new Logger (PlugInManager.class);

    /**
     * List of located plug-in jars, zips, and subdirectories
     */
    private static Collection<File> plugInLocations = new ArrayList<File>();

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
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Load the plug-ins and create the {@link MetaPlugIn} singleton.
     *
     * @throws CurnException on error
     */
    static void loadPlugIns()
        throws CurnException
    {
        MetaPlugIn metaPlugIn = MetaPlugIn.createMetaPlugIn();

        ClassFinder classFinder = new ClassFinder();
        classFinder.add (plugInLocations);
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
    private static void loadPlugInClasses (Collection<ClassInfo> classes)
    {
        MetaPlugIn metaPlugIn = MetaPlugIn.getMetaPlugIn();

        int totalPlugInsLoaded = 0;
        int totalOutputHandlersLoaded = 0;

        for (ClassInfo classInfo : classes)
        {
            String className = classInfo.getClassName();
            try
            {
                Class cls = Class.forName (className);

                if (OutputHandler.class.isAssignableFrom (cls))
                {
                    // It's an output handler. We're done, since the class
                    // is now loaded.

                    log.info ("Loaded output handler class \""
                            + className
                            + "\"");
log.info ("Output handler class loader=" + cls.getClassLoader().toString());
                    totalOutputHandlersLoaded++;
                }

                else
                {
                    // It must be a plug-in. Instantite the plug-in via the
                    // default constructor and add it to the meta-plug-in

                    PlugIn plugIn = (PlugIn) cls.newInstance();
                    log.info ("Loaded \""
                            + plugIn.getName()
                            + "\" plug-in");
                    metaPlugIn.addPlugIn (plugIn);
                    totalPlugInsLoaded++;
                }
            }

            catch (ClassNotFoundException ex)
            {
                log.error ("Can't load "
                         + classInfo.getClassLocation().getPath()
                         + "("
                         + className
                         + "): "
                         + ex.toString());
            }

            catch (ClassCastException ex)
            {
                log.error ("Can't load plug-in \""
                         + classInfo.getClassLocation().getPath()
                         + "("
                         + className
                         + "): "
                         + ex.toString());
            }

            catch (IllegalAccessException ex)
            {
                // Not a big deal. Might be one of ours (e.g., MetaPlugIn).

                log.info ("Plug-in "
                         + classInfo.getClassLocation().getPath()
                         + "("
                         + className
                         + ") has no accessible default constructor.");
            }

            catch (InstantiationException ex)
            {
                log.error ("Can't instantiate plug-in \""
                         + classInfo.getClassLocation().getPath()
                         + "("
                         + className
                         + "): "
                         + ex.toString());
            }

            catch (ExceptionInInitializerError ex)
            {
                log.error ("Default constructor for plug-in \""
                         + classInfo.getClassLocation().getPath()
                         + "("
                         + className
                         + ") threw an exception.",
                           ex.getException());
            }
        }

        if (log.isInfoEnabled())
        {
            int totalClasses = classes.size();
            log.info ("Loaded "
                    + totalPlugInsLoaded
                    + " plug-in"
                    + ((totalPlugInsLoaded == 1) ? "" : "s")
                    + " and "
                    + totalOutputHandlersLoaded
                    + " output handler"
                    + ((totalOutputHandlersLoaded == 1) ? "" : "s")
                    + " of "
                    + totalClasses
                    + " plug-in class"
                    + ((totalClasses == 1) ? "" : "es")
                    + " found.");
        }
    }
}
