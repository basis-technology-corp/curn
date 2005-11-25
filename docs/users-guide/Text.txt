<#--
  -----------------------------------------------------------------------
  curn: Customizable Utilitarian RSS Notifier

  FreeMarker template used to generate plain text output in conjunction
  with the curn FreeMarkerOutputHandler class.
  -----------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.

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
  -----------------------------------------------------------------------
  $Id$
-->
${title}
<#if extraText != "">
${wrapText (extraText)}
</#if>

<#list channels as channel>
---------------------------------------------------------------------------
${wrapText (channel.title, 0)}
${channel.url}
<#if channel.showDate && channel.date?exists>
${channel.date?string("E, dd MMM, yyyy 'at' HH:mm:ss")}
</#if>

<#list channel.items as item>
${wrapText (item.title, 4)}
${indentText (item.url, 4)}
<#assign desc = stripHTML(item.description)>
<#if desc != "">

${wrapText (desc, 8)}
</#if>

</#list>
</#list>

---------------------------------------------------------------------------
<#if (curn.showToolInfo)>
curn, ${curn.version}
Generated ${dateGenerated?string("EEEEEE, dd MMMM, yyyy 'at' HH:mm:ss zzz")}
</#if>
