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

import java.util.HashMap;
import java.util.Map;

import org.clapper.util.misc.Logger;

/**
 * Contains parsed configuration information for an output handler,
 * including the class name and output handler name (which is really the
 * section name).
 *
 * @see ConfigFile
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public class ConfiguredOutputHandler implements Comparable
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private String        sectionName;
    private String        className;
    private String        name;
    private OutputHandler handler = null;
    private Map           extraVariables = new HashMap();

    /**
     * For log messages
     */
    private static Logger log = new Logger (ConfiguredOutputHandler.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>ConfiguredOutputHandler</tt> object.
     *
     * @param name         a unique name for the handler
     * @param sectionName  the name of the configuration file section
     *                     where the output handler was defined, or null
     *                     if the handler has no corresponding section
     * @param className    the output handler's class name
     */
    public ConfiguredOutputHandler (String name,
                                    String sectionName,
                                    String className)
    {
        log.debug ("section=" + ((sectionName == null) ? "null" : sectionName));
        this.sectionName = sectionName;
        this.className   = className;
        this.name        = name;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the class name associated with this output handler
     *
     * @return the class name
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Get the configuration file section name where this output handler
     * was defined
     *
     * @return the section name, or null for none
     */
    public String getSectionName()
    {
        return sectionName;
    }

    /**
     * Get the unique name for this handler
     *
     * @return the unique name. Should never be null.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the actual <tt>OutputHandler</tt> object, instantiating it if
     * it hasn't already been instantiated.
     *
     * @return the <tt>OutputHandler</tt> object
     *
     * @throws CurnException on error
     */
    public synchronized OutputHandler getOutputHandler()
        throws CurnException
    {
        if (handler == null)
            handler = OutputHandlerFactory.getOutputHandler (className);

        return handler;
    }

    /**
     * Get the extra variables defined with this output handler.
     *
     * @return a <tt>Map</tt> of variables. The map is keyed by variable
     *         name, and each entry is a variable value. The <tt>Map</tt>
     *         will be empty, but never null, if there are no extra variables
     *         for this output handler.
     */
    public Map getExtraVariables()
    {
        return extraVariables;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o the object to compare
     *
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException  if the specified object's type prevents
     *                             it from being compared to this Object.
     */
    public int compareTo (Object o)
        throws ClassCastException
    {
        int cmp = 0;

        if (! (o instanceof ConfiguredOutputHandler))
            cmp = this.hashCode() - o.hashCode();

        else
        {
            ConfiguredOutputHandler other = (ConfiguredOutputHandler) o;

            cmp = this.sectionName.compareTo (other.sectionName);
        }

        return cmp;
    }
    
    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Add an extra variable to the list of extra variables.
     *
     * @param name  the variable name
     * @param value the value
     */
    void addExtraVariable (String name, String value)
    {
        extraVariables.put (name, value);
    }
}
