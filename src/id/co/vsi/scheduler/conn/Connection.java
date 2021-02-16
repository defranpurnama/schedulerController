package id.co.vsi.scheduler.conn;

import id.co.vsi.common.constants.ResponseCode;
import id.co.vsi.common.crypto.SymetricCryptoHandler;
import id.co.vsi.common.log.LogType;
import id.co.vsi.common.log.SystemLog;
import id.co.vsi.common.settings.SystemConfig.ConfigHelper;
import id.co.vsi.systemcore.isocore.SystemException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author  $Author:: didik                                                     $: Author of last revision
 * @version $Revision:: 669                                                     $: Last revision number
 * @since   $LastChangedDate:: 2013-09-12 16:37:21 +0700 (Thu, 12 Sep 2013)     $: Date of last revision
 */
public class Connection {
    public static final String  cRevisionNumber         = "$Revision: 669 $";

    private static final int    cTimeOutDV          = 20000;
    private static final String cAESEncryptionKey   = "encryption-key";
    private static final String cAESEncryptionType  = "encryption-type";
    private static final byte   cEndMessageByte     = -0x01;
    private static Connection    INSTANCE;

    protected String cTimeOutKey = "timeout";

    public static Connection getINSTANCE() {
        INSTANCE = new Connection();

        return INSTANCE;
    }

    /**
     * Build general param for sending message conatains parameter configuration and encryption setting
     * @param pConfigHelper
     * @param pRequestMessageStream
     * @return
     * @Param pConfigHelper
     * @Param pRequestMessageStream
     * @Return Map<String, Object>
     */
    protected Map<String, Object> buildSendMessageParams(final ConfigHelper pConfigHelper, final String pRequestMessageStream) {
        final Map<String, Object> tParams            = new HashMap<String, Object>();
        final String              tAESEncryptionType = pConfigHelper.getStringParameter(cAESEncryptionType);

        if (tAESEncryptionType != null) {
            final String[] tAESConfig = tAESEncryptionType.split("/");

            tParams.put("AESType", pConfigHelper.getStringParameter(tAESConfig[0], "AES"));
            tParams.put("AESMode", pConfigHelper.getStringParameter(tAESConfig[1], "ECB"));
            tParams.put("AESPadding", pConfigHelper.getStringParameter(tAESConfig[2], "PKCS5Padding"));
            tParams.put("AESKey", pConfigHelper.getStringParameter(cAESEncryptionKey));
        }

        tParams.put("requestMessage", pRequestMessageStream);
        tParams.put("timeout", pConfigHelper.getIntParameter(cTimeOutKey, cTimeOutDV));

        return tParams;
    }

    /**
     * Sending message to socket with define in paramaters
     * @param pParams
     * @return
     * @Param Map<String, Object> pParams
     * @Return String
     */
    protected String sendMessage(final Map<String, Object> pParams) {
        final InetSocketAddress pGatewaySocketAddress = (InetSocketAddress) pParams.get("initAddress");
        final String            tAESType              = (String) pParams.get("AESType");
        final String            tAESMode              = (String) pParams.get("AESMode");
        final String            tAESPadding           = (String) pParams.get("AESPadding");
        final String            tAESKey               = (String) pParams.get("AESKey");
        final Boolean           isUsesAES             = (Boolean) pParams.get("isUseAES");
        final Integer           tTimeout              = (Integer) pParams.get("timeout");
        final Socket            tGatewaySocket        = new Socket();
        String                  tRequestStream        = (String) pParams.get("requestMessage");
        SymetricCryptoHandler   tCryptoHandler        = null;

        if (isUsesAES) {
            try {
                tCryptoHandler = new SymetricCryptoHandler(tAESType, tAESMode, tAESPadding, tAESKey, null);
            } catch (InvalidAlgorithmParameterException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (NoSuchAlgorithmException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (NoSuchPaddingException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (InvalidKeyException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (IllegalBlockSizeException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (BadPaddingException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            }
        }

        try {
            tGatewaySocket.setSoTimeout(tTimeout);
            tGatewaySocket.connect(pGatewaySocketAddress, tTimeout);
        } catch (IOException ex) {
            try {
                SystemLog.getSingleton().log(this, LogType.ERROR, ex.getMessage() + " [IOException on SendMessage() when connecting to Gateway at " + pGatewaySocketAddress + "] : Construct RC 0068");

                tGatewaySocket.close();

                String tResponseStream = "";
                JSONObject tReturnValue = new JSONObject(tRequestStream);

                /*Given 0068 cause Connection Refused*/
                tReturnValue.put("RC", "0068");
                tReturnValue.put("RCD", ex.getMessage() + " [IOException on SendMessage() when connecting to Gateway at " + pGatewaySocketAddress + "] : Construct RC 0068");

                tResponseStream = tReturnValue.toString();

                return tResponseStream;
            } catch (IOException e) {
            } catch (JSONException e) {
            }
        }

        ByteArrayOutputStream tRequestByteStream = new ByteArrayOutputStream();

        if (isUsesAES) {
            try {
                SystemLog.getSingleton().log(this, LogType.TRACE, "before encrypt: " + tRequestStream);

                tRequestStream = tCryptoHandler.getCryptoMessage(SymetricCryptoHandler.ACTION_ENCRYPT, tRequestStream);

                SystemLog.getSingleton().log(this, LogType.TRACE, "after encrypt: " + tRequestStream);

            } catch (InvalidAlgorithmParameterException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (NoSuchAlgorithmException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (NoSuchPaddingException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (InvalidKeyException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (IllegalBlockSizeException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (BadPaddingException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            }
        }

        try {
            tRequestByteStream.write(tRequestStream.getBytes());
            tRequestByteStream.write(cEndMessageByte);
        } catch (IOException ex) {

            try {
                tGatewaySocket.close();
            } catch (IOException e) {}

            throw new SystemException(ResponseCode.ERROR_OTHER,
                                      "IOException on SendMessage() when writing request stream " + tRequestStream + " to byte array output stream.",
                                      ex, this);
        }

        try {
            tGatewaySocket.getOutputStream().write(tRequestByteStream.toByteArray());
        } catch (IOException ex) {

            try {
                tGatewaySocket.close();
            } catch (IOException e) {}

            throw new SystemException(ResponseCode.ERROR_TIMEOUT,
                                      "IOException on SendMessage() when writing stream " + tRequestStream + " + to outgoing socket at " +
                                      pGatewaySocketAddress, ex, this);
        }

        String       tResponseStream = "";
        byte         tMessageByte    = cEndMessageByte;
        StringBuilder sb              = new StringBuilder();

        try {
            while ((tMessageByte = (byte) tGatewaySocket.getInputStream().read()) != cEndMessageByte) {
                sb.append((char) tMessageByte);
            }

            tResponseStream = sb.toString();
        } catch (IOException ex) {

            try {
                tGatewaySocket.close();
            } catch (IOException e) {}

            throw new SystemException(ResponseCode.ERROR_TIMEOUT,
                                      "IOException on SendMessage() when reading response stream at incoming socket from " + pGatewaySocketAddress +
                                      ". Read message so far : [" + tResponseStream + "]", ex, this);
        }

        if (isUsesAES) {
            try {
                SystemLog.getSingleton().log(this, LogType.TRACE, "before encrypt: " + tResponseStream);

                tResponseStream = tCryptoHandler.getCryptoMessage(SymetricCryptoHandler.ACTION_DECRYPT, tResponseStream);

                SystemLog.getSingleton().log(this, LogType.TRACE, "after encrypt: " + tResponseStream);
            } catch (InvalidAlgorithmParameterException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (NoSuchAlgorithmException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (NoSuchPaddingException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (InvalidKeyException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (IllegalBlockSizeException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            } catch (BadPaddingException ex) {
                throw new SystemException(ResponseCode.ERROR_OTHER, "Error when encrypting message " + tRequestStream + " with key " + tAESKey, ex,
                                          this);
            }
        }

        try {
            tGatewaySocket.close();
        } catch (IOException ex) {
            SystemLog.getSingleton().log(this, LogType.WARNING, "Warning : Cannot close socket " + tGatewaySocket, ex);
        }

        if (tResponseStream.isEmpty()) {
            try {
                SystemLog.getSingleton().log(this, LogType.ERROR, "Empty response received from gateway when sending request message [" + tRequestStream + "] : Construct RC 0068");
                JSONObject tReturnValue = new JSONObject(tRequestStream);

                /*Given 0068 cause Empty Response*/
                tReturnValue.put("RC", "0068");
                tReturnValue.put("RCD", "Empty Response Received From Gateway When Sending Request Message [" + tRequestStream + "] : Construct RC 0068");
                tResponseStream = tReturnValue.toString();
            } catch (Exception ex) {
            }
        }

        return tResponseStream;
    }
}
