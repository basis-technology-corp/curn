<#--
  -----------------------------------------------------------------------
  curn: Customizable Utilitarian RSS Notifier

  FreeMarker template used to generate ATOM output from a feed,
  with the curn SaveAsRSSPlugIn plug-in class.
  -----------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M.a Clapper.

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
  $Id: Text.ftl 6688 2007-04-17 22:52:19Z bmc $
-->
<?xml version="1.0" encoding="${encoding}"?>
<!--
curn, ${curn.version} (build ${curn.buildID})
Generated ${dateGenerated?string("EEEEEE, dd MMMM, yyyy 'at' HH:mm:ss zzz")}
-->

<#-- Only works if the number of channels is 1. -->

<#if (channels?size > 1)>
<#stop "Atom Freemarker template only supports one feed at a time.">
</#if>

<#assign channel = channels?first>

<feed xmlns="http://www.w3.org/2005/Atom">

  <title>${escapeHTML(title)}</title>
  <link href="${channel.url}"/>
  <#if channel.date?exists>
  <updated>${channel.date?string("yyyy-MM-dd'T'HH:mm:ssZ")}</updated>
  </#if>
  <#list channel.authors as author>
  <author>
    <name>${author}</name>
  </author>
  </#list>
  <id>${channel.id}</id>

  <#list channel.items as item>
  <entry>
    <title>${escapeHTML(item.title)}</title>
    <link href="${item.url}"/>
    <id>${item.id}</id>
    <#if item.date?exists>
    <updated>${item.date?string("yyyy-MM-dd'T'HH:mm:ssZ")}</updated>
    </#if>
    <summary>${escapeHTML(item.description)}</summary>
    <#list item.authors as author>
    <author>
      <name>${escapeHTML(author)}</name>
    </author>
    </#list>
  </entry>
  </#list>

</feed>
