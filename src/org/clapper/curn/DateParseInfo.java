/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Used by <i>curn</i> when parsing dates.
 */
class DateParseInfo
{
    SimpleDateFormat format;
    String           formatString;
    boolean          timeOnly;    // format contains only time info

    DateParseInfo (String fmtString, boolean timeOnly)
    {
        this.formatString = fmtString;
        this.format       = new SimpleDateFormat (fmtString);
        this.timeOnly     = timeOnly;
    }

    public String formatDate (Date date)
    {
        return format.format (date);
    }

    public String toString()
    {
        return format.toPattern();
    }
}
