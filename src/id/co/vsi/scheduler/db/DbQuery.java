package id.co.vsi.scheduler.db;

import id.co.vsi.common.log.LogType;
import id.co.vsi.common.log.SystemLog;
import static id.co.vsi.scheduler.common.Common.cModuleNameSpace;
import id.co.vsi.scheduler.enums.DbQueryEnum;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * $Rev:: 752 $:Revision of last commit $Author:: didik $:Author of last commit
 * $Date:: 2013-10-11 19:27:26 +0700 (Fri, 11 Oct 2013) $:Date of last commit
 *
 */
public class DbQuery {

    public static boolean cUseLog;

    public JSONArray getDataScheduler() {
        final String tQuery_AccountData = DbQueryEnum.GET_SCHEDULE.getDBQuery();
        final JSONArray tDataResult = new DbConUtil().executeQuery(tQuery_AccountData, cModuleNameSpace, true);

        return tDataResult;
    }

    public void getDateNow() {
        final String tQuery_Now = DbQueryEnum.GET_NOW.getDBQuery();
        new DbConUtil().executeQuery(tQuery_Now, cModuleNameSpace, cUseLog);
    }

    public void insertJob(final JSONObject pRequest) throws JSONException {
        JSONObject tMPI = pRequest.getJSONObject("MPI");
        
        try {
            JSONObject tMsgJson = new JSONObject();
            tMsgJson.put("SID", tMPI.getString("ID"));
            tMsgJson.put("DT", pRequest.getString("DT"));
            tMsgJson.put("MC", tMPI.getString("MC_DEST"));
            tMsgJson.put("MT", pRequest.getString("MT"));
            
            String tStartTime = "";
            String tEveryTime = "";
            
            String tDescription = "";
            if (tMPI.getString("TYPE").equals("1")) {
                tDescription = "Every day at " + tMPI.getString("START_TIME");
                tStartTime = tMPI.getString("START_TIME");
            } else if (tMPI.getString("TYPE").equals("2")) {
                tDescription = "Every " + tMPI.getString("EVERY_TIME") + " minutes";
                tEveryTime = tMPI.getString("EVERY_TIME");
            }
            
            String tCreatedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            
            final String tQuery = DbQueryEnum.INSERT_JOB.getDBQuery()
                    .replace("<SCHEDULER_ID>", tMPI.getString("ID"))
                    .replace("<SCHEDULER_NAME>", tMPI.getString("NAME"))
                    .replace("<SCHEDULER_START_TIME>", tStartTime)
                    .replace("<SCHEDULER_EVERY_TIME>", tEveryTime)
                    .replace("<SCHEDULER_DESCRIPTION>", tDescription)
                    .replace("<SCHEDULER_STATUS>", "1")
                    .replace("<SCHEDULER_TYPE>", tMPI.getString("TYPE"))
                    .replace("<SCHEDULER_MESSAGE_JSON>", tMsgJson.toString())
                    .replace("<SCHEDULER_ADDRESS_IP>", tMPI.getString("IP"))
                    .replace("<SCHEDULER_ADDRESS_PORT>", tMPI.getString("PORT"))
                    .replace("<SCHEDULER_CREATED_AT>", tCreatedDate);
            
            new DbConUtil().executeQuery(tQuery, cModuleNameSpace, true);
        } catch (JSONException ex) {
            SystemLog.getSingleton().log(this, LogType.DB_ERROR, "[ERROR INSERT QUERY], JSONException on insertJob() : " + ex);
        }
    }

    public void updateJob(final JSONObject pRequest) throws JSONException {
        JSONObject tMPI = pRequest.getJSONObject("MPI");
        
        try {
            JSONObject tMsgJson = new JSONObject();
            tMsgJson.put("SID", tMPI.getString("ID"));
            tMsgJson.put("DT", pRequest.getString("DT"));
            tMsgJson.put("MC", tMPI.getString("MC_DEST"));
            tMsgJson.put("MT", pRequest.getString("MT"));
            
            String tStartTime = "";
            String tEveryTime = "";
            
            String tDescription = "";
            if (tMPI.getString("TYPE").equals("1")) {
                tDescription = "Every day at " + tMPI.getString("START_TIME");
                tStartTime = tMPI.getString("START_TIME");
            } else if (tMPI.getString("TYPE").equals("2")) {
                tDescription = "Every " + tMPI.getString("EVERY_TIME") + " minutes";
                tEveryTime = tMPI.getString("EVERY_TIME");
            }
            
            String tUpdatedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            
            final String tQuery = DbQueryEnum.UPDATE_JOB.getDBQuery()
                    .replace("<SCHEDULER_ID>", tMPI.getString("ID"))
                    .replace("<SCHEDULER_NAME>", tMPI.getString("NAME"))
                    .replace("<SCHEDULER_START_TIME>", tStartTime)
                    .replace("<SCHEDULER_EVERY_TIME>", tEveryTime)
                    .replace("<SCHEDULER_DESCRIPTION>", tDescription)
                    .replace("<SCHEDULER_STATUS>", "1")
                    .replace("<SCHEDULER_TYPE>", tMPI.getString("TYPE"))
                    .replace("<SCHEDULER_MESSAGE_JSON>", tMsgJson.toString())
                    .replace("<SCHEDULER_ADDRESS_IP>", tMPI.getString("IP"))
                    .replace("<SCHEDULER_ADDRESS_PORT>", tMPI.getString("PORT"))
                    .replace("<SCHEDULER_UPDATED_AT>", tUpdatedDate);
            
            new DbConUtil().executeQuery(tQuery, cModuleNameSpace, true);
        } catch (JSONException ex) {
            SystemLog.getSingleton().log(this, LogType.DB_ERROR, "[ERROR UPDATE QUERY], JSONException on updateJob() : " + ex);
        }
    }
    
    public void deleteJob(final JSONObject pRequest) throws JSONException {
        JSONObject tMPI = pRequest.getJSONObject("MPI");
        
        try {
            final String tQuery = DbQueryEnum.DELETE_JOB.getDBQuery()
                    .replace("<SCHEDULER_ID>", tMPI.getString("ID"));
            
            new DbConUtil().executeQuery(tQuery, cModuleNameSpace, true);
        } catch (JSONException ex) {
            SystemLog.getSingleton().log(this, LogType.DB_ERROR, "[ERROR DELETE QUERY], JSONException on deleteJob() : " + ex);
        }
    }
}
