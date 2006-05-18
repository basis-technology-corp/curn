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

/**
 * This interface defines the methods that must be supported by plug-ins
 * that wish intercept <i>curn</i> feed-section configuration items.
 *
 * @see PlugIn
 * @see MetaPlugIn
 * @see MainConfigItemPlugIn
 * @see OutputHandlerConfigItemPlugIn
 * @see UnknownSectionConfigItemPlugIn
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public interface FeedConfigItemPlugIn extends PlugIn
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Called by the plug-in manager right after <i>curn</i> has read and
     * processed a configuration item in a "feed" configuration section.
     * All configuration items are passed, one by one, to each loaded
     * plug-in. If a plug-in class is not interested in a particular
     * configuration item, this method should simply return without doing
     * anything. Note that some configuration items may simply be variable
     * assignment; there's no real way to distinguish a variable assignment
     * from a blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param config       the {@link CurnConfig} object
     * @param feedInfo     partially complete <tt>FeedInfo</tt> object
     *                     for the feed. The URL is guaranteed to be
     *                     present, but no other fields are.
     * 
     * @throws CurnException on error
     *
     * @see CurnConfig
     * @see FeedInfo
     * @see FeedInfo#getURL
     */
    public void runFeedConfigItemPlugIn (String     sectionName,
                                         String     paramName,
                                         CurnConfig config,
                                         FeedInfo   feedInfo)
	throws CurnException;
}
