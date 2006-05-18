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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
public class MetaPlugIn
    implements CacheLoadedPlugIn,
               FeedConfigItemPlugIn,
               MainConfigItemPlugIn,
               OutputHandlerConfigItemPlugIn,
               PostConfigPlugIn,
               PostFeedDownloadPlugIn,
               PostFeedOutputPlugIn,
               PostFeedParsePlugIn,
               PostOutputHandlerFlushPlugIn,
               PreCacheSavePlugIn,
               PreFeedDownloadPlugIn,
               PreFeedOutputPlugIn,
               ShutdownPlugIn,
               StartupPlugIn,
               UnknownSectionConfigItemPlugIn
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The loaded plug-ins, by type.
     */
    private Collection<CacheLoadedPlugIn>
        cacheLoadedPlugIns = new ArrayList<CacheLoadedPlugIn>();

    private Collection<FeedConfigItemPlugIn>
        feedConfigItemPlugIns = new ArrayList<FeedConfigItemPlugIn>();

    private Collection<MainConfigItemPlugIn>
        mainConfigItemPlugIns = new ArrayList<MainConfigItemPlugIn>();

    private Collection<OutputHandlerConfigItemPlugIn>
        outputHandlerConfigItemPlugIns =
            new ArrayList<OutputHandlerConfigItemPlugIn>();

    private Collection<PostConfigPlugIn>
        postConfigPlugIns = new ArrayList<PostConfigPlugIn>();

    private Collection<PostFeedDownloadPlugIn>
        postFeedDownloadPlugIns = new ArrayList<PostFeedDownloadPlugIn>();

    private Collection<PostFeedOutputPlugIn>
        postFeedOutputPlugIns = new ArrayList<PostFeedOutputPlugIn>();

    private Collection<PostFeedParsePlugIn>
        postFeedParsePlugIns = new ArrayList<PostFeedParsePlugIn>();

    private Collection<PostOutputHandlerFlushPlugIn>
        postOutputHandlerFlushPlugIns =
            new ArrayList<PostOutputHandlerFlushPlugIn>();

    private Collection<PreCacheSavePlugIn>
        preCacheSavePlugIns = new ArrayList<PreCacheSavePlugIn>();

    private Collection<PreFeedDownloadPlugIn>
        preFeedDownloadPlugIns = new ArrayList<PreFeedDownloadPlugIn>();

    private Collection<PreFeedOutputPlugIn>
        preFeedOutputPlugIns = new ArrayList<PreFeedOutputPlugIn>();

    private Collection<ShutdownPlugIn>
        shutdownPlugIns = new ArrayList<ShutdownPlugIn>();

    private Collection<StartupPlugIn>
        startupPlugIns = new ArrayList<StartupPlugIn>();

    private Collection<UnknownSectionConfigItemPlugIn>
        unknownSectionConfigItemPlugIns =
            new ArrayList<UnknownSectionConfigItemPlugIn>();

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
        synchronized (MetaPlugIn.class)
        {
            if (plugIn instanceof CacheLoadedPlugIn)
                cacheLoadedPlugIns.add ((CacheLoadedPlugIn) plugIn);

            else if (plugIn instanceof FeedConfigItemPlugIn)
                feedConfigItemPlugIns.add ((FeedConfigItemPlugIn) plugIn);

            else if (plugIn instanceof MainConfigItemPlugIn)
                mainConfigItemPlugIns.add ((MainConfigItemPlugIn) plugIn);

            else if (plugIn instanceof OutputHandlerConfigItemPlugIn)
                outputHandlerConfigItemPlugIns.add
                    ((OutputHandlerConfigItemPlugIn) plugIn);

            else if (plugIn instanceof PostConfigPlugIn)
                postConfigPlugIns.add ((PostConfigPlugIn) plugIn);

            else if (plugIn instanceof PostFeedDownloadPlugIn)
                postFeedDownloadPlugIns.add ((PostFeedDownloadPlugIn) plugIn);

            else if (plugIn instanceof PostFeedOutputPlugIn)
                postFeedOutputPlugIns.add ((PostFeedOutputPlugIn) plugIn);

            else if (plugIn instanceof PostFeedParsePlugIn)
                postFeedParsePlugIns.add ((PostFeedParsePlugIn) plugIn);

            else if (plugIn instanceof PostOutputHandlerFlushPlugIn)
                postOutputHandlerFlushPlugIns.add
                    ((PostOutputHandlerFlushPlugIn) plugIn);

            else if (plugIn instanceof PreCacheSavePlugIn)
                preCacheSavePlugIns.add ((PreCacheSavePlugIn) plugIn);

            else if (plugIn instanceof PreFeedDownloadPlugIn)
                preFeedDownloadPlugIns.add ((PreFeedDownloadPlugIn) plugIn);

            else if (plugIn instanceof PreFeedOutputPlugIn)
                preFeedOutputPlugIns.add ((PreFeedOutputPlugIn) plugIn);

            else if (plugIn instanceof ShutdownPlugIn)
                shutdownPlugIns.add ((ShutdownPlugIn) plugIn);

            else if (plugIn instanceof StartupPlugIn)
                startupPlugIns.add ((StartupPlugIn) plugIn);

            else if (plugIn instanceof UnknownSectionConfigItemPlugIn)
                unknownSectionConfigItemPlugIns.add
                    ((UnknownSectionConfigItemPlugIn) plugIn);

            else
                assert (false);
        }
    }

    /*----------------------------------------------------------------------*\
                Public Methods Required by PlugIn Interface
    \*----------------------------------------------------------------------*/

    public String getName()
    {
        return getClass().getName();
    }

    public synchronized void runStartupHook()
        throws CurnException
    {
        for (StartupPlugIn plugIn : startupPlugIns)
        {
            logHookInvocation ("runStartupHook", plugIn);
            plugIn.runStartupHook();
        }
    }

    public synchronized void runMainConfigItemHook (String     sectionName,
                                                    String     paramName,
                                                    CurnConfig config)
	throws CurnException
    {
        for (MainConfigItemPlugIn plugIn : mainConfigItemPlugIns)
        {
            logHookInvocation ("runMainConfigItemHook",
                               plugIn,
                               sectionName,
                               paramName);
            plugIn.runMainConfigItemHook (sectionName, paramName, config);
        }
    }

    public synchronized void runFeedConfigItemHook (String     sectionName,
                                                    String     paramName,
                                                    CurnConfig config,
                                                    FeedInfo   feedInfo)
	throws CurnException
    {
        for (FeedConfigItemPlugIn plugIn : feedConfigItemPlugIns)
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

    public synchronized void
    runOutputHandlerConfigItemHook (String                  sectionName,
                                    String                  paramName,
                                    CurnConfig              config,
                                    ConfiguredOutputHandler handler)
	throws CurnException
    {
        for (OutputHandlerConfigItemPlugIn plugIn :
                 outputHandlerConfigItemPlugIns)
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

    public synchronized void
    runUnknownSectionConfigItemHook (String     sectionName,
                                     String     paramName,
                                     CurnConfig config)
	throws CurnException
    {
        for (UnknownSectionConfigItemPlugIn plugIn :
                 unknownSectionConfigItemPlugIns)
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

    public synchronized void runPostConfigurationHook (CurnConfig config)
	throws CurnException
    {
        for (PostConfigPlugIn plugIn : postConfigPlugIns)
        {
            logHookInvocation ("runPostConfigurationHook", plugIn);
            plugIn.runPostConfigurationHook (config);
        }
    }

    public synchronized void runCacheLoadedHook (FeedCache cache)
	throws CurnException
    {
        for (CacheLoadedPlugIn plugIn : cacheLoadedPlugIns)
        {
            logHookInvocation ("runCacheLoadedHook", plugIn);
            plugIn.runCacheLoadedHook (cache);
        }
    }

    public synchronized boolean runPreFeedDownloadHook (FeedInfo feedInfo)
	throws CurnException
    {
        boolean keepGoing = true;

        for (PreFeedDownloadPlugIn plugIn : preFeedDownloadPlugIns)
        {
            logHookInvocation ("runPreFeedDownloadHook", plugIn);
            keepGoing = plugIn.runPreFeedDownloadHook (feedInfo);

            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized boolean runPostFeedDownloadHook (FeedInfo feedInfo,
                                                         File     feedDataFile,
                                                         String   encoding)
	throws CurnException
    {
        boolean keepGoing = true;

        for (PostFeedDownloadPlugIn plugIn : postFeedDownloadPlugIns)
        {
            logHookInvocation ("runPostFeedDownloadHook", plugIn);
            keepGoing = plugIn.runPostFeedDownloadHook (feedInfo,
                                                        feedDataFile,
                                                        encoding);
            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized boolean runPostFeedParseHook (FeedInfo   feedInfo,
                                                      RSSChannel channel)
	throws CurnException
    {
        boolean keepGoing = true;

        for (PostFeedParsePlugIn plugIn : postFeedParsePlugIns)
        {
            logHookInvocation ("runPostFeedParseHook", plugIn);
            keepGoing = plugIn.runPostFeedParseHook (feedInfo, channel);
            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized void runPreFeedOutputHook (FeedInfo      feedInfo,
                                                   RSSChannel    channel,
                                                   OutputHandler outputHandler)
	throws CurnException
    {
        for (PreFeedOutputPlugIn plugIn : preFeedOutputPlugIns)
        {
            logHookInvocation ("runPreFeedOutputHook", plugIn);
            plugIn.runPreFeedOutputHook (feedInfo, channel, outputHandler);
        }
    }

    public synchronized void runPostFeedOutputHook (FeedInfo      feedInfo,
                                       OutputHandler outputHandler)
	throws CurnException
    {
        for (PostFeedOutputPlugIn plugIn : postFeedOutputPlugIns)
        {
            logHookInvocation ("runPostFeedOutputHook", plugIn);
            plugIn.runPostFeedOutputHook (feedInfo, outputHandler);
        }
    }

    public synchronized boolean
    runPostOutputHandlerFlushHook (OutputHandler outputHandler)
	throws CurnException
    {
        boolean keepGoing = true;

        for (PostOutputHandlerFlushPlugIn plugIn :
                 postOutputHandlerFlushPlugIns)
        {
            logHookInvocation ("runPostOutputHandlerFlushHook", plugIn);
            keepGoing = plugIn.runPostOutputHandlerFlushHook (outputHandler);

            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized void runPreCacheSaveHook (FeedCache cache)
	throws CurnException
    {
        for (PreCacheSavePlugIn plugIn : preCacheSavePlugIns)
        {
            logHookInvocation ("runPreCacheSaveHook", plugIn);
            plugIn.runPreCacheSaveHook (cache);
        }
    }

    public synchronized void runShutdownHook()
        throws CurnException
    {
        for (ShutdownPlugIn plugIn : shutdownPlugIns)
        {
            logHookInvocation ("runShutdownHook", plugIn);
            plugIn.runShutdownHook();
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
