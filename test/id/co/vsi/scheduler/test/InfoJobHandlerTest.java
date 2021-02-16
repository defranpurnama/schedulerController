/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.vsi.scheduler.test;

import id.co.recis.common.test.ConUtilTester;
import id.co.vsi.systemcore.jasoncore.JSONMessage;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONObject;

/**
 *
 * @author User
 */
public class InfoJobHandlerTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ParseException {
        infoJob();
    }

    private static void infoJob() throws IOException {
        final String tIp = "192.168.26.62";
        final int tPort = 51151;

        JSONMessage tRequest = buildUpstreamRequest();

        final String tStringResponse = new ConUtilTester().testQuery(tRequest.toString(), tIp, tPort);

        System.out.println(new JSONMessage(tStringResponse).toString());
    }
    
    private static JSONMessage buildUpstreamRequest() {
        JSONObject tRequest = new JSONObject();
        tRequest.put("MT", "2100");
        tRequest.put("MC", "90003");
        tRequest.put("DT", new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()));
        
        return new JSONMessage(tRequest.toString());
    }
}
