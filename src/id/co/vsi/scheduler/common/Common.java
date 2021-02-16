package id.co.vsi.scheduler.common;

import id.co.vsi.common.constants.ResponseCode;
import id.co.vsi.common.log.LogType;
import id.co.vsi.common.log.SystemLog;
import id.co.vsi.common.settings.SystemConfig;
import id.co.vsi.common.settings.SystemConfig.ConfigHelper;
import id.co.vsi.scheduler.enums.ResponseCodeSCH;
import id.co.vsi.scheduler.plugin.Main;
import id.co.vsi.systemcore.isocore.SystemException;
import id.co.vsi.systemcore.jasoncore.JSONMessage;
import id.co.vsi.systemcore.jasoncore.JSONPlugin;
import id.co.vsi.systemcore.jasoncore.JSONPluginHandler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * $Rev:: 716 $:Revision of last commit $Author:: didik $:Author of last commit
 * $Date:: 2013-10-01 23:46:10 +0700 (Tue, 01 Oct 2013) $:Date of last commit
 *
 */
public abstract class Common extends JSONPluginHandler {

    /*xml general configuration parameter */
    public static Main cLoggingStaticHookObject = new Main();
    public static String cModuleNameSpace = "scheduler-controller";
    public static ConfigHelper cConfigHelper = SystemConfig.getNameSpace(Common.cModuleNameSpace);
    public static final String cJobGroup = "quartz-group";
    public static final String cMessagingRequest = "messaging-request";
    public static final String cUseNetmanDB = "use-netman-db";
    public static final String cUseNetmanDBLog = "use-netman-db-log";
    public static final String cDbConnectionString = "db-connection-string";
    public static final String cDbSchedulerTable = "scheduler-table";
    public static final String cDbTableName = cConfigHelper.getStringParameter(cDbSchedulerTable, "scheduler");
    public static final byte cEndMessageByte = -0x01;

    public Common(JSONPlugin pParentPlugin) {
        super(pParentPlugin);
    }

    /**
     * Generate a CRON expression is a string comprising 6 or 7 fields separated
     * by white space.
     *
     * @param pSeconds mandatory = yes. allowed values = {@code  0-59    * / , -}
     * @param pMinutes mandatory = yes. allowed values = {@code  0-59    * / , -}
     * @param pHours mandatory = yes. allowed values = {@code 0-23   * / , -}
     * @param pDayOfMonth mandatory = yes. allowed values =
     * {@code 1-31  * / , - ? L W}
     * @param pPonth mandatory = yes. allowed values =
     * {@code 1-12 or JAN-DEC    * / , -}
     * @param pDayOfWeek mandatory = yes. allowed values =
     * {@code 0-6 or SUN-SAT * / , - ? L #}
     * @param year mandatory = no. allowed values = {@code 1970–2099    * / , -}
     * @return a CRON Formatted String.
     */
    private static String generateCronExpressionMinuteHour(final String pSeconds,
            final String pMinutes,
            final String pHours,
            final String pDayOfMonth,
            final String pPonth,
            final String pDayOfWeek) {
        return String.format("%1$s %2$s %3$s %4$s %5$s %6$s", pSeconds, pMinutes, pHours, pDayOfMonth, pPonth, pDayOfWeek);
    }

    public static String convertCronExpressionByMinuteHour(final String pTime) {
        return generateCronExpressionMinuteHour("0", pTime.split(":")[1], pTime.split(":")[0], "*", "*", "?");
    }

    public static String convertCronExpressionByMinutes(final String pMinutes) {
        String tMinutes = pMinutes;
        Integer tInt = Integer.valueOf(pMinutes);
        Integer tHoursCalc = tInt / 60;

        if (tHoursCalc >= 1) {
            String tHours = String.valueOf(tHoursCalc);
            if (tHoursCalc == 1) {
                return generateCronExpressionMinuteHour("0", "0", "*", "*", "*", "?");
            }
            return generateCronExpressionMinuteHour("0", "0", "0/" + tHours, "*", "*", "?");
        }
        return generateCronExpressionMinuteHour("0", "0/" + tMinutes, "*", "*", "*", "?");
    }

    public static String formatDateTime(final String pDateTime,
            final String pPatternInput,
            final String pPatternExpected) {
        String formattedDate = "";

        try {
            SimpleDateFormat tDateYears = new SimpleDateFormat(pPatternInput);
            Date tDate = tDateYears.parse(pDateTime);
            tDateYears = new SimpleDateFormat(pPatternExpected);
            formattedDate = tDateYears.format(tDate);

        } catch (ParseException ex) {
        }

        return formattedDate;
    }

    public boolean isJSONValid(String pMayBeJSON) {
        boolean valid = false;
        try {
            new JSONObject(pMayBeJSON);
            valid = true;
        } catch (JSONException ex) {
            valid = false;
        }
        return valid;
    }

    public static String typeResponse(final JSONObject pJsonRequest) {
        String tResponseMT = null;
        String tMT = pJsonRequest.getString("MT");

        if (tMT.length() == 4) {
            tResponseMT = tMT.substring(0, 2) + "10";
        }

        return tResponseMT;
    }

    protected JSONObject selectResponseCode(final String pResponseCode,
            final String pErrorMessage) {
        JSONObject tResponseCode = new JSONObject();

        String[] tSplitRC = pResponseCode.split("-");

        tResponseCode.put("RC", tSplitRC[0]);
        tResponseCode.put("RCM", tSplitRC[1]);
        tResponseCode.put("RCD", pErrorMessage);

        return tResponseCode;
    }

    protected JSONObject selectResponseCode(final ResponseCodeSCH pResponseCode) {
        JSONObject tResponseCode = new JSONObject();

        String[] tSplitRC = pResponseCode.getResponseCode().split("-");

        tResponseCode.put("RC", tSplitRC[0]);
        tResponseCode.put("RCM", pResponseCode.name());
        tResponseCode.put("RCD", tSplitRC[1]);

        return tResponseCode;
    }

    protected JSONObject constructErrorSystemException(JSONMessage pRequestMessage,
            SystemException pEx) {
        JSONObject tErrorJsonMessage = new JSONObject();

        try {
            tErrorJsonMessage = new JSONObject(pRequestMessage.toString());
            ResponseCode tExceptionResponseCode = pEx.getResponseCode();

            if (tExceptionResponseCode == null) {
                tExceptionResponseCode = ResponseCode.ERROR_OTHER;

                SystemLog.getSingleton().log(this, LogType.ERROR,
                        "Response Code is null, replacing response code with " + tExceptionResponseCode
                        + ". SystemException generating stack : " + pEx.getStackTraceString());
            }

            tErrorJsonMessage.put("RC", selectResponseCode(pEx.getResponseCode().getResponseCodeString(), pEx.getStackTraceString()));
        } catch (JSONException ex) {
            SystemLog.getSingleton().log(this, LogType.ERROR, "Cannot Construct Error Message on constructErrorSystemMessage() : " + ex.getMessage());
        }

        return tErrorJsonMessage;
    }

    protected JSONObject constructErrorJSONException(JSONMessage pRequestMessage,
            JSONException pEx) {
        JSONObject tErrorJsonMessage = new JSONObject();

        try {
            String tExceptionResponseCode = pEx.getMessage();
            String tExceptionResponseMsg = pEx.getMessage();
            tErrorJsonMessage = new JSONObject(pRequestMessage.toString());

            boolean isJSONValid = isJSONValid(pEx.getMessage());

            if (tExceptionResponseCode == null) {
                tExceptionResponseCode = ResponseCodeSCH.ERROR_UNKNOWN.getResponseCode();

                SystemLog.getSingleton().log(this, LogType.ERROR,
                        "Response Code is null, replacing response code with " + tExceptionResponseCode
                        + ". JSONException generating stack : " + pEx.getMessage());
            } else if (!isJSONValid && tExceptionResponseCode.length() != 4) {
                tExceptionResponseCode = ResponseCodeSCH.ERROR_PARSE_MESSAGE.getResponseCode();
                SystemLog.getSingleton().log(this, LogType.ERROR, "jsonexception generating stack : " + pEx);
            } else if (isJSONValid) {
                JSONObject tAdditionalMessage = new JSONObject(pEx.getMessage());
                tExceptionResponseCode = tAdditionalMessage.getString("RC");
            }

            JSONObject tResponseCode = selectResponseCode(tExceptionResponseCode, tExceptionResponseMsg);

            tErrorJsonMessage.put("RC", tResponseCode);
        } catch (JSONException ex) {
            SystemLog.getSingleton().log(this, LogType.ERROR, "Cannot Construct Error Message on constructErrorJsonObject() : " + ex.getMessage());
        }

        return tErrorJsonMessage;
    }
}
