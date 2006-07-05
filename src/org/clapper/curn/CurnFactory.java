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

import org.clapper.util.logging.Logger;

/**
 * <p>This static singleton class is used to allocate a new {@link Curn}
 * object for RSS processing. Hiding the allocation behind a factory allows
 * for various bootstrap activities, including the installation and use of
 * a different class loader that adds the plug-in jars and directories
 * to the load path at runtime.</p>
 *
 * @version <tt>$Revision$</tt>
 */
public class CurnFactory
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For log messages
     */
    private static Logger log = new Logger (CurnFactory.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Cannot be instantiated.
     */
    private CurnFactory()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Create a new {@link Curn} object. The resulting object will be
     * loaded via a different class loader. This method also implicitly
     * loads the plug-ins.
     *
     * @return the <tt>Curn</tt> object
     *
     * @throws CurnException on error
     */
    public static Curn newCurn()
        throws CurnException
    {
        // Load the plug-ins.

        PlugInManager.loadPlugIns();

        return new Curn();
    }
}
