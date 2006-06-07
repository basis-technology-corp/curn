<html> 
<head>
<meta http-equiv="Content-Type" content="text/html; charset=${encoding?upper_case}">
<meta name="GENERATOR" content="curn, version ${curn.version}">

<#--
  -----------------------------------------------------------------------
  curn: Customizable Utilitarian RSS Notifier

  FreeMarker template used to generate HTML output in conjunction with
  the curn FreeMarkerOutputHandler class.
  -----------------------------------------------------------------------
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
  -----------------------------------------------------------------------
  $Id$
-->
<title>${title}</title>

<style type="text/css">
<!--
   body               { background-color: #ffffff; color: #000000;
                        font-family: serif, "Times New Roman", Times; }
   span.channelTitle  { font-family: sans-serif, Arial, Helvetica;
                        font-weight: bold; }
   span.itemTitle     { font-family: sans-serif, Arial, Helvetica; }
   span.itemDesc      { font-family: serif, "Times New Roman", Times; }
   .heading           { background-color: #CCCCCC; }
   td.oddItemRow      { background-color: #CCCCCC; }
   td.oddChannel      { background-color: #ffffff; }
   td.smaller         { font-size: smaller; }
   a                  { text-decoration: none; }
   .toc               { font-family: sans-serif, Arial, Helvetica;
                        font-weight: bold; }
   .tocEntry          { font-family: sans-serif, Arial, Helvetica; }
-->
</style>
</head>

<body>
<h1>${title}</h1>
<p>${dateGenerated?string("dd MMM, yyyy, HH:mm:ss")}</p>

<#if (tableOfContents.needed)>
<span class="toc">Table of Contents</span>
<ul>
  <#list tableOfContents.channels as toc>
  <li class="TOCEntry"><a href="#${toc.channelAnchor}">${toc.title}</a> (${toc.totalItems} items)</li>
  </#list>
</ul>
</#if>

<table border="0" width="100%" summary="RSS Feeds"
       cellspacing="0" cellpadding="2">
  <tr><td colspan="3"><hr></td></tr>
  <tr valign="top">
    <th align="left"><b>Site</b></th>
    <th align="left"><b>Item</b></th>
    <th align="left"><b>Item Summary (if available)</b></th>
  </tr>
  <tr><td colspan="3"><hr></td></tr>

  <#assign row = 0>
  <#list channels as channel>
  <#list channel.items as item>
  <#assign row = row + 1>

  <tr valign="top">
    <#if (row % 2) = 0>
      <#assign evenOdd = "even">
    <#else>
      <#assign evenOdd = "odd">
    </#if>

    <#if (item.index = 1)>

    <td class="${evenOdd}Channel" align="left" rowspan="${channel.totalItems}">
        <a name="${channel.anchorName}"></a>
        <a href="${channel.url}">${channel.title}</a>&nbsp;&nbsp;<br>
        <#if channel.showDate && channel.date?exists>${channel.date?string("E, dd MMM, yyyy 'at' HH:mm:ss")}</#if>
    </td>

    </#if>

    <td class="${evenOdd}ItemRow" align="left">
      <table border="0" align="left" valign="top" summary="" cellpadding="0" cellspacing="0">

	<tr><td align="left"><a href="${item.url}">${item.title}</a></td></tr>
	<tr><td align="left"><#if item.showDate && item.date?exists>${item.date?string("E, dd MMM, yyyy 'at' HH:mm:ss")}</#if></td></tr>
        <#if item.showAuthor>
	<tr><td>${item.author}</td></tr>
	</#if>

      </table>
    </td>
    <td align="left" class="${evenOdd}ItemRow">${item.description}</td>
  </tr>
  </#list>
  <tr><td colspan="3"><hr></td></tr>

  </#list>

</table>

<#if (curn.showToolInfo)>
<table border="0" summary="curn configuration information">
  <tr valign="top">
    <td class="smaller"><a href="http://www.clapper.org/software/java/curn/"><i>curn</i></a>, version ${curn.version} (build ID ${curn.buildID})</td>
  </tr>
  <tr valign="top">
    <td class="smaller">Document generated on ${dateGenerated?string("dd MMMM, yyyy 'at' HH:mm:ss zzz")}</td>
  </tr>
  <tr valign="top">
    <td class="smaller">Configuration file URL: <tt>${configFile.url}</tt></td>
  </tr>
</table>
</#if>
</body>
</html>
