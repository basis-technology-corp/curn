/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.text.SimpleDateFormat;

/**
 * Used by <i>rssget</i> when parsing dates.
 */
class DateParseInfo
{
    SimpleDateFormat format;
    boolean          timeOnly;    // format contains only time info

    DateParseInfo (String fmtString, boolean timeOnly)
    {
        this.format   = new SimpleDateFormat (fmtString);
        this.timeOnly = timeOnly;
    }

    public String toString()
    {
        return format.toPattern();
    }
}
