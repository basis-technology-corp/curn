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

package org.clapper.curn.plugins;

import org.clapper.curn.Curn;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.MainConfigItemPlugIn;
import org.clapper.curn.OutputHandler;
import org.clapper.curn.PostOutputPlugIn;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;

import org.clapper.util.io.Zipper;
import org.clapper.util.text.TextUtil;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The <tt>ZipOutputPlugIn</tt> handles zipping up the output from a
 * <i>curn</i> run, if one or more email addresses are specified in the
 * configuration file. It intercepts the following main (<tt>[curn]</tt>)
 * section configuration parameter:
 *
 * <table border="1">
 *   <tr valign="top">
 *     <td><tt>ZipOutputTo</tt></td>
 *     <td>The path to the zip file to receive the output files. The zip
 *         file is overwritten if it exists.</td>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class ZipOutputPlugIn
    implements MainConfigItemPlugIn,
               PostOutputPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_ZIP_FILE = "ZipOutputTo";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The zip file
     */
    private File zipFile = null;

    /**
     * For log messages
     */
    private static Logger log = new Logger (ZipOutputPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public ZipOutputPlugIn()
    {
    }

    /*----------------------------------------------------------------------*\
               Public Methods Required by *PlugIn Interfaces
    \*----------------------------------------------------------------------*/

    /**
     * Get a displayable name for the plug-in.
     *
     * @return the name
     */
    public String getName()
    {
        return "Zip Output";
    }

    /**
     * Get the sort key for this plug-in.
     *
     * @return the sort key string.
     */
    public String getSortKey()
    {
        return ClassUtil.getShortClassName (getClass().getName());
    }

    /**
     * Called immediately after <i>curn</i> has read and processed a
     * configuration item in the main [curn] configuration section. All
     * configuration items are passed, one by one, to each loaded plug-in.
     * If a plug-in class is not interested in a particular configuration
     * item, this method should simply return without doing anything. Note
     * that some configuration items may simply be variable assignment;
     * there's no real way to distinguish a variable assignment from a
     * blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param config       the {@link CurnConfig} object
     * 
     * @throws CurnException on error
     *
     * @see CurnConfig
     */
    public void runMainConfigItemPlugIn (String     sectionName,
                                         String     paramName,
                                         CurnConfig config)
	throws CurnException
    {
        try
        {
            if (paramName.equals (VAR_ZIP_FILE))
            {
                String zipFilePath = config.getConfigurationValue (sectionName,
                                                                   paramName);
                this.zipFile = new File (zipFilePath);
            }
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
        }
    }

    /**
     * Called after <i>curn</i> has flushed <i>all</i> output handlers. A
     * post-output plug-in is a useful place to consolidate the output from
     * all output handlers. For instance, such a plug-in might pack all the
     * output into a zip file, or email it.
     *
     * @param outputHandlers a <tt>Collection</tt> of the
     *                       {@link OutputHandler} objects (useful for
     *                       obtaining the output files, for instance).
     *
     * @throws CurnException on error
     *
     * @see OutputHandler
     */
    public void runPostOutputPlugIn (Collection<OutputHandler> outputHandlers)
	throws CurnException
    {
        if (zipFile != null)
        {
            log.debug ("Zipping output to \"" + zipFile.getPath() + "\"");
            zipOutput (outputHandlers);
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Zip the output from all output handlers.
     *
     * @param outputHandlers  the output handlers
     *
     * @throws CurnException on error
     */
    private void zipOutput (Collection<OutputHandler> outputHandlers)
        throws CurnException
    {
        try
        {
            boolean haveFiles = false;

            // First, figure out whether we have any output or not.

            for (OutputHandler handler : outputHandlers)
            {
                if (handler.hasGeneratedOutput())
                {
                    haveFiles = true;
                    break;
                }
            }

            if (! haveFiles)
            {
                // None of the handlers produced any output.

                log.error ("Warning: None of the output handlers "
                         + "produced any zippable output.");
            }

            else
            {
                // Create the zip file.

                Zipper zipper = new Zipper (zipFile, /* flatten */ true);

                for (OutputHandler handler : outputHandlers)
                {
                    File file = handler.getGeneratedOutput();
                    if (file != null)
                    {
                        log.debug ("Zipping \"" + file.getPath() + "\"");
                        zipper.put (file);
                    }
                }

                zipper.close();
            }
        }

        catch (IOException ex)
        {
            throw new CurnException (ex);
        }
    }
}
