/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.clapper.util.config.ConfigurationException;

/**
 * Wraps an <tt>OutputHandler</tt>, holding the temporary file and other
 * housekeeping information about an individual output handler.
 *
 * @see OutputHandler
 * @see OutputHandlerFactory
 *
 * @version <tt>$Revision$</tt>
 */
public class OutputHandlerContainer
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private OutputHandler       handler   = null;
    private File                tempFile  = null;
    private FileOutputStream    fileOut   = null;
    private OutputStreamWriter  writerOut = null;
    /**
     * For logging
     */
    private static Logger log = new Logger (OutputHandlerContainer.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>OutputHandlerContainer</tt> object that holds a
     * specific <tt>OutputHandler</tt>.
     *
     * @param handler  the handler
     */
    public OutputHandlerContainer (OutputHandler handler)
    {
        this.handler = handler;
    }

    /*----------------------------------------------------------------------*\
                                Destructor
    \*----------------------------------------------------------------------*/

    /**
     * Cleans up all open files.
     */
    protected void finalize()
    {
        try
        {
            close();

            if (tempFile != null)
            {
                tempFile.delete();
                tempFile = null;
            }
        }

        catch (FeedException ex)
        {
            String className = handler.getClass().getName();
            log.debug ("Failed to close output handler " + className, ex);
        }
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * If the handler wants an output file, this method creates a temporary
     * file for the handler's output and initializes the handler with the
     * temporary file. Otherwise, it just initializes the handler.
     *
     * @param config   the parsed configuration file data
     *
     * @throws ConfigurationException configuration error
     * @throws FeedException          some other error
     */
    public void init (ConfigFile config)
        throws ConfigurationException,
               FeedException
    {
        if (handler.wantsOutputFile())
        {
            try
            {
                tempFile = File.createTempFile ("curn", null);
                tempFile.deleteOnExit();
                fileOut = new FileOutputStream (tempFile);
                writerOut = new OutputStreamWriter (fileOut);
            }

            catch (IOException ex)
            {
                throw new FeedException ("Can't initialize handler", ex);
            }
        }

        else
        {
            writerOut = null;
            fileOut   = null;
            tempFile  = null;
        }

        handler.init (writerOut, config);
    }

    /**
     * Close the output stream, if any. Does not delete the temporary file.
     *
     * @throws FeedException on error
     */
    public void close()
        throws FeedException
    {
        try
        {
            if (writerOut != null)
            {
                writerOut.close();
                writerOut = null;
            }

            if (fileOut != null)
            {
                fileOut.close();
                fileOut = null;
            }
        }

        catch (IOException ex)
        {
            throw new FeedException ("Error closing output.", ex);
        }
    }

    /**
     * Get the <tt>OutputHandler</tt> contained with this container.
     *
     * @return the handler
     */
    public OutputHandler getOutputHandler()
    {
        return handler;
    }

    /**
     * Get the temporary file associated with the handler, if any.
     *
     * @return the temporary file, or null if the handler didn't want a file.
     */
    public File getTempFile()
    {
        return tempFile;
    }

    /**
     * Determine whether there's any output available from the handler.
     *
     * @return <tt>true</tt> if the temporary file contains output from the
     *         handler, <tt>false</tt> if it's empty or nonexistent.
     */
    public boolean hasOutput()
    {
        return ((tempFile != null)&& (tempFile.length() > 0));
    }
}
