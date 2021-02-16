/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.vsi.scheduler.test;

import id.co.recis.common.test.ConUtilTester;
import id.co.vsi.systemcore.jasoncore.JSONMessage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.json.JSONObject;

/**
 *
 * @author User
 */
public class CreateJobHandlerTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        createJob();
    }

    private static void createJob() throws IOException {
        final String tIp = "192.168.26.62";
        final int tPort = 51151;

        //TYPE 1: Every day at HH:mm
//        JSONMessage tRequest = buildUpstreamRequestTypeCreate1Test1();

        //TYPE 2: Every m Minutes
        JSONMessage tRequest = buildUpstreamRequestTypeCreate1Test2();

        final String tStringResponse = new ConUtilTester().testQuery(tRequest.toString(), tIp, tPort);

        System.out.println(new JSONMessage(tStringResponse).toString());
    }
    
    private static JSONMessage buildUpstreamRequestTypeCreate1Test1() {
        JSONObject tRequest = new JSONObject();
        tRequest.put("MT", "2100");
        tRequest.put("MC", "90001");
        tRequest.put("DT", new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()));
        
        JSONObject tMPI = new JSONObject();
        tMPI.put("ID", "300");
        tMPI.put("NAME", "BE CREATE1 TEST1");
        tMPI.put("TYPE", "1");
        tMPI.put("START_TIME", "14:31");
        tMPI.put("STATUS", "1");
        tMPI.put("MC_DEST", "950151");
        tMPI.put("IP", "192.168.26.62");
        tMPI.put("PORT", "51152");
        
        tRequest.put("MPI", tMPI);
        
        return new JSONMessage(tRequest.toString());
    }
    
    private static JSONMessage buildUpstreamRequestTypeCreate1Test2() {
        JSONObject tRequest = new JSONObject();
        tRequest.put("MT", "2100");
        tRequest.put("MC", "90001");
        tRequest.put("DT", new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()));
        
        JSONObject tMPI = new JSONObject();
        tMPI.put("ID", "301");
        tMPI.put("NAME", "BE CREATE1 TEST2");
        tMPI.put("TYPE", "2");
        tMPI.put("EVERY_TIME", "2");
        tMPI.put("STATUS", "1");
        tMPI.put("MC_DEST", "950151");
        tMPI.put("IP", "192.168.26.62");
        tMPI.put("PORT", "51152");
        
        tRequest.put("MPI", tMPI);
        
        return new JSONMessage(tRequest.toString());
    }
}
