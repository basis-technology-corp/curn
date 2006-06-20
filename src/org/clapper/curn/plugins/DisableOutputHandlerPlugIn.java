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

import org.clapper.curn.ConfiguredOutputHandler;
import org.clapper.curn.Constants;
import org.clapper.curn.Curn;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.OutputHandlerConfigItemPlugIn;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.io.FileUtil;
import org.clapper.util.logging.Logger;

/**
 * The <tt>DisableOutputHandlerPlugIn</tt> handles disabling an output handler. It
 * intercepts the following per-handler configuration parameters:
 *
 * <table border="1">
 *   <tr valign="top">
 *     <td><tt>Disabled</tt></td>
 *     <td>Flag indicating whether or not to disable the output handler.</td>
 *   </tr>
 *   <tr valign="top">
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class DisableOutputHandlerPlugIn
    implements OutputHandlerConfigItemPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    /**
     * For log messages
     */
    private static Logger log = new Logger (DisableOutputHandlerPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public DisableOutputHandlerPlugIn()
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
        return "Disable Output Handler";
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
     * configuration item in an output handler configuration section. All
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
     * @param handler      partially complete {@link ConfiguredOutputHandler}
     *                     object. The class name is guaranteed to be set,
     *                     but the other fields may not be.
     * 
     * @return <tt>true</tt> to continue processing the handler,
     *         <tt>false</tt> to skip it
     *
     * @throws CurnException on error
     *
     * @see CurnConfig
     * @see ConfiguredOutputHandler
     */
    public boolean
    runOutputHandlerConfigItemPlugIn (String                  sectionName,
                                      String                  paramName,
                                      CurnConfig              config,
                                      ConfiguredOutputHandler handler)
	throws CurnException
    {
        boolean keepGoing = true;

        try
        {
            if (paramName.equals (CurnConfig.VAR_DISABLED))
            {
                boolean disable = config.getRequiredBooleanValue (sectionName,
                                                                  paramName);
                log.debug ("[" + sectionName + "]: " + paramName +
                           "=" + disable);

                if (disable)
                    keepGoing = false;
            }
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
        }

        return keepGoing;
    }
}
