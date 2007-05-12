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

package org.clapper.curn.plugins;

import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.MainConfigItemPlugIn;
import org.clapper.curn.OutputHandler;
import org.clapper.curn.PostOutputPlugIn;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;

import org.clapper.util.io.Zipper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.clapper.curn.CurnUtil;

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
    private static final Logger log = new Logger (ZipOutputPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public ZipOutputPlugIn()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
               Public Methods Required by *PlugIn Interfaces
    \*----------------------------------------------------------------------*/

    /**
     * Get a displayable name for the plug-in.
     *
     * @return the name
     */
    public String getPlugInName()
    {
        return "Zip Output";
    }

    /**
     * Get the sort key for this plug-in.
     *
     * @return the sort key string.
     */
    public String getPlugInSortKey()
    {
        return ClassUtil.getShortClassName (getClass().getName());
    }

    /**
     * Initialize the plug-in. This method is called before any of the
     * plug-in methods are called.
     *
     * @throws CurnException on error
     */
    public void initPlugIn()
        throws CurnException
    {
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
                this.zipFile = CurnUtil.mapConfiguredPathName (zipFilePath);
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

                log.error ("Warning: None of the output handlers " +
                           "produced any zippable output.");
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
