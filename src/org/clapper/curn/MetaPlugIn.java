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
 * @see CacheLoadedPlugIn
 * @see FeedConfigItemPlugIn
 * @see MainConfigItemPlugIn
 * @see OutputHandlerConfigItemPlugIn
 * @see PostConfigPlugIn
 * @see PostFeedDownloadPlugIn
 * @see PostFeedOutputPlugIn
 * @see PostFeedParsePlugIn
 * @see PostOutputHandlerFlushPlugIn
 * @see PreCacheSavePlugIn
 * @see PreFeedDownloadPlugIn
 * @see PreFeedOutputPlugIn
 * @see ShutdownPlugIn
 * @see StartupPlugIn
 * @see UnknownSectionConfigItemPlugIn
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
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

            if (plugIn instanceof FeedConfigItemPlugIn)
                feedConfigItemPlugIns.add ((FeedConfigItemPlugIn) plugIn);

            if (plugIn instanceof MainConfigItemPlugIn)
                mainConfigItemPlugIns.add ((MainConfigItemPlugIn) plugIn);

            if (plugIn instanceof OutputHandlerConfigItemPlugIn)
                outputHandlerConfigItemPlugIns.add
                    ((OutputHandlerConfigItemPlugIn) plugIn);

            if (plugIn instanceof PostConfigPlugIn)
                postConfigPlugIns.add ((PostConfigPlugIn) plugIn);

            if (plugIn instanceof PostFeedDownloadPlugIn)
                postFeedDownloadPlugIns.add ((PostFeedDownloadPlugIn) plugIn);

            if (plugIn instanceof PostFeedOutputPlugIn)
                postFeedOutputPlugIns.add ((PostFeedOutputPlugIn) plugIn);

            if (plugIn instanceof PostFeedParsePlugIn)
                postFeedParsePlugIns.add ((PostFeedParsePlugIn) plugIn);

            if (plugIn instanceof PostOutputHandlerFlushPlugIn)
                postOutputHandlerFlushPlugIns.add
                    ((PostOutputHandlerFlushPlugIn) plugIn);

            if (plugIn instanceof PreCacheSavePlugIn)
                preCacheSavePlugIns.add ((PreCacheSavePlugIn) plugIn);

            if (plugIn instanceof PreFeedDownloadPlugIn)
                preFeedDownloadPlugIns.add ((PreFeedDownloadPlugIn) plugIn);

            if (plugIn instanceof PreFeedOutputPlugIn)
                preFeedOutputPlugIns.add ((PreFeedOutputPlugIn) plugIn);

            if (plugIn instanceof ShutdownPlugIn)
                shutdownPlugIns.add ((ShutdownPlugIn) plugIn);

            if (plugIn instanceof StartupPlugIn)
                startupPlugIns.add ((StartupPlugIn) plugIn);

            if (plugIn instanceof UnknownSectionConfigItemPlugIn)
                unknownSectionConfigItemPlugIns.add
                    ((UnknownSectionConfigItemPlugIn) plugIn);
        }
    }

    /*----------------------------------------------------------------------*\
                Public Methods Required by PlugIn Interface
    \*----------------------------------------------------------------------*/

    public String getName()
    {
        return getClass().getName();
    }

    public synchronized void runStartupPlugIn()
        throws CurnException
    {
        for (StartupPlugIn plugIn : startupPlugIns)
        {
            logPlugInInvocation ("runStartupPlugIn", plugIn);
            plugIn.runStartupPlugIn();
        }
    }

    public synchronized void runMainConfigItemPlugIn (String     sectionName,
                                                    String     paramName,
                                                    CurnConfig config)
	throws CurnException
    {
        for (MainConfigItemPlugIn plugIn : mainConfigItemPlugIns)
        {
            logPlugInInvocation ("runMainConfigItemPlugIn",
                                 plugIn,
                                 sectionName,
                                 paramName);
            plugIn.runMainConfigItemPlugIn (sectionName, paramName, config);
        }
    }

    public synchronized void runFeedConfigItemPlugIn (String     sectionName,
                                                    String     paramName,
                                                    CurnConfig config,
                                                    FeedInfo   feedInfo)
	throws CurnException
    {
        for (FeedConfigItemPlugIn plugIn : feedConfigItemPlugIns)
        {
            logPlugInInvocation ("runFeedConfigItemPlugIn",
                                 plugIn,
                                 sectionName,
                                 paramName);
            plugIn.runFeedConfigItemPlugIn (sectionName,
                                            paramName,
                                            config,
                                            feedInfo);
        }
    }

    public synchronized void
    runOutputHandlerConfigItemPlugIn (String                  sectionName,
                                    String                  paramName,
                                    CurnConfig              config,
                                    ConfiguredOutputHandler handler)
	throws CurnException
    {
        for (OutputHandlerConfigItemPlugIn plugIn :
                 outputHandlerConfigItemPlugIns)
        {
            logPlugInInvocation ("runOutputHandlerConfigItemPlugIn",
                                 plugIn,
                                 sectionName,
                                 paramName);
            plugIn.runOutputHandlerConfigItemPlugIn (sectionName,
                                                     paramName,
                                                     config,
                                                     handler);
        }
    }

    public synchronized void
    runUnknownSectionConfigItemPlugIn (String     sectionName,
                                       String     paramName,
                                       CurnConfig config)
	throws CurnException
    {
        for (UnknownSectionConfigItemPlugIn plugIn :
                 unknownSectionConfigItemPlugIns)
        {
            logPlugInInvocation ("runUnknownSectionConfigItemPlugIn",
                                 plugIn,
                                 sectionName,
                                 paramName);
            plugIn.runUnknownSectionConfigItemPlugIn (sectionName,
                                                      paramName,
                                                      config);
        }
    }

    public synchronized void runPostConfigurationPlugIn (CurnConfig config)
	throws CurnException
    {
        for (PostConfigPlugIn plugIn : postConfigPlugIns)
        {
            logPlugInInvocation ("runPostConfigurationPlugIn", plugIn);
            plugIn.runPostConfigurationPlugIn (config);
        }
    }

    public synchronized void runCacheLoadedPlugIn (FeedCache cache)
	throws CurnException
    {
        for (CacheLoadedPlugIn plugIn : cacheLoadedPlugIns)
        {
            logPlugInInvocation ("runCacheLoadedPlugIn", plugIn);
            plugIn.runCacheLoadedPlugIn (cache);
        }
    }

    public synchronized boolean runPreFeedDownloadPlugIn (FeedInfo feedInfo)
	throws CurnException
    {
        boolean keepGoing = true;

        for (PreFeedDownloadPlugIn plugIn : preFeedDownloadPlugIns)
        {
            logPlugInInvocation ("runPreFeedDownloadPlugIn", plugIn);
            keepGoing = plugIn.runPreFeedDownloadPlugIn (feedInfo);

            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized boolean runPostFeedDownloadPlugIn (FeedInfo feedInfo,
                                                           File     feedDataFile,
                                                           String   encoding)
	throws CurnException
    {
        boolean keepGoing = true;

        for (PostFeedDownloadPlugIn plugIn : postFeedDownloadPlugIns)
        {
            logPlugInInvocation ("runPostFeedDownloadPlugIn", plugIn);
            keepGoing = plugIn.runPostFeedDownloadPlugIn (feedInfo,
                                                          feedDataFile,
                                                          encoding);
            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized boolean runPostFeedParsePlugIn (FeedInfo   feedInfo,
                                                        RSSChannel channel)
	throws CurnException
    {
        boolean keepGoing = true;

        for (PostFeedParsePlugIn plugIn : postFeedParsePlugIns)
        {
            logPlugInInvocation ("runPostFeedParsePlugIn", plugIn);
            keepGoing = plugIn.runPostFeedParsePlugIn (feedInfo, channel);
            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized void runPreFeedOutputPlugIn (FeedInfo      feedInfo,
                                                     RSSChannel    channel,
                                                     OutputHandler outputHandler)
	throws CurnException
    {
        for (PreFeedOutputPlugIn plugIn : preFeedOutputPlugIns)
        {
            logPlugInInvocation ("runPreFeedOutputPlugIn", plugIn);
            plugIn.runPreFeedOutputPlugIn (feedInfo, channel, outputHandler);
        }
    }

    public synchronized void runPostFeedOutputPlugIn (FeedInfo      feedInfo,
                                       OutputHandler outputHandler)
	throws CurnException
    {
        for (PostFeedOutputPlugIn plugIn : postFeedOutputPlugIns)
        {
            logPlugInInvocation ("runPostFeedOutputPlugIn", plugIn);
            plugIn.runPostFeedOutputPlugIn (feedInfo, outputHandler);
        }
    }

    public synchronized boolean
    runPostOutputHandlerFlushPlugIn (OutputHandler outputHandler)
	throws CurnException
    {
        boolean keepGoing = true;

        for (PostOutputHandlerFlushPlugIn plugIn :
                 postOutputHandlerFlushPlugIns)
        {
            logPlugInInvocation ("runPostOutputHandlerFlushPlugIn", plugIn);
            keepGoing = plugIn.runPostOutputHandlerFlushPlugIn (outputHandler);

            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized void runPreCacheSavePlugIn (FeedCache cache)
	throws CurnException
    {
        for (PreCacheSavePlugIn plugIn : preCacheSavePlugIns)
        {
            logPlugInInvocation ("runPreCacheSavePlugIn", plugIn);
            plugIn.runPreCacheSavePlugIn (cache);
        }
    }

    public synchronized void runShutdownPlugIn()
        throws CurnException
    {
        for (ShutdownPlugIn plugIn : shutdownPlugIns)
        {
            logPlugInInvocation ("runShutdownPlugIn", plugIn);
            plugIn.runShutdownPlugIn();
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
     * Log a plug-in invocation.
     *
     * @param methodName  calling method name
     * @param plugIn      plug-in class
     * @param args        method args, if any
     */
    private void logPlugInInvocation (String    methodName,
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
