/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.clapper.util.config.ConfigurationException;

/**
 * This class provides a factory for retrieving a specific
 * <tt>OutputHandler</tt> implementation.
 *
 * @see OutputHandler
 *
 * @version <tt>$Revision$</tt>
 */
public class OutputHandlerFactory
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get an instance of the named <tt>OutputHandler</tt> class. This
     * method loads the specified class, verifies that it conforms to the
     * {@link OutputHandler} interface, instantiates it (via its default
     * constructor), and returns the resulting <tt>OutputHandler</tt> object.
     *
     * @param className  the class name
     *
     * @return an <tt>OutputHandler</tt> object
     *
     * @throws ConfigurationException Error instantiating class. The
     *                                exception will contain (i.e., nest)
     *                                the real underlying exception.
     *                                (<tt>ConfigurationException</tt> is
     *                                thrown because it's unlikely to get
     *                                here unless an incorrect class name
     *                                was placed in the config file.)
     */
    public static OutputHandler getOutputHandler (String className)
        throws ConfigurationException
    {
        try
        {
            Class cls = Class.forName (className);
            Constructor constructor = cls.getConstructor (null);
            return (OutputHandler) constructor.newInstance (null);
        }

        catch (ClassNotFoundException ex)
        {
            throw new ConfigurationException (ex);
        }

        catch (NoSuchMethodException ex)
        {
            throw new ConfigurationException (ex);
        }

        catch (InvocationTargetException ex)
        {
            throw new ConfigurationException (ex);
        }

        catch (InstantiationException ex)
        {
            throw new ConfigurationException (ex);
        }

        catch (IllegalAccessException ex)
        {
            throw new ConfigurationException (ex);
        }
    }
}
