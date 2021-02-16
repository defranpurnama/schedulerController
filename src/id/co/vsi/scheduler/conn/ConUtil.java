package id.co.vsi.scheduler.conn;

import id.co.vsi.common.constants.ResponseCode;
import id.co.vsi.common.log.LogType;
import id.co.vsi.common.log.SystemLog;
import id.co.vsi.common.settings.SystemConfig.ConfigHelper;
import id.co.vsi.systemcore.isocore.SystemException;
import java.net.InetSocketAddress;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class ConUtil extends Connection{

    /*Conf xml*/
    private final static String cServiceIPKey            = "service-ip";
    private final static String cServicePortEncryptKey   = "service-port-Encrypt";
    private final static String cServicePortKey          = "service-port";
    private final static String cServiceUseAes           = "service-use-aes";

    public void sendJsonToService(final ConfigHelper pConfigHelper,
            final JSONObject pRequestMessage) throws JSONException {
        final Map<String, Object> tSendMessageParams = buildSendMessageParams(pConfigHelper, pRequestMessage.toString());
        final Map<String, Object> pParams            = initAddressService(pConfigHelper, tSendMessageParams, pRequestMessage);
        JSONObject               tReturnValue;
        String                   tResponseStream = "";

        SystemLog.getSingleton().log(this, LogType.useTemporaryType("scheduler-running"),
                                     "REQUEST   : to   " + pParams.get("initAddress") + " " + pRequestMessage.toString());

        try {
            tResponseStream = sendMessage(pParams);
            tReturnValue    = new JSONObject(tResponseStream);
        } catch (JSONException ex) {
            throw new SystemException(ResponseCode.ERROR_INVALID_MESSAGE,
                                      "ParseException on SendMessage() when trying to parse response stream " + tResponseStream +
                                      " into JSON Message.", ex, this);
        }

        SystemLog.getSingleton().log(this, LogType.useTemporaryType("scheduler-running"),
                                     "RESPONSE  : from " + pParams.get("initAddress") + " " + tReturnValue.toString());
    }

    private Map<String, Object> initAddressService(final ConfigHelper pConfigHelper,
            final Map<String, Object> pParams, JSONObject pRequestMessage) {
        final Map<String, Object> tParams         = pParams;
//        final String              tGatewayAddress = pConfigHelper.getStringParameter(cServiceIPKey, "127.0.0.1");
//        final int                 tGatewayPort    = pConfigHelper.getBooleanParameter(cServiceUseAes, false)
//                                                    ? pConfigHelper.getIntParameter(cServicePortEncryptKey)
//                                                    : pConfigHelper.getIntParameter(cServicePortKey);
        final String              tGatewayAddress = pRequestMessage.getString("IP");
        final int                 tGatewayPort    = Integer.parseInt(pRequestMessage.getString("PORT"));
        InetSocketAddress tGatewaySocketAddress = new InetSocketAddress(tGatewayAddress, tGatewayPort);

        tParams.put("initAddress", tGatewaySocketAddress);
        tParams.put("isUseAES", false);
//        tParams.put("isUseAES", pConfigHelper.getBooleanParameter(cServiceUseAes, false));

        return tParams;
    }
}