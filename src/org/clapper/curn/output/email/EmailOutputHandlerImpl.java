/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn.email;

import org.clapper.curn.OutputHandler;
import org.clapper.curn.EmailOutputHandler;
import org.clapper.curn.OutputHandlerContainer;
import org.clapper.curn.Util;
import org.clapper.curn.Version;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.ConfigFile;
import org.clapper.curn.FeedException;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.mail.EmailMessage;
import org.clapper.util.mail.EmailTransport;
import org.clapper.util.mail.SMTPEmailTransport;
import org.clapper.util.mail.EmailAddress;
import org.clapper.util.mail.EmailException;

import org.clapper.util.config.ConfigurationException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Provides an output handler that wraps the real output handlers and
 * sends an email message, using the contents of the real handlers.
 *
 * @see OutputHandler
 * @see org.clapper.curn.curn
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
     * @param writer  the <tt>OutputStreamWriter</tt> where the handler
     *                should send output. Not used here.
     * @param config  the parsed <i>curn</i> configuration data
     *
     * @throws ConfigurationException  configuration error
     * @throws FeedException           some other initialization error
     */
    public void init (OutputStreamWriter writer, ConfigFile config)
        throws ConfigurationException,
               FeedException
    {
        this.config = config;

	for (Iterator it = handlers.iterator(); it.hasNext(); )
        {
            OutputHandlerContainer entry = (OutputHandlerContainer) it.next();

            entry.init (config);
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
     * @throws FeedException  unable to write output
     */
    public void displayChannel (RSSChannel  channel,
                                FeedInfo    feedInfo)
        throws FeedException
    {
	for (Iterator it = handlers.iterator(); it.hasNext(); )
        {
            OutputHandlerContainer entry = (OutputHandlerContainer) it.next();
            OutputHandler handler = entry.getOutputHandler();

            handler.displayChannel (channel, feedInfo);
        }
    }
    
    /**
     * Flush any buffered-up output. <i>curn</i> calls this method
     * once, after calling <tt>displayChannelItems()</tt> for all channels.
     *
     * @throws FeedException  unable to write output
     */
    public void flush() throws FeedException
    {
        for (Iterator it = handlers.iterator(); it.hasNext(); )
        {
            OutputHandlerContainer entry = (OutputHandlerContainer) it.next();
            OutputHandler handler = entry.getOutputHandler();

            handler.flush();
            entry.close();
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
     * Add an <tt>OutputHandler</tt> to this handler. The output handler
     * will be used to generate output that is attached to the email message.
     * This method must be called <i>after</i> <tt>init()</tt> is called.
     *
     * @param handler  the handler to add
     *
     * @throws FeedException error opening a temporary file for the output
     */
    public void addOutputHandler (OutputHandler handler)
	throws FeedException
    {
	handlers.add (new OutputHandlerContainer (handler));
    }

    /**
     * Add one or more email addresses to the output handler. The
     * <tt>flush()</tt> method actually sends the message.
     *
     * @param emailAddress  email address to add
     *
     * @throws FeedException  bad email address
     */
    public void addRecipient (String emailAddress)
        throws FeedException
    {
        try
        {
            recipients.add (new EmailAddress (emailAddress));
        }

        catch (EmailException ex)
        {
            throw new FeedException (ex);
        }
    }

    /**
     * Determine whether this <tt>OutputHandler</tt> wants a file for its
     * output or not. For example, a handler that produces text output
     * wants a file, or something similar, to receive the text; such a
     * handler would return <tt>true</tt> when this method is called. By
     * contrast, a handler that swallows its output, or a handler that
     * writes to a network connection, does not want a file to receive
     * output.
     *
     * @return <tt>true</tt> if the handler wants a file or file-like object
     *         for its output, and <tt>false</tt> otherwise
     */
    public boolean wantsOutputFile()
    {
        // Not really applicable

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
     * @throws FeedException  on error
     */
    private void emailOutput()
        throws FeedException
    {
        try
        {
            Iterator               it;
            OutputHandlerContainer entry;
            OutputHandlerContainer firstEntryWithOutput = null;
            OutputHandler          handler;
            int                    totalAttachments = 0;

            // First, figure out whether we have any attachments or not.

            for (it = handlers.iterator(); it.hasNext(); )
            {
                entry = (OutputHandlerContainer) it.next();
                File tempFile = entry.getTempFile();

                if ((tempFile != null) && (tempFile.length() > 0))
                {
                    totalAttachments++;
                    firstEntryWithOutput = entry;
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
                    entry = firstEntryWithOutput;
                    handler = entry.getOutputHandler();
                    String contentType = handler.getContentType();

                    File tempFile = entry.getTempFile();
                    message.setMultipartSubtype (EmailMessage.MULTIPART_MIXED);
                    if (contentType.startsWith ("text/"))
                        message.setText (tempFile, contentType);
                    else
                        message.addAttachment (tempFile, contentType);
                }

                else
                {
                    message.setMultipartSubtype
                                          (EmailMessage.MULTIPART_ALTERNATIVE);

                    for (it = handlers.iterator(); it.hasNext(); )
                    {
                        entry = (OutputHandlerContainer) it.next();
                        handler = entry.getOutputHandler();

                        if (entry.hasOutput())
                        {
                            message.addAttachment (entry.getTempFile(),
                                                   handler.getContentType());
                        }
                    }
                }

                transport.send (message);
            }
        }

        catch (EmailException ex)
        {
            throw new FeedException (ex);
        }
    }
}
