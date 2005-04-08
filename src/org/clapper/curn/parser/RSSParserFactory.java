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

package org.clapper.curn.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class provides a factory for retrieving a specific RSS parser
 * implementation.
 *
 * @see RSSParser
 * @see RSSChannel
 * @see RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSParserFactory
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private RSSParserFactory()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the named RSS parser. This method loads the specified class,
     * verifies that it conforms to the {@link RSSParser} interface,
     * instantiates it (via its default constructor), and returns the
     * resulting <tt>RSSParser</tt> object.
     *
     * @param className  the class name
     *
     * @return an <tt>RSSParser</tt> object
     *
     * @throws RSSParserException        Error instantiating class. The
     *                                   exception will contain (i.e., nest)
     *                                   the real underlying exception.
     */
    public static RSSParser getRSSParser (String className)
        throws RSSParserException
    {
        try
        {
            Class parserClass = Class.forName (className);
            Constructor constructor = parserClass.getConstructor();
            return (RSSParser) constructor.newInstance();
        }

        catch (ClassNotFoundException ex)
        {
            throw new RSSParserException (ex);
        }

        catch (NoSuchMethodException ex)
        {
            throw new RSSParserException (ex);
        }

        catch (InvocationTargetException ex)
        {
            throw new RSSParserException (ex);
        }

        catch (InstantiationException ex)
        {
            throw new RSSParserException (ex);
        }

        catch (IllegalAccessException ex)
        {
            throw new RSSParserException (ex);
        }

    }
}
