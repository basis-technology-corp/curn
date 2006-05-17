/*---------------------------------------------------------------------------*\
  $Id: OutputHandler.java 5749 2006-03-24 16:21:51Z bmc $
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

import org.clapper.curn.Curn;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedCache;
import org.clapper.curn.parser.RSSChannel;

import org.clapper.util.logging.Logger;

import java.io.File;

import java.util.Collection;
import java.util.ArrayList;

/**
 * A <tt>MetaPlugIn</tt> object is basically a plug-in that contains all the
 * loaded plug-ins. It's a singleton that makes it easier for <i>curn</i>
 * to invoke the various loaded plugins. It is not used outside of
 * <i>curn</i>. The <tt>MetaPlugIn</tt> singleton object is loaded by an
 * instance of the {@link PlugInManager} class.
 *
 * @see PlugIn
 * @see PlugInManager
 * @see AbstractPlugIn
 * @see Curn
 *
 * @version <tt>$Revision: 5749 $</tt>
 */
public class MetaPlugIn implements PlugIn
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The loaded plug-ins.
     */
    private Collection<PlugIn> plugIns = null;

    /**
     * Lock object, for synchronization
     */
    private static Object lock = new Object();

    /**
     * The singleton
     */
    private static MetaPlugIn metaPlugIn = null;

    /**
     * For log messages
     */
    private static Logger log = new Logger (MetaPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Cannot be instantiated normally.
     */
    private MetaPlugIn()
    {
        plugIns = new ArrayList<PlugIn>();
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the <tt>MetaPlugIn</tt> singleton.
     *
     * @param classLoader class loader to use
     *
     * @return the <tt>MetaPlugIn</tt> singleton
     *
     * @throws CurnException on error
     */
    public static MetaPlugIn getMetaPlugIn()
    {
        assert (metaPlugIn != null);
        return metaPlugIn;
    }

    /**
     * Add a plug-in to the list of plug-ins wrapped in this object.
     * This method is only intended for use by the {@link PlugInManager}.
     *
     * @param plugIn  the {@link PlugIn} to add
     */
    public void addPlugIn (PlugIn plugIn)
    {
        synchronized (lock)
        {
            plugIns.add (plugIn);
        }
    }

    /*----------------------------------------------------------------------*\
                Public Methods Required by PlugIn Interface
    \*----------------------------------------------------------------------*/

    public String getName()
    {
        return getClass().getName();
    }

    public void runStartupHook()
        throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runStartupHook", plugIn);
                plugIn.runStartupHook();
            }
        }
    }

    public void runMainConfigItemHook (String     sectionName,
                                       String     paramName,
                                       CurnConfig config)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runMainConfigItemHook",
                                   plugIn,
                                   sectionName,
                                   paramName);
                plugIn.runMainConfigItemHook (sectionName,
                                              paramName,
                                              config);
            }
        }
    }

    public void runFeedConfigItemHook (String     sectionName,
                                       String     paramName,
                                       CurnConfig config,
                                       FeedInfo   feedInfo)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runFeedConfigItemHook",
                                   plugIn,
                                   sectionName,
                                   paramName);
                plugIn.runFeedConfigItemHook (sectionName,
                                              paramName,
                                              config,
                                              feedInfo);
            }
        }
    }

    public void
    runOutputHandlerConfigItemHook (String                  sectionName,
                                    String                  paramName,
                                    CurnConfig              config,
                                    ConfiguredOutputHandler handler)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runOutputHandlerConfigItemHook",
                                   plugIn,
                                   sectionName,
                                   paramName);
                plugIn.runOutputHandlerConfigItemHook (sectionName,
                                                       paramName,
                                                       config,
                                                       handler);
            }
        }
    }

    public void
    runUnknownSectionConfigItemHook (String     sectionName,
                                     String     paramName,
                                     CurnConfig config)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runUnknownSectionConfigItemHook",
                                   plugIn,
                                   sectionName,
                                   paramName);
                plugIn.runUnknownSectionConfigItemHook (sectionName,
                                                        paramName,
                                                        config);
            }
        }
    }

    public void runPostConfigurationHook (CurnConfig config)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPostConfigurationHook", plugIn);
                plugIn.runPostConfigurationHook (config);
            }
        }
    }

    public void runCacheLoadedHook (FeedCache cache)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runCacheLoadedHook", plugIn);
                plugIn.runCacheLoadedHook (cache);
            }
        }
    }

    public boolean runPreFeedDownloadHook (FeedInfo feedInfo)
	throws CurnException
    {
        boolean keepGoing = true;

        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPreFeedDownloadHook", plugIn);
                keepGoing = plugIn.runPreFeedDownloadHook (feedInfo);
                if (! keepGoing)
                    break;
            }
        }

        return keepGoing;
    }

    public boolean runPostFeedDownloadHook (FeedInfo feedInfo,
					    File     feedDataFile,
                                            String   encoding)
	throws CurnException
    {
        boolean keepGoing = true;

        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPostFeedDownloadHook", plugIn);
                keepGoing = plugIn.runPostFeedDownloadHook (feedInfo,
                                                            feedDataFile,
                                                            encoding);
                if (! keepGoing)
                    break;
            }
        }

        return keepGoing;
    }

    public boolean runPostFeedParseHook (FeedInfo feedInfo, RSSChannel channel)
	throws CurnException
    {
        boolean keepGoing = true;

        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPostFeedParseHook", plugIn);
                keepGoing = plugIn.runPostFeedParseHook (feedInfo, channel);
                if (! keepGoing)
                    break;
            }
        }

        return keepGoing;
    }

    public void runPreFeedOutputHook (FeedInfo      feedInfo,
                                      RSSChannel    channel,
                                      OutputHandler outputHandler)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPreFeedOutputHook", plugIn);
                plugIn.runPreFeedOutputHook (feedInfo, channel, outputHandler);
            }
        }
    }

    public void runPostFeedOutputHook (FeedInfo      feedInfo,
                                       OutputHandler outputHandler)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPreFeedOutputHook", plugIn);
                plugIn.runPostFeedOutputHook (feedInfo, outputHandler);
            }
        }
    }

    public boolean runPostOutputHandlerFlushHook (OutputHandler outputHandler)
	throws CurnException
    {
        boolean keepGoing = true;

        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPostOutputHandlerFlushHook", plugIn);
                if (! plugIn.runPostOutputHandlerFlushHook (outputHandler))
                    keepGoing = false;
            }
        }

        return keepGoing;
    }

    public void runPreCacheSaveHook (FeedCache cache)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPreCacheSaveHook", plugIn);
                plugIn.runPreCacheSaveHook (cache);
            }
        }
    }

    public void runShutdownHook()
        throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runShutdownHook", plugIn);
                plugIn.runShutdownHook();
            }
        }
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Create the MetaPlugIn
     *
     * @param classLoader class loader to use
     *
     * @return the created MetaPlugIn
     *
     * @throws CurnException on error
     */
    static MetaPlugIn createMetaPlugIn (ClassLoader classLoader)
        throws CurnException
    {
        assert (metaPlugIn == null);
        try
        {
            Class cls = classLoader.loadClass ("org.clapper.curn.MetaPlugIn");
            metaPlugIn = (MetaPlugIn) cls.newInstance();
            return metaPlugIn;
        }

        catch (Exception ex)
        {
            throw new CurnException ("Can't allocate MetaPlugIn", ex);
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Log a hook invocation.
     *
     * @param methodName  calling method name
     * @param plugIn      plug-in class
     * @param args        method args, if any
     */
    private void logHookInvocation (String    methodName,
                                    PlugIn    plugIn,
                                    Object... args)
    {
        if (log.isDebugEnabled())
        {
            StringBuilder buf = new StringBuilder();

            buf.append ("invoking ");
            buf.append (methodName);
            String sep = "(";

            for (Object arg : args)
            {
                buf.append (sep);
                sep = ", ";

                if (arg instanceof Number)
                    buf.append (arg.toString());

                else
                {
                    buf.append ('"');
                    buf.append (arg.toString());
                    buf.append ('"');
                }
            }

            buf.append (") for plug-in ");
            buf.append (plugIn.getClass().getName());
            log.debug (buf.toString());
        }
    }
}
