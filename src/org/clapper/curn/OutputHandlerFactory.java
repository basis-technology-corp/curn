/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004 Brian M. Clapper. All rights reserved.

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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.clapper.util.config.ConfigurationException;

import org.clapper.util.misc.Logger;

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
                            Instance Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For log messages
     */
    private static Logger log = new Logger (OutputHandlerFactory.class);

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get an instance of the named <tt>OutputHandler</tt> class. This
     * method loads the specified class, verifies that it conforms to the
     * {@link OutputHandler} interface, instantiates it (via its default
     * constructor), and returns the resulting <tt>OutputHandler</tt> object.
     *
     * @param cls  the class for the output handler
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
    public static OutputHandler getOutputHandler (Class cls)
        throws ConfigurationException
    {
        try
        {
            log.debug ("Instantiating output handler: " + cls.getName());

            Constructor constructor = cls.getConstructor (null);
            return (OutputHandler) constructor.newInstance (null);
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
            return getOutputHandler (Class.forName (className));
        }

        catch (ClassNotFoundException ex)
        {
            throw new ConfigurationException (ex);
        }
    }
}
