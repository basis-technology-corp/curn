/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004 Brian M. Clapper. All rights reserved.

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

package org.clapper.curn.output.email;

import org.clapper.curn.ConfigFile;
import org.clapper.curn.ConfiguredOutputHandler;
import org.clapper.curn.CurnException;
import org.clapper.curn.EmailOutputHandler;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.OutputHandler;
import org.clapper.curn.Version;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.util.Util;

import org.clapper.util.mail.EmailMessage;
import org.clapper.util.mail.EmailTransport;
import org.clapper.util.mail.SMTPEmailTransport;
import org.clapper.util.mail.EmailAddress;
import org.clapper.util.mail.EmailException;

import org.clapper.util.config.ConfigurationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Provides an output handler that wraps the real output handlers and
 * sends an email message, using the contents of the real handlers.
 *
 * @see OutputHandler
 * @see org.clapper.curn.Curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class EmailOutputHandlerImpl implements EmailOutputHandler
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private Collection  handlers   = new ArrayList();
    private Collection  recipients = new ArrayList();
    private ConfigFile  config     = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>EmailOutputHandlerImpl</tt>.
     */
    public EmailOutputHandlerImpl()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Initializes the output handler for another set of RSS channels.
     *
     * @param config     the parsed <i>curn</i> configuration data. The
     *                   output handler is responsible for retrieving its
     *                   own parameters from the configuration, by calling
     *                   <tt>config.getOutputHandlerSpecificVariables()</tt>
     * @param cfgHandler the <tt>ConfiguredOutputHandler</tt> wrapper
     *                   containing this object; the wrapper has some useful
     *                   metadata, such as the object's configuration section
     *                   name and extra variables.
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other initialization error
     */
    public void init (ConfigFile config, ConfiguredOutputHandler cfgHandler)
        throws ConfigurationException,
               CurnException
    {
        this.config = config;

	for (Iterator it = handlers.iterator(); it.hasNext(); )
        {
            OutputHandler handler = (OutputHandler) it.next();
            handler.init (config, cfgHandler);
        }
    }

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class.
     *
     * @param channel  The channel containing the items to emit. The method
     *                 should emit all the items in the channel; the caller
     *                 is responsible for clearing out any items that should
     *                 not be seen.
     * @param feedInfo Information about the feed, from the configuration
     *
     * @throws CurnException  unable to write output
     */
    public void displayChannel (RSSChannel  channel,
                                FeedInfo    feedInfo)
        throws CurnException
    {
	for (Iterator it = handlers.iterator(); it.hasNext(); )
        {
            OutputHandler handler = (OutputHandler) it.next();
            handler.displayChannel (channel, feedInfo);
        }
    }
    
    /**
     * Flush any buffered-up output. <i>curn</i> calls this method
     * once, after calling <tt>displayChannelItems()</tt> for all channels.
     *
     * @throws CurnException  unable to write output
     */
    public void flush() throws CurnException
    {
        for (Iterator it = handlers.iterator(); it.hasNext(); )
            ((OutputHandler) it.next()).flush();

        emailOutput();
    }

    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public String getContentType()
    {
	// Not really applicable.

        return null;
    }

    /**
     * Add an <tt>OutputHandler</tt> to this handler. The output handler
     * will be used to generate output that is attached to the email message.
     * This method must be called <i>after</i> <tt>init()</tt> is called.
     *
     * @param handler  the handler to add
     *
     * @throws CurnException error opening a temporary file for the output
     */
    public void addOutputHandler (OutputHandler handler)
	throws CurnException
    {
	handlers.add (handler);
    }

    /**
     * Add one or more email addresses to the output handler. The
     * <tt>flush()</tt> method actually sends the message.
     *
     * @param emailAddress  email address to add
     *
     * @throws CurnException  bad email address
     */
    public void addRecipient (String emailAddress)
        throws CurnException
    {
        try
        {
            recipients.add (new EmailAddress (emailAddress));
        }

        catch (EmailException ex)
        {
            throw new CurnException (ex);
        }
    }

    /**
     * Get an <tt>InputStream</tt> that can be used to read the output data
     * produced by the handler, if applicable.
     *
     * @return an open input stream, or null if no suitable output was produced
     *
     * @throws CurnException an error occurred
     *
     * @see #hasGeneratedOutput
     * @see #getContentType
     */
    public InputStream getGeneratedOutput()
        throws CurnException
    {
        return null;
    }

    /**
     * Determine whether this handler has produced any actual output (i.e.,
     * whether {@link #getGeneratedOutput()} will return a non-null
     * <tt>InputStream</tt> if called).
     *
     * @return <tt>true</tt> if the handler has produced output,
     *         <tt>false</tt> if not
     *
     * @see #getGeneratedOutput
     * @see #getContentType
     */
    public boolean hasGeneratedOutput()
    {
        return false;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Send buffered-up output to the specified list of recipients.
     *
     * @param recipients  collection of email addresses, as strings.
     *
     * @throws CurnException  on error
     */
    private void emailOutput()
        throws CurnException
    {
        try
        {
            Iterator       it;
            OutputHandler  firstHandlerWithOutput = null;
            OutputHandler  handler;
            int            totalAttachments = 0;

            // First, figure out whether we have any attachments or not.

            for (it = handlers.iterator(); it.hasNext(); )
            {
                handler = (OutputHandler) it.next();
                if (handler.hasGeneratedOutput())
                {
                    totalAttachments++;
                    firstHandlerWithOutput = handler;
                }
            }

            if (totalAttachments == 0)
            {
                // None of the handlers produced any output.

                System.err.println ("Warning: None of the output handlers "
                                  + "produced any emailable output.");
            }

            else
            {
                // Create an SMTP transport and a new email message.

                EmailMessage message = new EmailMessage();
                String smtpHost = config.getSMTPHost();
                EmailTransport transport = new SMTPEmailTransport (smtpHost);

                // Fill 'er up.

                for (it = recipients.iterator(); it.hasNext(); )
                    message.addTo ((EmailAddress) it.next());

                message.addHeader ("X-Mailer",
                                   "curn, version " + Version.VERSION);
                message.setSubject (config.getEmailSubject());

                // Add the output. If there's only one attachment, and its
                // output is text, then there's no need for attachments.
                // Just set it as the text part, and set the appropriate
                // Content-type: header. Otherwise, make a
                // multipart-alternative message with separate attachments
                // for each output.

                if (totalAttachments == 1)
                {
                    handler = firstHandlerWithOutput;
                    String contentType = handler.getContentType();
                    InputStream is = handler.getGeneratedOutput();
                    message.setMultipartSubtype (EmailMessage.MULTIPART_MIXED);
                    if (contentType.startsWith ("text/"))
                        message.setText (is, contentType);
                    else
                        message.addAttachment (is, contentType);
                }

                else
                {
                    message.setMultipartSubtype
                                          (EmailMessage.MULTIPART_ALTERNATIVE);

                    for (it = handlers.iterator(); it.hasNext(); )
                    {
                        handler = (OutputHandler) it.next();

                        InputStream is = handler.getGeneratedOutput();
                        if (is != null)
                        {
                            message.addAttachment (is,
                                                   handler.getContentType());
                        }
                    }
                }

                transport.send (message);
            }
        }

        catch (EmailException ex)
        {
            throw new CurnException (ex);
        }
    }
}
