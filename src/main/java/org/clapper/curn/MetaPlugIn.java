/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2012 Brian M. Clapper.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name "clapper.org", "curn", nor the names of the project's
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/


package org.clapper.curn;

import org.clapper.curn.parser.RSSChannel;

import org.clapper.util.logging.Logger;

import java.io.File;

import java.net.URLConnection;

import java.util.Collection;
import java.util.TreeSet;

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
               ForceFeedDownloadPlugIn,
               MainConfigItemPlugIn,
               OutputHandlerConfigItemPlugIn,
               PostConfigPlugIn,
               PostFeedDownloadPlugIn,
               PostFeedOutputPlugIn,
               PostFeedParsePlugIn,
               PostFeedProcessPlugIn,
               PostOutputHandlerFlushPlugIn,
               PreCacheSavePlugIn,
               PreFeedDownloadPlugIn,
               PreFeedOutputPlugIn,
               PostOutputPlugIn,
               ShutdownPlugIn,
               StartupPlugIn,
               UnknownSectionConfigItemPlugIn
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Plug-in comparator.
     */
    private PlugInComparator cmp = new PlugInComparator();

    /**
     * The loaded plug-ins, by type.
     */
    private final Collection<CacheLoadedPlugIn>
        cacheLoadedPlugIns = new TreeSet<CacheLoadedPlugIn>(cmp);

    private final Collection<FeedConfigItemPlugIn>
        feedConfigItemPlugIns = new TreeSet<FeedConfigItemPlugIn>(cmp);

    private final Collection<MainConfigItemPlugIn>
        mainConfigItemPlugIns = new TreeSet<MainConfigItemPlugIn>(cmp);

    private final Collection<OutputHandlerConfigItemPlugIn>
        outputHandlerConfigItemPlugIns =
            new TreeSet<OutputHandlerConfigItemPlugIn>(cmp);

    private final Collection<PostConfigPlugIn>
        postConfigPlugIns = new TreeSet<PostConfigPlugIn>(cmp);

    private final Collection<PostFeedDownloadPlugIn>
        postFeedDownloadPlugIns = new TreeSet<PostFeedDownloadPlugIn>(cmp);

    private final Collection<PostFeedOutputPlugIn>
        postFeedOutputPlugIns = new TreeSet<PostFeedOutputPlugIn>(cmp);

    private final Collection<PostFeedProcessPlugIn>
        postFeedProcessPlugIns = new TreeSet<PostFeedProcessPlugIn>(cmp);

    private final Collection<PostFeedParsePlugIn>
        postFeedParsePlugIns = new TreeSet<PostFeedParsePlugIn>(cmp);

    private final Collection<PostOutputHandlerFlushPlugIn>
        postOutputHandlerFlushPlugIns =
            new TreeSet<PostOutputHandlerFlushPlugIn>(cmp);

    private final Collection<PreCacheSavePlugIn>
        preCacheSavePlugIns = new TreeSet<PreCacheSavePlugIn>(cmp);

    private final Collection<ForceFeedDownloadPlugIn>
        forceFeedDownloadPlugIns = new TreeSet<ForceFeedDownloadPlugIn>(cmp);

    private final Collection<PreFeedDownloadPlugIn>
        preFeedDownloadPlugIns = new TreeSet<PreFeedDownloadPlugIn>(cmp);

    private final Collection<PreFeedOutputPlugIn>
        preFeedOutputPlugIns = new TreeSet<PreFeedOutputPlugIn>(cmp);

    private final Collection<PostOutputPlugIn>
        postOutputPlugIns = new TreeSet<PostOutputPlugIn>(cmp);

    private final Collection<ShutdownPlugIn>
        shutdownPlugIns = new TreeSet<ShutdownPlugIn>(cmp);

    private final Collection<StartupPlugIn>
        startupPlugIns = new TreeSet<StartupPlugIn>(cmp);

    private final Collection<UnknownSectionConfigItemPlugIn>
        unknownSectionConfigItemPlugIns =
            new TreeSet<UnknownSectionConfigItemPlugIn>(cmp);

    private final Collection<PlugIn> allPlugIns = new TreeSet<PlugIn>(cmp);

    /**
     * The singleton
     */
    private static MetaPlugIn metaPlugInWrapper = null;

    /**
     * For log messages
     */
    private static final Logger log = new Logger (MetaPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Cannot be instantiated normally.
     */
    private MetaPlugIn()
    {
        // Cannot be instantiated directly.
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the <tt>MetaPlugIn</tt> singleton.
     *
     * @return the <tt>MetaPlugIn</tt> singleton
     */
    public static MetaPlugIn getMetaPlugIn()
    {
        assert (metaPlugInWrapper != null);
        return metaPlugInWrapper;
    }

    /**
     * Add a plug-in to the list of plug-ins wrapped in this object.
     * This method is only intended for use by the {@link PlugInManager}.
     *
     * @param plugIn  the {@link PlugIn} to add
     */
    public void addPlugIn (final PlugIn plugIn)
    {
        synchronized (MetaPlugIn.class)
        {
            if (plugIn instanceof CacheLoadedPlugIn)
                cacheLoadedPlugIns.add((CacheLoadedPlugIn) plugIn);

            if (plugIn instanceof FeedConfigItemPlugIn)
                feedConfigItemPlugIns.add((FeedConfigItemPlugIn) plugIn);

            if (plugIn instanceof MainConfigItemPlugIn)
                mainConfigItemPlugIns.add((MainConfigItemPlugIn) plugIn);

            if (plugIn instanceof OutputHandlerConfigItemPlugIn)
                outputHandlerConfigItemPlugIns.add
                    ((OutputHandlerConfigItemPlugIn) plugIn);

            if (plugIn instanceof PostConfigPlugIn)
                postConfigPlugIns.add((PostConfigPlugIn) plugIn);

            if (plugIn instanceof PostFeedDownloadPlugIn)
                postFeedDownloadPlugIns.add((PostFeedDownloadPlugIn) plugIn);

            if (plugIn instanceof PostFeedOutputPlugIn)
                postFeedOutputPlugIns.add((PostFeedOutputPlugIn) plugIn);

            if (plugIn instanceof PostFeedParsePlugIn)
                postFeedParsePlugIns.add((PostFeedParsePlugIn) plugIn);

            if (plugIn instanceof PostFeedProcessPlugIn)
                postFeedProcessPlugIns.add((PostFeedProcessPlugIn) plugIn);

            if (plugIn instanceof PostOutputHandlerFlushPlugIn)
                postOutputHandlerFlushPlugIns.add
                    ((PostOutputHandlerFlushPlugIn) plugIn);

            if (plugIn instanceof PreCacheSavePlugIn)
                preCacheSavePlugIns.add((PreCacheSavePlugIn) plugIn);

            if (plugIn instanceof ForceFeedDownloadPlugIn)
                forceFeedDownloadPlugIns.add((ForceFeedDownloadPlugIn) plugIn);

            if (plugIn instanceof PreFeedDownloadPlugIn)
                preFeedDownloadPlugIns.add((PreFeedDownloadPlugIn) plugIn);

            if (plugIn instanceof PreFeedOutputPlugIn)
                preFeedOutputPlugIns.add((PreFeedOutputPlugIn) plugIn);

            if (plugIn instanceof PostOutputPlugIn)
                postOutputPlugIns.add((PostOutputPlugIn) plugIn);

            if (plugIn instanceof ShutdownPlugIn)
                shutdownPlugIns.add((ShutdownPlugIn) plugIn);

            if (plugIn instanceof StartupPlugIn)
                startupPlugIns.add((StartupPlugIn) plugIn);

            if (plugIn instanceof UnknownSectionConfigItemPlugIn)
                unknownSectionConfigItemPlugIns.add
                    ((UnknownSectionConfigItemPlugIn) plugIn);

            allPlugIns.add(plugIn);
        }
    }

    /**
     * Find all plug-ins that implement the {@link PersistentDataClient}
     * interface and register them with the specified {@link DataPersister}.
     *
     * @param dataPersister the <tt>DataPersister</tt>
     */
    public void registerPersistentDataClientPlugIns(DataPersister dataPersister)
    {
        for (PlugIn plugIn : allPlugIns)
        {
            if (plugIn instanceof PersistentDataClient)
            {
                log.debug(plugIn.getPlugInName() + " plug-in is a " +
                          "persistent data client. Registering it.");
                dataPersister.addPersistentDataClient
                    ((PersistentDataClient) plugIn);
            }
        }
    }

    /*----------------------------------------------------------------------*\
                Public Methods Required by PlugIn Interface
    \*----------------------------------------------------------------------*/

    public String getPlugInName()
    {
        return getClass().getName();
    }

    public String getPlugInSortKey()
    {
        return null; // "invisible" plug-in
    }

    public void initPlugIn()
        throws CurnException
    {
        for (PlugIn plugIn : allPlugIns)
            plugIn.initPlugIn();
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

    public synchronized void
    runMainConfigItemPlugIn(final String     sectionName,
                            final String     paramName,
                            final CurnConfig config)
        throws CurnException
    {
        for (MainConfigItemPlugIn plugIn : mainConfigItemPlugIns)
        {
            logPlugInInvocation("runMainConfigItemPlugIn",
                                plugIn,
                                sectionName,
                                paramName);
            plugIn.runMainConfigItemPlugIn(sectionName, paramName, config);
        }
    }

    public synchronized boolean
    runFeedConfigItemPlugIn(final String     sectionName,
                            final String     paramName,
                            final CurnConfig config,
                            final FeedInfo   feedInfo)
        throws CurnException
    {
        boolean keepGoing = true;

        for (FeedConfigItemPlugIn plugIn : feedConfigItemPlugIns)
        {
            logPlugInInvocation("runFeedConfigItemPlugIn",
                                plugIn,
                                sectionName,
                                paramName);
            keepGoing = plugIn.runFeedConfigItemPlugIn(sectionName,
                                                       paramName,
                                                       config,
                                                       feedInfo);
            if (! keepGoing)
            {
                log.info("Plug-in " + plugIn.getPlugInName() +
                         " has aborted processing of feed " + sectionName);
                break;
            }
        }

        return keepGoing;
    }

    public synchronized boolean
    runOutputHandlerConfigItemPlugIn(final String                  sectionName,
                                     final String                  paramName,
                                     final CurnConfig              config,
                                     final ConfiguredOutputHandler handler)
        throws CurnException
    {
        boolean keepGoing = true;

        for (OutputHandlerConfigItemPlugIn plugIn :
               outputHandlerConfigItemPlugIns)
        {
            logPlugInInvocation("runOutputHandlerConfigItemPlugIn",
                                plugIn,
                                sectionName,
                                paramName);
            keepGoing = plugIn.runOutputHandlerConfigItemPlugIn(sectionName,
                                                                paramName,
                                                                config,
                                                                handler);
            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized void
    runUnknownSectionConfigItemPlugIn(final String     sectionName,
                                      final String     paramName,
                                      final CurnConfig config)
        throws CurnException
    {
        for (UnknownSectionConfigItemPlugIn plugIn :
                 unknownSectionConfigItemPlugIns)
        {
            logPlugInInvocation("runUnknownSectionConfigItemPlugIn",
                                plugIn,
                                sectionName,
                                paramName);
            plugIn.runUnknownSectionConfigItemPlugIn(sectionName,
                                                     paramName,
                                                     config);
        }
    }

    public synchronized void runPostConfigPlugIn(final CurnConfig config)
        throws CurnException
    {
        for (PostConfigPlugIn plugIn : postConfigPlugIns)
        {
            logPlugInInvocation("runPostConfigPlugIn", plugIn);
            plugIn.runPostConfigPlugIn(config);
        }
    }

    public synchronized void runCacheLoadedPlugIn (final FeedCache cache)
        throws CurnException
    {
        for (CacheLoadedPlugIn plugIn : cacheLoadedPlugIns)
        {
            logPlugInInvocation ("runCacheLoadedPlugIn", plugIn);
            plugIn.runCacheLoadedPlugIn (cache);
        }
    }

    public synchronized boolean
    forceFeedDownload(final FeedInfo feedInfo, final FeedCache feedCache)
        throws CurnException
    {
        boolean forceDownload = false;

        for (ForceFeedDownloadPlugIn plugIn : forceFeedDownloadPlugIns)
        {
            logPlugInInvocation("forceFeedDownload", plugIn);
            forceDownload = plugIn.forceFeedDownload(feedInfo, feedCache);

            if (forceDownload)
                break;
        }

        return forceDownload;
    }

    public synchronized boolean
    runPreFeedDownloadPlugIn(final FeedInfo      feedInfo,
                             final URLConnection urlConn)
        throws CurnException
    {
        boolean keepGoing = true;

        for (PreFeedDownloadPlugIn plugIn : preFeedDownloadPlugIns)
        {
            logPlugInInvocation("runPreFeedDownloadPlugIn", plugIn);
            keepGoing = plugIn.runPreFeedDownloadPlugIn(feedInfo, urlConn);

            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized boolean
    runPostFeedDownloadPlugIn(final FeedInfo feedInfo,
                              final File     feedDataFile,
                              final String   encoding)
        throws CurnException
    {
        boolean keepGoing = true;

        for (PostFeedDownloadPlugIn plugIn : postFeedDownloadPlugIns)
        {
            logPlugInInvocation("runPostFeedDownloadPlugIn", plugIn);
            keepGoing = plugIn.runPostFeedDownloadPlugIn(feedInfo,
                                                         feedDataFile,
                                                         encoding);
            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized boolean
    runPostFeedParsePlugIn(final FeedInfo   feedInfo,
                           final FeedCache  feedCache,
                           final RSSChannel channel)
        throws CurnException
    {
        boolean keepGoing = true;

        for (PostFeedParsePlugIn plugIn : postFeedParsePlugIns)
        {
            logPlugInInvocation("runPostFeedParsePlugIn", plugIn);
            keepGoing = plugIn.runPostFeedParsePlugIn(feedInfo, feedCache,
                                                      channel);
            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized boolean
    runPostFeedProcessPlugIn(final FeedInfo   feedInfo,
                             final FeedCache  feedCache,
                             final RSSChannel channel)
        throws CurnException
    {
        boolean keepGoing = true;

        for (PostFeedProcessPlugIn plugIn : postFeedProcessPlugIns)
        {
            logPlugInInvocation("runPostFeedParsePlugIn", plugIn);
            keepGoing = plugIn.runPostFeedProcessPlugIn(feedInfo, feedCache,
                                                        channel);
            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public synchronized void
    runPreFeedOutputPlugIn(final FeedInfo      feedInfo,
                           final RSSChannel    channel,
                           final OutputHandler outputHandler)
        throws CurnException
    {
        for (PreFeedOutputPlugIn plugIn : preFeedOutputPlugIns)
        {
            logPlugInInvocation("runPreFeedOutputPlugIn", plugIn);
            plugIn.runPreFeedOutputPlugIn(feedInfo, channel, outputHandler);
        }
    }

    public synchronized void
    runPostFeedOutputPlugIn(final FeedInfo      feedInfo,
                            final OutputHandler outputHandler)
        throws CurnException
    {
        for (PostFeedOutputPlugIn plugIn : postFeedOutputPlugIns)
        {
            logPlugInInvocation("runPostFeedOutputPlugIn", plugIn);
            plugIn.runPostFeedOutputPlugIn(feedInfo, outputHandler);
        }
    }

    public synchronized boolean
    runPostOutputHandlerFlushPlugIn(final OutputHandler outputHandler)
        throws CurnException
    {
        boolean keepGoing = true;

        for (PostOutputHandlerFlushPlugIn plugIn :
                 postOutputHandlerFlushPlugIns)
        {
            logPlugInInvocation("runPostOutputHandlerFlushPlugIn", plugIn);
            keepGoing = plugIn.runPostOutputHandlerFlushPlugIn(outputHandler);

            if (! keepGoing)
                break;
        }

        return keepGoing;
    }

    public void
    runPostOutputPlugIn(final Collection<OutputHandler> outputHandlers)
        throws CurnException
    {
        for (PostOutputPlugIn plugIn : postOutputPlugIns)
        {
            logPlugInInvocation("runPostOutputPlugIn", plugIn);
            plugIn.runPostOutputPlugIn(outputHandlers);
        }
    }

    public synchronized void runPreCacheSavePlugIn(final FeedCache cache)
        throws CurnException
    {
        for (PreCacheSavePlugIn plugIn : preCacheSavePlugIns)
        {
            logPlugInInvocation("runPreCacheSavePlugIn", plugIn);
            plugIn.runPreCacheSavePlugIn(cache);
        }
    }

    public synchronized void runShutdownPlugIn()
        throws CurnException
    {
        for (ShutdownPlugIn plugIn : shutdownPlugIns)
        {
            logPlugInInvocation("runShutdownPlugIn", plugIn);
            plugIn.runShutdownPlugIn();
        }
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Create the MetaPlugIn
     *
     * @return the created MetaPlugIn
     *
     * @throws CurnException on error
     */
    static MetaPlugIn createMetaPlugIn()
        throws CurnException
    {
        assert (metaPlugInWrapper == null);
        try
        {
            metaPlugInWrapper = new MetaPlugIn();
            return metaPlugInWrapper;
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
    private void logPlugInInvocation(final String    methodName,
                                     final PlugIn    plugIn,
                                     final Object... args)   // NOPMD
    {
        if (log.isDebugEnabled())
        {
            StringBuilder buf = new StringBuilder();

            buf.append("invoking ");
            buf.append(methodName);
            String sep = "(";

            for (Object arg : args)
            {
                buf.append(sep);
                sep = ", ";

                if (arg instanceof Number)
                    buf.append(arg.toString());

                else
                {
                    buf.append('"');
                    buf.append(arg.toString());
                    buf.append('"');
                }
            }

            buf.append(") for plug-in ");
            buf.append(plugIn.getClass().getName());
            log.debug(buf.toString());
        }
    }
}
