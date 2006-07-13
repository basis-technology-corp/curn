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
 * that wish to be notified immediately after <i>curn</i> loads its cache.
 *
 * @see PlugIn
 * @see MetaPlugIn
 * @see PreCacheSavePlugIn
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public interface CacheLoadedPlugIn extends PlugIn
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Called after the <i>curn</i> cache has been read (and after any
     * expired entries have been purged), but before any feeds are processed.
     *
     * @param cache  the loaded {@link FeedCache} object
     * 
     * @throws CurnException on error
     *
     * @see FeedCache
     */
    public void runCacheLoadedPlugIn (FeedCache cache)
        throws CurnException;
}
