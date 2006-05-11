/*---------------------------------------------------------------------------*\
  $Id: OutputHandler.java 5749 2006-03-24 16:21:51Z bmc $
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

import java.io.File;

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
 * @see AbstractPlugIn
 * @see Curn
 *
 * @version <tt>$Revision: 5749 $</tt>
 */
public class PlugInManager
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For log messages
     */
    private static Logger log = new Logger (PlugInManager.class);

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
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Load the plug-ins and create the {@link MetaPlugIn} singleton.
     *
     * @throws CurnException on error
     */
    public static void loadPlugIns()
        throws CurnException
    {
        MetaPlugIn metaPlugIn = MetaPlugIn.getMetaPlugIn();

        // Try <install-path>/plugins, then $HOME/curn/plugins, then
        // $HOME/.curn/plugins

        String curnHome = System.getProperty ("org.clapper.curn.home");
        loadPlugInsFromDirectory (new File (curnHome, "plugins"));

        String userHome = System.getProperty ("user.home");
        File userCurn = new File (userHome, "curn");
        loadPlugInsFromDirectory (new File (userCurn, "plugins"));

        userCurn = new File (userHome, ".curn");
        loadPlugInsFromDirectory (new File (userCurn, "plugins"));
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Load all plug-ins in a given directory.
     *
     * @throws CurnException on error
     */
    private static void loadPlugInsFromDirectory (File dir)
        throws CurnException
    {
        if (! dir.exists())
        {
            log.error ("Plug-in directory \"" + dir.getPath() +
                       "\" does not exist.");
        }

        else if (! dir.isDirectory())
        {
            log.error ("Plug-in directory \"" + dir.getPath() +
                       "\" is not a directory.");
        }

        else
        {
            log.debug ("Loading plug-ins from directory \"" +
                       dir.getPath() + "\"");
        }
    }
}
