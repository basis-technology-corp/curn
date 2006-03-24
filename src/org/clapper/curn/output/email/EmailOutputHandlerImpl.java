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

package org.clapper.curn.output.email;

import org.clapper.curn.ConfigFile;
import org.clapper.curn.ConfiguredOutputHandler;
import org.clapper.curn.CurnException;
import org.clapper.curn.EmailOutputHandler;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.OutputHandler;
import org.clapper.curn.Version;
import org.clapper.curn.parser.RSSChannel;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;
import org.clapper.util.mail.EmailMessage;
import org.clapper.util.mail.EmailTransport;
import org.clapper.util.mail.SMTPEmailTransport;
import org.clapper.util.mail.EmailAddress;
import org.clapper.util.mail.EmailException;
import org.clapper.util.misc.MIMETypeUtil;

import java.io.File;
import java.text.DecimalFormat;
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
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private Collection<ConfiguredOutputHandler>  handlers =
        new ArrayList<ConfiguredOutputHandler>();

    private Collection<EmailAddress> recipients =
        new ArrayList<EmailAddress>();

    private ConfigFile  config     = null;

    /**
     * For log messages
     */
    private static Logger log = new Logger (EmailOutputHandlerImpl.class);

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
            ConfiguredOutputHandler handlerWrapper;
            OutputHandler           handler;

            handlerWrapper = (ConfiguredOutputHandler) it.next();
            handler        = handlerWrapper.getOutputHandler();

            handler.init (config, handlerWrapper);
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
            ConfiguredOutputHandler handlerWrapper;
            OutputHandler           handler;

            handlerWrapper = (ConfiguredOutputHandler) it.next();
            handler        = handlerWrapper.getOutputHandler();

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
        {
            ConfiguredOutputHandler handlerWrapper;
            OutputHandler           handler;

            handlerWrapper = (ConfiguredOutputHandler) it.next();
            handler        = handlerWrapper.getOutputHandler();

            handler.flush();
        }

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
     * Add a <tt>ConfiguredOutputHandler</tt> to this handler. The output
     * handler will be used to generate output that is attached to the
     * email message. This method must be called <i>after</i>
     * <tt>init()</tt> is called.
     *
     * @param handler  the handler to add
     *
     * @throws CurnException error opening a temporary file for the output
     */
    public void addOutputHandler (ConfiguredOutputHandler handler)
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
     * Get the <tt>File</tt> that represents the output produced by the
     * handler, if applicable. Note that this handler produces no output
     * and always returns null.
     *
     * @return null, unconditionally
     *
     * @throws CurnException an error occurred
     */
    public File getGeneratedOutput()
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
            Iterator                 it;
            OutputHandler            firstHandlerWithOutput = null;
            OutputHandler            handler;
            ConfiguredOutputHandler  handlerWrapper;
            int                      totalAttachments = 0;

            // First, figure out whether we have any attachments or not.

            for (it = handlers.iterator(); it.hasNext(); )
            {
                handlerWrapper = (ConfiguredOutputHandler) it.next();
                handler        = handlerWrapper.getOutputHandler();

                if (handler.hasGeneratedOutput())
                {
                    totalAttachments++;
                    if (firstHandlerWithOutput == null)
                    {
                        log.debug ("First handler with output="
                                 + handlerWrapper.getName());
                        firstHandlerWithOutput = handler;
                    }
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

                String smtpHost = config.getSMTPHost();
                String sender = config.getEmailSender();
                EmailTransport transport = new SMTPEmailTransport (smtpHost);
                EmailMessage message = new EmailMessage();

                log.debug ("SMTP host = " + smtpHost);

                // Fill 'er up.

                for (it = recipients.iterator(); it.hasNext(); )
                    message.addTo ((EmailAddress) it.next());

                message.addHeader ("X-Mailer", Version.getFullVersion());
                message.setSubject (config.getEmailSubject());

                if (sender != null)
                    message.setSender (sender);

                if (log.isDebugEnabled())
                    log.debug ("Email sender = " + message.getSender());

                // Add the output. If there's only one attachment, and its
                // output is text, then there's no need for attachments.
                // Just set it as the text part, and set the appropriate
                // Content-type: header. Otherwise, make a
                // multipart-alternative message with separate attachments
                // for each output.

                DecimalFormat fmt  = new DecimalFormat ("##000");
                StringBuffer  name = new StringBuffer();
                String        ext;
                String        contentType;
                File          file;

                if (totalAttachments == 1)
                {
                    handler = firstHandlerWithOutput;
                    contentType = handler.getContentType();
                    ext = MIMETypeUtil.fileExtensionForMIMEType (contentType);
                    file = handler.getGeneratedOutput();
                    message.setMultipartSubtype (EmailMessage.MULTIPART_MIXED);

                    name.append (fmt.format (1));
                    name.append ('.');
                    name.append (ext);

                    if (contentType.startsWith ("text/"))
                        message.setText (file, name.toString(), contentType);
                    else
                        message.addAttachment (file,
                                               name.toString(),
                                               contentType);
                }

                else
                {
                    message.setMultipartSubtype
                                          (EmailMessage.MULTIPART_ALTERNATIVE);

                    int i = 1;
                    for (it = handlers.iterator(); it.hasNext(); )
                    {
                        handlerWrapper = (ConfiguredOutputHandler) it.next();
                        handler        = handlerWrapper.getOutputHandler();

                        contentType = handler.getContentType();
                        ext = MIMETypeUtil.fileExtensionForMIMEType
                                                                (contentType);
                        file = handler.getGeneratedOutput();
                        if (file != null)
                        {
                            name.setLength (0);
                            name.append (fmt.format (i));
                            name.append ('.');
                            name.append (ext);
                            i++;
                            message.addAttachment (file,
                                                   name.toString(),
                                                   contentType);
                        }
                    }
                }

                log.debug ("Sending message.");
                transport.send (message);
                message.clear();
            }
        }

        catch (EmailException ex)
        {
            throw new CurnException (ex);
        }
    }
}
