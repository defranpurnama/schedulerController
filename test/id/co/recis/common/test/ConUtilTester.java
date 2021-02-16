/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.recis.common.test;

import id.co.vsi.common.crypto.SymetricCryptoHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author User
 */
public class ConUtilTester {
    
    SymetricCryptoHandler tCryptoHandler;
    
     public String testQuery(final String pRequestMessage, final String pIP, final int pPort) throws SocketException, IOException {
        final int                    tTotalSend             = 1;
        final boolean                isUsePortEncrypt       = false;
        final String                 tIPGateway             = pIP;
        final int                    tPortEncrypt           = pPort;
        final int                    tPortNotEncrypt        = pPort;
        final BlockingQueue<Integer> tTotalRCSuccessed      = new ArrayBlockingQueue<Integer>(tTotalSend);
        final BlockingQueue<Integer> tTotalRCFailed         = new ArrayBlockingQueue<Integer>(tTotalSend);
        final BlockingQueue<Integer> tTotalFailedConnection = new ArrayBlockingQueue<Integer>(tTotalSend);
        final BlockingQueue<Long>    tTotalTimeConnection   = new ArrayBlockingQueue<Long>(tTotalSend);
        final String[]               tRequestStream         = new String[] { pRequestMessage };
        final String[]               tResponse              = new String[1];

        if (isUsePortEncrypt) {
            final long tStartEncrypt = System.currentTimeMillis();

            System.out.println("[debug] - REQUEST_STREAM = " + tRequestStream[0]);

            tRequestStream[0] = encryptAES(tRequestStream[0]);
        }

        final long tStart = System.currentTimeMillis();

        System.err.println("[debug] - tStart = " + tStart);

        for (int i = 0; i < tTotalSend; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InetSocketAddress tGatewaySocketAddress = new InetSocketAddress(tIPGateway, (isUsePortEncrypt ? tPortEncrypt : tPortNotEncrypt));
                    final Socket      tGatewaySocket        = new Socket();
                    final int         tTimeout              = 1000 * 100;
                    final byte        cEndMessageByte       = -0x01;

                    System.out.println("[debug] - REQUEST_STREAM  = " + tGatewaySocketAddress.getAddress() + ":" + tGatewaySocketAddress.getPort() +
                                       " " + tRequestStream[0]);

                    try {
                        tGatewaySocket.setSoTimeout(tTimeout);
                        tGatewaySocket.setKeepAlive(true);
                        tGatewaySocket.connect(tGatewaySocketAddress, tTimeout);

                        final long            tStartRequest      = System.currentTimeMillis();
                        ByteArrayOutputStream tRequestByteStream = new ByteArrayOutputStream();

                        tRequestByteStream.write(tRequestStream[0].getBytes());
                        tRequestByteStream.write(cEndMessageByte);
                        tGatewaySocket.getOutputStream().write(tRequestByteStream.toByteArray());

                        byte         tMessageByte = cEndMessageByte;
                        StringBuffer sb           = new StringBuffer();

                        while ((tMessageByte = (byte) tGatewaySocket.getInputStream().read()) != cEndMessageByte) {
                            sb.append((char) tMessageByte);
                        }

                        tTotalTimeConnection.add((System.currentTimeMillis() - tStartRequest));

                        // System.out.println("[debug] - Time stream request response = " + (System.currentTimeMillis() - tStartRequest));
                        final long tStartDecrypt = System.currentTimeMillis();

                        if (isUsePortEncrypt) {
                            System.out.println("[debug] - RESPONSE_STREAM = " + sb.toString());
                        }

                        tResponse[0] = isUsePortEncrypt ? decryptAES(sb.toString()) : sb.toString();

                        // System.out.println("[debug] - Elapse Decrypt = " + (System.currentTimeMillis() - tStartDecrypt));
                        System.out.println("[debug] - RESPONSE_STREAM = " + tGatewaySocketAddress.getAddress() + ":" +
                                           tGatewaySocketAddress.getPort() + " " + tResponse[0]);

//                        if (tResponse[0].contains("\"RC\":\"0000\"")) {
                            tTotalRCSuccessed.add(1);
//                        } else if (tResponse[0].contains("\"RC\":")) {
//                            tTotalRCFailed.add(1);
//                        }
                    } catch (IOException iOException) {
                        tTotalFailedConnection.add(1);
                    }
                }
            }).start();
        }

        while (true) {

            // final long tElapseTime = System.currentTimeMillis() - tStart;
            int z = tTotalRCFailed.size() + tTotalRCSuccessed.size() + tTotalFailedConnection.size();

            if (z == tTotalSend) {
                Long tElapseTime = 0L;

                for (Long _Long : tTotalTimeConnection) {

                    // System.out.println("[debug] - _Long = " + _Long);
                    tElapseTime += _Long;
                }

                // tElapseTime = tElapseTime / tTotalTimeConnection.size();
                // final long tDelta = tElapseTime / 1000;
                System.err.println("[debug] - SOCKET CONNECTION ENCRYPT/DECRYPT \t= " + isUsePortEncrypt);
                System.err.println("[debug] - TIME TAKEN FOR TESTS              \t= " + tElapseTime + " ms from " + z);

                // System.err.println("[debug] - TPS                               \t= " + (z / tDelta));
//                System.err.println("[debug] - RC FAILED                         \t= " + tTotalRCFailed.size());
                System.err.println("[debug] - RC SUCCESSED                      \t= " + tTotalRCSuccessed.size());
                System.err.println("[debug] - FAILED CONNECTION                 \t= " + tTotalFailedConnection.size());

                break;
            }
        }

        final long tEnd = System.currentTimeMillis();

        System.err.println("[debug] - ***** END TIME **** = " + (tEnd - tStart));

        return tResponse[0];
    }

    private String decryptAES(final String pMessage) {
        String tEncryptAES = null;

        try {
            tEncryptAES = tCryptoHandler.getCryptoMessage(SymetricCryptoHandler.ACTION_DECRYPT, pMessage);
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {}
        catch (NoSuchPaddingException noSuchPaddingException) {}
        catch (InvalidKeyException invalidKeyException) {}
        catch (IllegalBlockSizeException illegalBlockSizeException) {}
        catch (BadPaddingException badPaddingException) {}
        catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {}

        return tEncryptAES;
    }

    private String encryptAES(final String pMessage) {
        String tEncryptAES = null;

        try {
            tEncryptAES = tCryptoHandler.getCryptoMessage(SymetricCryptoHandler.ACTION_ENCRYPT, pMessage);
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {}
        catch (NoSuchPaddingException noSuchPaddingException) {}
        catch (InvalidKeyException invalidKeyException) {}
        catch (IllegalBlockSizeException illegalBlockSizeException) {}
        catch (BadPaddingException badPaddingException) {}
        catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {}

        return tEncryptAES;
    }
}
