        package id.co.vsi.scheduler.enums;

/**
 *
 * $Rev:: 747                                                                   $:Revision of last commit
 * $Author:: didik                                                              $:Author of last commit
 * $Date:: 2013-10-10 14:42:34 +0700 (Thu, 10 Oct 2013)                         $:Date of last commit
 *
 */
public enum ResponseCodeSCH {

    SUCCESS("0000-SUCCESS"),
    ERROR_UNKNOWN("0005-ERROR UNKNOWN"),
    ERROR_SCHEDULER("0014-ERROR SCHEDULER SERVICE"),
    ERROR_PARSE_MESSAGE("0015-ERROR PARSE MESSAGE"),
    ERROR_INVALID_MESSAGE("0030-ERROR INVALID MESSAGE"),
    ERROR_ALREADY("0054-ERROR SCHEDULER ALREADY RUNNING"),
    ERROR_TIMEOUT("0068-TIMEOUT"),
    ERROR_CUT_OFF_SESSION("0089-ERROR CUT OFF SESSION"),
    ERROR_DATABASE("0091-ERROR DATABASE");

    /**
     * Response code in string
     */
    private String mResponseCode;

    /**
     * Construct response code
     * @param pResponseCode
     */
    private ResponseCodeSCH(String pResponseCode)
    {
        mResponseCode = pResponseCode;
    }

    /**
     * Get NcResponseCode
     * @return NcResponseCode in string
     */
    public String getResponseCode()
    {
        return mResponseCode;
    }

    /**
     * Parse input string to appropriate <code>NcResponseCode</code>
     * @param pTypeString String to be parsed to NcResponseCode
     * @return appropriate NcResponseCode
     */
    public static ResponseCodeSCH parseResponseCode(String pTypeString)
    {
        ResponseCodeSCH tRetResponseCode = null;
        for (ResponseCodeSCH tResponseCode : values())
        {
            if (tResponseCode.getResponseCode().equals(pTypeString))
            {
                tRetResponseCode = tResponseCode;
                break;
            }
        }
        return tRetResponseCode;
    }
}