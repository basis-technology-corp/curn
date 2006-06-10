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

package org.clapper.curn.plugins;

import org.clapper.curn.Constants;
import org.clapper.curn.Curn;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.MainConfigItemPlugIn;
import org.clapper.curn.OutputHandler;
import org.clapper.curn.PostOutputPlugIn;
import org.clapper.curn.Version;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;
import org.clapper.util.mail.EmailMessage;
import org.clapper.util.mail.EmailTransport;
import org.clapper.util.mail.SMTPEmailTransport;
import org.clapper.util.mail.EmailAddress;
import org.clapper.util.mail.EmailException;
import org.clapper.util.misc.MIMETypeUtil;
import org.clapper.util.text.TextUtil;

import java.io.File;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The <tt>EmailOutputPlugIn</tt> handles emailing the output from a
 * <i>curn</i> run, if one or more email addresses are specified in the
 * configuration file. It intercepts the following main (<tt>[curn]</tt>)
 * section configuration parameters:
 *
 * <table border="1">
 *   <tr valign="top">
 *     <th align="left">Parameter</th>
 *     <th align="left">Meaning</th>
 *     <th align="left">Default</th>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>MailOutputTo</tt></td>
 *     <td>One or more comma- or blank-separated email addresses to receive
 *         an email containing the output.</td>
 *     <td>None</td>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>MailFrom</tt></td>
 *     <td>The email address to use as the sender of the message.</td>
 *     <td>The user running curn, and the current machine.</td>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>MailSubject</tt></td>
 *     <td>The subject to use for email messages.</td>
 *     <td>"RSS Feeds"</td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class EmailOutputPlugIn
    implements MainConfigItemPlugIn,
               PostOutputPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_EMAIL_OUTPUT_TO   = "MailOutputTo";
    private static final String VAR_SMTP_HOST         = "SMTPHost";
    private static final String DEF_SMTP_HOST         = "localhost";
    private static final String VAR_EMAIL_SENDER      = "MailSender";
    private static final String VAR_EMAIL_SUBJECT     = "MailSubject";
    private static final String DEF_EMAIL_SUBJECT     = "RSS Feeds";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Collection of email addresses
     */
    private Collection<EmailAddress> emailAddresses = null;

    /**
     * SMTP host to use
     */
    private String smtpHost = DEF_SMTP_HOST;

    /**
     * Email sender address
     */
    private EmailAddress emailSender = null;

    /**
     * Email subject
     */
    private String emailSubject = DEF_EMAIL_SUBJECT;

    /**
     * For log messages
     */
    private static Logger log = new Logger (EmailOutputPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public EmailOutputPlugIn()
    {
    }

    /*----------------------------------------------------------------------*\
               Public Methods Required by *PlugIn Interfaces
    \*----------------------------------------------------------------------*/

    /**
     * Get a displayable name for the plug-in.
     *
     * @return the name
     */
    public String getName()
    {
        return "Email Output";
    }

    /**
     * Get the sort key for this plug-in.
     *
     * @return the sort key string.
     */
    public String getSortKey()
    {
        return ClassUtil.getShortClassName (getClass().getName());
    }

    /**
     * Called immediately after <i>curn</i> has read and processed a
     * configuration item in the main [curn] configuration section. All
     * configuration items are passed, one by one, to each loaded plug-in.
     * If a plug-in class is not interested in a particular configuration
     * item, this method should simply return without doing anything. Note
     * that some configuration items may simply be variable assignment;
     * there's no real way to distinguish a variable assignment from a
     * blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param config       the {@link CurnConfig} object
     * 
     * @throws CurnException on error
     *
     * @see CurnConfig
     */
    public void runMainConfigItemPlugIn (String     sectionName,
                                         String     paramName,
                                         CurnConfig config)
	throws CurnException
    {
        try
        {
            if (paramName.equals (VAR_SMTP_HOST))
            {
                smtpHost = config.getConfigurationValue (sectionName,
                                                         paramName);
            }

            else if (paramName.equals (VAR_EMAIL_SENDER))
            {
                if (emailSender != null)
                {
                    throw new CurnException
                        (Constants.BUNDLE_NAME,
                         "EmailOutputPlugIn.senderAlreadyDefined",
                         "Section [{0}], configuration item \"{1}\": Email "
                       + "sender has already been defined.",
                         new Object[] {sectionName, paramName});
                }

                String sender = config.getConfigurationValue (sectionName,
                                                              paramName);
                try
                {
                    emailSender = new EmailAddress (sender);
                                             
                }

                catch (EmailException ex)
                {
                    throw new CurnException
                        (Constants.BUNDLE_NAME,
                         "EmailOutputPlugIn.badEmailAddress",
                         "Section [{0}], configuration item \"{1}\": "
                       + "\"{2}\" is an invalid email address",
                         new Object[] {sectionName, paramName, sender},
                         ex);
                }
            }

            else if (paramName.equals (VAR_EMAIL_SUBJECT))
            {
                emailSubject = config.getConfigurationValue (sectionName,
                                                             paramName);
            }

            else if (paramName.equals (VAR_EMAIL_OUTPUT_TO))
            {
                String addrList = config.getConfigurationValue (sectionName,
                                                                paramName);
                String[] addrs = TextUtil.split (addrList, ",");

                if ((addrs == null) || (addrs.length == 0))
                {
                    throw new CurnException
                        (Constants.BUNDLE_NAME,
                         "EmailOutputPlugIn.missingEmailAddresses",
                         "Missing email address(es) in {0} section "
                       + "configuration item \"{1}\"",
                         new Object[] {sectionName, paramName});
                }

                // Might as well validate them here.

                emailAddresses = new ArrayList<EmailAddress>();
                for (String addr : addrs)
                {
                    try
                    {
                        addr = addr.trim();
                        emailAddresses.add (new EmailAddress (addr));
                    }

                    catch (EmailException ex)
                    {
                        emailAddresses = null;
                        throw new CurnException
                            (Constants.BUNDLE_NAME,
                             "EmailOutputPlugIn.badEmailAddress",
                             "Section [{0}], configuration item \"{1}\": "
                           + "\"{2}\" is an invalid email address",
                             new Object[] {sectionName, paramName, addr},
                             ex);
                    }
                }
            }
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
        }
    }

    /**
     * Called after <i>curn</i> has flushed <i>all</i> output handlers. A
     * post-output plug-in is a useful place to consolidate the output from
     * all output handlers. For instance, such a plug-in might pack all the
     * output into a zip file, or email it.
     *
     * @param outputHandlers a <tt>Collection</tt> of the
     *                       {@link OutputHandler} objects (useful for
     *                       obtaining the output files, for instance).
     *
     * @throws CurnException on error
     *
     * @see OutputHandler
     */
    public void runPostOutputPlugIn (Collection<OutputHandler> outputHandlers)
	throws CurnException
    {
        if ((emailAddresses != null) && (emailAddresses.size() > 0))
        {
            log.debug ("There are email addresses.");
            emailOutput (outputHandlers, emailAddresses);
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private void emailOutput (Collection<OutputHandler> outputHandlers,
                              Collection<EmailAddress>  emailAddresses)
        throws CurnException
    {
        try
        {
            OutputHandler firstHandlerWithOutput = null;
            int           totalAttachments = 0;

            // First, figure out whether we have any attachments or not.

            for (OutputHandler handler : outputHandlers)
            {
                if (handler.hasGeneratedOutput())
                {
                    totalAttachments++;
                    if (firstHandlerWithOutput == null)
                        firstHandlerWithOutput = handler;
                }
            }

            if (totalAttachments == 0)
            {
                // None of the handlers produced any output.

                log.error ("Warning: None of the output handlers "
                         + "produced any emailable output.");
            }

            else
            {
                // Create an SMTP transport and a new email message.

                EmailTransport transport = new SMTPEmailTransport (smtpHost);
                EmailMessage   message = new EmailMessage();

                log.debug ("SMTP host = " + smtpHost);

                // Fill 'er up.

                for (EmailAddress emailAddress : emailAddresses)
                {
                    try
                    {
                        log.debug ("Email recipient = " + emailAddress);
                        message.addTo (emailAddress);
                    }

                    catch (EmailException ex)
                    {
                        throw new CurnException (ex);
                    }
                }

                message.addHeader ("X-Mailer", Version.getFullVersion());
                message.setSubject (emailSubject);

                if (emailSender != null)
                    message.setSender (emailSender);

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
                    OutputHandler handler = firstHandlerWithOutput;
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
                    for (OutputHandler handler : outputHandlers)
                    {
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
