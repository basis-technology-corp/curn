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

package org.clapper.curn;

import java.util.Collection;

/**
 * This interface defines the methods that must be supported by plug-ins
 * that wish to be notified after <i>curn</i> has finished invoking all
 * {@link OutputHandler}s.
 *
 * @see PlugIn
 * @see MetaPlugIn
 * @see PreFeedOutputPlugIn
 * @see PostFeedOutputPlugIn
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public interface PostOutputPlugIn extends PlugIn
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

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
	throws CurnException;
}
