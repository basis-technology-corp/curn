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
      without prior written permission of Brian M.a Clapper.

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

package org.clapper.curn;

import java.util.HashMap;
import java.util.Map;

import org.clapper.util.logging.Logger;

/**
 * Contains parsed configuration information for an output handler,
 * including the class name and output handler name (which is really the
 * section name).
 *
 * @see CurnConfig
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public class ConfiguredOutputHandler implements Comparable
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private final String             sectionName;
    private final String             className;
    private final String             name;
    private OutputHandler            handler = null;
    private boolean                  disabled = false;
    private final Map<String,String> extraVariables =
        new HashMap<String,String>();

    /**
     * For log messages
     */
    private static final Logger log =
        new Logger (ConfiguredOutputHandler.class);

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
        log.debug ("section=" + ((sectionName == null) ? "null"
                                                       : sectionName));
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
     * Mark this handler disabled, usually in response to a plug-in
     * action.
     *
     * @see #isDisabled
     */
    public void disable()
    {
        disabled = true;
    }

    /**
     * Determine whether this handler is disabled.
     *
     * @return <tt>true</tt> if disabled, <tt>false</tt> if enabled
     *
     * @see #disable
     */
    public boolean isDisabled()
    {
        return disabled;
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
        if (this.handler == null)
        {
            this.handler = OutputHandlerFactory.getOutputHandler (className);
            this.handler.setName (name);
        }

        return this.handler;
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
    public int compareTo (final Object o)
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
    void addExtraVariable (final String name, final String value)
    {
        extraVariables.put (name, value);
    }
}
