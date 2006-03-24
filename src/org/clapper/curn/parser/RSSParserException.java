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

package org.clapper.curn.parser;

import org.clapper.curn.CurnException;

/**
 * A <tt>RSSParserException</tt> is thrown by parser implementations
 * to signify parser errors.
 *
 * @see org.clapper.util.misc.NestedException
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSParserException extends CurnException
{
    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /**
     * See JDK 1.5 version of java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor, for an exception with no nested exception and
     * no message.
     */
    public RSSParserException()
    {
	super();
    }

    /**
     * Constructs an exception containing another exception, but no message
     * of its own.
     *
     * @param exception  the exception to contain
     */
    public RSSParserException (Throwable exception)
    {
	super (exception);
    }

    /**
     * Constructs an exception containing an error message, but no
     * nested exception.
     *
     * @param message  the message to associate with this exception
     */
    public RSSParserException (String message)
    {
        super (message);
    }

    /**
     * Constructs an exception containing another exception and a message.
     *
     * @param message    the message to associate with this exception
     * @param exception  the exception to contain
     */
    public RSSParserException (String message, Throwable exception)
    {
	super (message, exception);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #RSSParserException(String,String,String,Object[])}
     * constructor, with a null pointer for the <tt>Object[]</tt>
     * parameter. Calls to
     * {@link org.clapper.util.misc.NestedException#getMessage(java.util.Locale)}
     * will attempt to retrieve the top-most message (i.e., the message
     * from this exception, not from nested exceptions) by querying the
     * named resource bundle. Calls to
     * {@link org.clapper.util.misc.NestedException#printStackTrace(PrintWriter,java.util.Locale)}
     * will do the same, where applicable. The message is not retrieved
     * until one of those methods is called, because the desired locale is
     * passed into <tt>getMessage()</tt> and <tt>printStackTrace()</tt>,
     * not this constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     *
     * @see #RSSParserException(String,String,String,Object[])
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public RSSParserException (String bundleName,
                               String messageKey,
                               String defaultMsg)
    {
        super (bundleName, messageKey, defaultMsg);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #RSSParserException(String,String,String,Object[],Throwable)}
     * constructor, with a null pointer for the <tt>Throwable</tt>
     * parameter. Calls to
     * {@link org.clapper.util.misc.NestedException#getMessage(java.util.Locale)}
     * will attempt to retrieve the top-most message (i.e., the message
     * from this exception, not from nested exceptions) by querying the
     * named resource bundle. Calls to
     * {@link org.clapper.util.misc.NestedException#printStackTrace(PrintWriter,java.util.Locale)}
     * will do the same, where applicable. The message is not retrieved
     * until one of those methods is called, because the desired locale is
     * passed into <tt>getMessage()</tt> and <tt>printStackTrace()</tt>,
     * not this constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param msgParams   parameters to the message, if any, or null
     *
     * @see #RSSParserException(String,String,String,Object[],Throwable)
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public RSSParserException (String   bundleName,
                               String   messageKey,
                               String   defaultMsg,
                               Object[] msgParams)
    {
        super (bundleName, messageKey, defaultMsg, msgParams);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message (in case the resource bundle can't be found), and
     * another exception. Using this constructor is equivalent to calling then
     * {@link #RSSParserException(String,String,String,Object[],Throwable)}
     * constructor, with a null pointer for the <tt>Object[]</tt>
     * parameter. Calls to
     * {@link org.clapper.util.misc.NestedException#getMessage(java.util.Locale)}
     * will attempt to retrieve the top-most message (i.e., the message
     * from this exception, not from nested exceptions) by querying the
     * named resource bundle. Calls to
     * {@link org.clapper.util.misc.NestedException#printStackTrace(PrintWriter,java.util.Locale)}
     * will do the same, where applicable. The message is not retrieved
     * until one of those methods is called, because the desired locale is
     * passed into <tt>getMessage()</tt> and <tt>printStackTrace()</tt>,
     * not this constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param exception   the exception to nest
     *
     * @see #RSSParserException(String,String,String,Object[],Throwable)
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public RSSParserException (String    bundleName,
                               String    messageKey,
                               String    defaultMsg,
                               Throwable exception)
    {
        this (bundleName, messageKey, defaultMsg, null, exception);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message format (in case the resource bundle can't be
     * found), arguments to be incorporated in the message via
     * <tt>java.text.MessageFormat</tt>, and another exception. Calls to
     * {@link org.clapper.util.misc.NestedException#getMessage(java.util.Locale)}
     * will attempt to retrieve the top-most message (i.e., the message
     * from this exception, not from nested exceptions) by querying the
     * named resource bundle. Calls to
     * {@link org.clapper.util.misc.NestedException#printStackTrace(PrintWriter,java.util.Locale)}
     * will do the same, where applicable. The message is not retrieved
     * until one of those methods is called, because the desired locale is
     * passed into <tt>getMessage()</tt> and <tt>printStackTrace()</tt>,
     * not this constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param msgParams   parameters to the message, if any, or null
     * @param exception   exception to be nested
     *
     * @see #RSSParserException(String,String,String,Object[])
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public RSSParserException (String    bundleName,
                               String    messageKey,
                               String    defaultMsg,
                               Object[]  msgParams,
                               Throwable exception)
    {
        super (bundleName, messageKey, defaultMsg, msgParams, exception);
    }
}
