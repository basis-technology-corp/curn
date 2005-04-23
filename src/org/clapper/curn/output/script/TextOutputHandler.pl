# $Id$
# ---------------------------------------------------------------------------
# This software is released under a Berkeley-style license:
# 
# Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.
# 
# Redistribution and use in source and binary forms are permitted provided
# that: (1) source distributions retain this entire copyright notice and
# comment; and (2) modifications made to the software are prominently
# mentioned, and a copy of the original software (or a pointer to its
# location) are included. The name of the author may not be used to endorse
# or promote products derived from this software without specific prior
# written permission.
# 
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
# 
# Effectively, this means you can do what you want with the software except
# remove this notice or take advantage of the author's name. If you modify
# the software and redistribute your modified version, you must indicate that
# your version is a modification of the original, and you must provide either
# a pointer to or a copy of the original.
# 
# ---------------------------------------------------------------------------
#
# Perl version of the curn-supplied TextOutputHandler
# (org.clapper.curn.output.TextOutputHandler) class. This script must be
# used in conjunction with curn's ScriptOutputHandler class
# (org.clapper.curn.output.script.ScriptOutputHandler), which requires the
# presence of the Jakarta version of the Bean Scripting Framework (BSF).
#
# WARNING!
#
# This code does not actually work yet! I put it together to test bsfperl
# (bsfperl.sourceforge.net), but bsfperl can't yet cope with complex Java
# classes published via the BSFManager.
#
# This code exists solely as a placeholder for now.
# ---------------------------------------------------------------------------

import org::clapper::curn::CurnException
import org::clapper::util::io::WordWrapWriter

$HORIZONTAL_RULE = "---------------------------------------" 
                 . "---------------------------------------";


sub processChannels()
{
    # If we didn't care about wrapping the output, we'd just use:
    #
    #     out = open (self.outputPath, "w")
    #
    # But it'd be nice to wrap long summaries on word boundaries at
    # the end of an 80-character line. For that reason, we use the
    # Java org.clapper.util.io.WordWrapWriter class.

    $out = new WordWrapWriter (open ($outputPath));
    $out->setPrefix ("");

	$msg = $config->getOptionalStringValue ($sectionName, "Message", "");

    $totalNew = 0;

    # First, count the total number of new items

    $iterator = $channels->iterator();
    while ($iterator->hasNext())
    {
        $channel_wrapper = $iterator->next();
        $channel = $channel_wrapper->getChannel();
        $totalNew = $totalNew + $channel->getItems()->size();
    }

    if ($totalNew > 0)
    {
        # If the config file specifies a message for this handler,
        # display it.

        if ($msg != "")
	{
            $out->println ($msg);
	    $out->println ();
	}

        # Now, process the items

        $indentation = 0;
        $iterator = $channels->iterator();
        while ($iterator.hasNext())
	{
            $channel_wrapper = $iterator->next();
            $channel = $channel_wrapper->getChannel();
            $feed_info = $channel_wrapper->getFeedInfo();
            process_channel ($out, $channel, $feed_info, $indentation);
	}

        $mimeTypeBuf->append ("text/plain");

        # Output a footer

        indent ($out, $indentation);
        $out->println ();
        $out->println ($HORIZONTAL_RULE);
        $out->println ($version);
        $out->flush();
    }
}

sub process_channel()
{
    my $out = shift;
    my $channel = shift;
    my $feed_info = shift;
    my $indentation = shift;

    printf STDOUT ("Processing channel: %s\n", $channel->getLink()->toString());
    $logger->debug ("Processing channel \"" . $channel->getLink()->toString() . "\"");

    # Print a channel header

    indent ($out, $indentation);
    $out->println ($HORIZONTAL_RULE);
    $out->println ($channel->getTitle());
    $out->println ($channel->getLink()->toString());
    $out->println ($channel->getItems()->size() . " item(s)");
    if ($config->showDates())
    {
        $date = $channel->getPublicationDate();
        if (defined ($date) && ($date ne ""))
	{
            $out->println ($date->toString());
	}
    }

    if ($config->showRSSVersion())
    {
        $out->println ("(Format: " . $channel->getRSSFormat() . ")");
    }

    $indentation++;
    indent ($out, $indentation);
    $iterator = $channel->getItems()->iterator();
    while ($iterator->hasNext())
    {
        # These are RSSItem objects
        $item = $iterator->next();

        $out->println();
        $out->println ($item->getTitle());
        $out->println ($item->getLink()->toString());

        if ($config->showDates())
	{
            $date = $item->getPublicationDate();
            if (defined ($date) && ($date ne ""))
	    {
                $out->println ($date->toString());
	    }
	}

	$out->println();

        if (! $feed_info->summarizeOnly())
	{
            $summary = $item->getSummary();
            if (defined ($summary) && ($summary ne ""))
	    {
                indent ($out, $indentation + 1);
                $out->println ($summary);
                indent ($out, $indentation);
	    }
	}
    }
}

sub indent()
{
    my $out = shift;
    my $indentation = shift;

    $prefix = "";
    while ($indentation-- > 0)
    {
        $prefix = $prefix . "    ";
    }

    $out->setPrefix ($prefix);
}

# ---------------------------------------------------------------------------

$channels    = $bsf->lookupBean ("channels");
$outputPath  = $bsf->lookupBean ("outputPath");
$mimeTypeBuf = $bsf->lookupBean ("mimeType");
$config      = $bsf->lookupBean ("config");
$sectionName = $bsf->lookupBean ("configSection");
$logger      = $bsf->lookupBean ("logger");;
$version     = $bsf->lookupBean ("version");
$message     = "";

processChannels();
