/*---------------------------------------------------------------------------*\
  $Id: PlugIn.java 5916 2006-05-17 12:54:05Z bmc $
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

/**
 * This interface defines the methods that must be supported by plug-ins
 * that wish to be notified after <i>curn</i> sends a parsed feed to an
 * {@link OutputHandler}.
 *
 * @see PlugIn
 * @see MetaPlugIn
 * @see PreFeedOutputPlugIn
 * @see PostFeedOutputPlugIn
 * @see Curn
 *
 * @version <tt>$Revision: 5916 $</tt>
 */
public interface PostOutputHandlerFlushPlugIn extends PlugIn
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Called immediately after an output handler is flushed (i.e., after
     * its output has been written to a temporary file), but before that
     * output is displayed, emailed, etc.
     *
     * @param outputHandler the {@link OutputHandler} that is about to be
     *                      called. This object is read-only.
     *
     * @return <tt>true</tt> if <i>curn</i> should process the output,
     *         <tt>false</tt> to skip the output from the handler.
     *
     * @throws CurnException on error
     *
     * @see OutputHandler
     */
    public boolean
    runPostOutputHandlerFlushPlugIn (OutputHandler outputHandler)
	throws CurnException;
}
