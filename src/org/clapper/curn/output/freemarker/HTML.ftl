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
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. The end-user documentation included with the redistribution, if any,
     must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

     Alternately, this acknowlegement may appear in the software itself,
     if wherever such third-party acknowlegements normally appear.

  3. Neither the names "clapper.org", "curn", nor any of the names of the
     project contributors may be used to endorse or promote products
     derived from this software without prior written permission. For
     written permission, please contact bmc@clapper.org.

  4. Products derived from this software may not be called "curn", nor may
     "clapper.org" appear in their names without prior written permission
     of Brian M. Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
  <#if toc.totalItems gt 1>
    <#assign plural = "s">
  <#else>
    <#assign plural = "">
  </#if>
  <li class="TOCEntry"><a href="#${toc.channelAnchor}">${toc.title}</a> (${toc.totalItems} item${plural})</li>
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
        <#if channel.date?exists>${channel.date?string("E, dd MMM, yyyy 'at' HH:mm:ss")}</#if>
        <#if (channel.rssFormat?exists)>{channel.rssFormat}</#if>
    </td>

    </#if>

    <td class="${evenOdd}ItemRow" align="left">
      <table border="0" align="left" valign="top" summary="" cellpadding="0" cellspacing="0">

	<tr><td align="left"><a href="${item.url}">${item.title}</a></td></tr>
	<tr><td align="left"><#if item.date?exists>${item.date?string("E, dd MMM, yyyy 'at' HH:mm:ss")}</#if></td></tr>
        <#if item.author?exists>
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
    <td class="smaller"><a href="http://www.clapper.org/software/java/curn/"><i>curn</i></a>, version ${curn.version} (build ${curn.buildID})</td>
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
