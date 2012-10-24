/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2012 Brian M. Clapper.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name "clapper.org", "curn", nor the names of the project's
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/


package org.clapper.curn;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.classutil.ClassUtilException;
import org.clapper.util.config.ConfigurationException;

/**
 * Class that's used to create a {@link DataPersister}. This class is
 * implemented as a plug-in, so that <i>curn</i>'s plug-in discovery
 * mechanism will find it and automatically configure it. The factory
 * methods, though, are static; the plug-in logic merely sets up the data
 * structures that are used by the static methods.
 *
 * @see PlugIn
 * @see DataPersister
 *
 * @version <tt>$Revision$</tt>
 */
public final class DataPersisterFactory
    implements MainConfigItemPlugIn,
               PostConfigPlugIn
{
    /*----------------------------------------------------------------------*\
                               Private Constants
    \*----------------------------------------------------------------------*/

    private static final String CFG_VAR_PERSISTER_CLASS = "DataPersisterClass";
    private static final String DEF_DATA_PERSISTER_CLASS_NAME =
        "org.clapper.curn.XMLDataPersister";

    /*----------------------------------------------------------------------*\
                             Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * The data persister class name.
     */
    private static String dataPersisterClassName =
        DEF_DATA_PERSISTER_CLASS_NAME;

    /**
     * The data persister object to use.
     */
    private static DataPersister dataPersisterInstance = null;

    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new instance of DataPersisterFactory
     */
    public DataPersisterFactory()
    {
    }

    /*----------------------------------------------------------------------*\
                                Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the configured data persister object.
     *
     * @return the configured data persister.
     */
    public static DataPersister getInstance()
    {
        return dataPersisterInstance;
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
     * Get a displayable name for the plug-in.
     *
     * @return the name
     */
    public String getPlugInName()
    {
        return null; // "invisible" plug-in
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
            if (paramName.equals(CFG_VAR_PERSISTER_CLASS))
            {
                dataPersisterClassName =
                    config.getConfigurationValue(sectionName, paramName);
            }
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException(ex);
        }
    }

    /**
     * Called after the entire configuration has been read and parsed, but
     * before any feeds are processed. Intercepting this event is useful
     * for plug-ins that want to adjust the configuration. For instance,
     * the <i>curn</i> command-line wrapper intercepts this plug-in event
     * so it can adjust the configuration to account for command line
     * options.
     *
     * @param config  the parsed {@link CurnConfig} object
     *
     * @throws CurnException on error
     *
     * @see CurnConfig
     */
    public void runPostConfigPlugIn(CurnConfig config)
        throws CurnException
    {
        synchronized (DEF_DATA_PERSISTER_CLASS_NAME)
        {
            if (dataPersisterInstance == null)
            {
                try
                {
                    // Try to load and instantiate the data persister class.

                    dataPersisterInstance =
                        (DataPersister) ClassUtil.instantiateClass
                            (dataPersisterClassName);
                    dataPersisterInstance.init(config);
                }

                catch (ClassUtilException ex)
                {
                    throw new CurnException("Can't instantiate data " +
                                            "persister class " +
                                            dataPersisterClassName,
                                            ex);
                }
            }
        }
    }

    /*----------------------------------------------------------------------*\
                               Protected Methods
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                Private Methods
    \*----------------------------------------------------------------------*/
}
