package id.co.vsi.scheduler.plugin;

import id.co.vsi.common.log.LogType;
import id.co.vsi.common.log.SystemLog;
import id.co.vsi.common.settings.SystemConfig;
import id.co.vsi.scheduler.common.Common;
import static id.co.vsi.scheduler.common.Common.cModuleNameSpace;
import static id.co.vsi.scheduler.common.Common.convertCronExpressionByMinutes;
import id.co.vsi.scheduler.common.Netman;
import id.co.vsi.scheduler.db.DbQuery;
import id.co.vsi.scheduler.handler.CreateJobHandler;
import id.co.vsi.scheduler.handler.InfoJobHandler;
import id.co.vsi.scheduler.handler.UpdateDeleteJobHandler;
import id.co.vsi.scheduler.jobs.JobTrigger;
import id.co.vsi.systemcore.jasoncore.JSONMessage;
import id.co.vsi.systemcore.jasoncore.JSONPlugin;
import id.co.vsi.systemcore.jasoncore.JSONPluginHandler;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import static id.co.vsi.scheduler.common.Common.convertCronExpressionByMinuteHour;

/**
 *
 * $Rev:: $:Revision of last commit $Author:: $:Author of last commit $Date::
 * $:Date of last commit
 *
 */
public class SchedulerPlugin extends JSONPlugin {

    public static JSONArray cSchArray = new JSONArray();
    public static JSONObject cSchObj = new JSONObject();
    public static Scheduler cScheduler;

    public SchedulerPlugin() {
        registerHandler("Sys_UpdateDeleteJobHandler", new UpdateDeleteJobHandler(this));
        registerHandler("Sys_CreateJobHandler", new CreateJobHandler(this));
        registerHandler("Sys_InfoJobHandler", new InfoJobHandler(this));
    }

    @Override
    public Object clone() {
        SchedulerPlugin t_Plugin = new SchedulerPlugin();

        t_Plugin.setThreadID(this.getThreadID());

        return t_Plugin;
    }

    @Override
    public HashMap<String, HashMap<String, String>> performSelfTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPluginId() {
        return cModuleNameSpace;
    }

    @Override
    public Object execute(Object p_Object) {
        JSONMessage tRequestMessage = (JSONMessage) p_Object;

        SystemLog.getSingleton().log(this, LogType.STREAM, "DOWNLINE REQUEST : from " + getRequestAddress() + " " + tRequestMessage.toString());

        for (JSONPluginHandler tHandler : mHandlerHashMap.values()) {
            if (tHandler.canHandleMessage(tRequestMessage)) {
                tHandler.setRequestingAddress(getRequestAddress());

                JSONMessage tResponseMessage = tHandler.handleMessage(tRequestMessage);

                SystemLog.getSingleton().log(this, LogType.STREAM, "DOWNLINE RESPONSE : to   " + getRequestAddress() + " " + tResponseMessage.getMessageStream());

                String tMessageResponseCode = String.valueOf(tResponseMessage.getString(mThreadID));

                SystemConfig.getNameSpace(cModuleNameSpace).incrementParameter(tMessageResponseCode);

                return tResponseMessage;
            }
        }

        SystemLog.getSingleton().log(this, LogType.ERROR, "No handler for message : " + p_Object + ", returning original request message.");
        SystemLog.getSingleton().log(this, LogType.STREAM, "DOWNLINE RESPONSE : to   " + getRequestAddress() + " " + tRequestMessage.getMessageStream());

        return tRequestMessage;
    }

    @Override
    public void initialize() {
        try {
            cSchArray = new DbQuery().getDataScheduler();

            /*Create Schedule*/
            cScheduler = new StdSchedulerFactory().getScheduler();
            cScheduler.start();

            /*Add Job & Trigger*/
            for (int i = 0; i < cSchArray.length(); i++) {
                cSchObj = cSchArray.getJSONObject(i);

                if (cSchObj.getString("status").equalsIgnoreCase("1")) {
                    String cronExpression = "";
                    final JSONObject tMsgJson = new JSONObject(cSchObj.getString("msg_json"));
                    final String tIpPort = cSchObj.getString("ip") + ":" + cSchObj.getString("port");
                    if (cSchObj.getString("type").equals("1")) {
                        cronExpression = convertCronExpressionByMinuteHour(cSchObj.getString("start_time"));
                        SystemLog.getSingleton().log(this, LogType.TRACE, "[Initialize Schedule from DB] ID:" + cSchObj.getString("id") + ", NAME:[" + cSchObj.getString("name") + "], FIRE AT:[" + cSchObj.getString("start_time") + "], CRON EXPRESSION:[" + cronExpression + "]");
                    } else if (cSchObj.getString("type").equals("2")) {
                        cronExpression = convertCronExpressionByMinutes(cSchObj.getString("every_time"));
                        SystemLog.getSingleton().log(this, LogType.TRACE, "[Initialize Schedule from DB] ID:" + cSchObj.getString("id") + ", NAME:[" + cSchObj.getString("name") + "], FIRE EVERY:[" + cSchObj.getString("every_time") + "], CRON EXPRESSION:[" + cronExpression + "]");
                    }

                    cScheduler.scheduleJob(new JobTrigger().jobDetail(tMsgJson, tIpPort), new JobTrigger().trigger(cronExpression, tMsgJson, tIpPort));
                }
            }

            /*Create PSQL NETMAN*/
            boolean tUseNetmanDB = Common.cConfigHelper.getBooleanParameter(Common.cUseNetmanDB, false);
            SystemLog.getSingleton().log(this, LogType.TRACE, "[Use Netman DB] :" + tUseNetmanDB);

            if (tUseNetmanDB) {
                DbQuery.cUseLog = Common.cConfigHelper.getBooleanParameter(Common.cUseNetmanDBLog, false);
                SystemLog.getSingleton().log(this, LogType.TRACE, "[Use Netman DB Log] :" + DbQuery.cUseLog);

                new Netman().initSchedulerNetman();
            }
        } catch (JSONException ex) {
            SystemLog.getSingleton().log(this, LogType.ERROR, "[JSONException] Cannot initialize for reason " + ex);
        } catch (SchedulerException ex) {
            SystemLog.getSingleton().log(this, LogType.ERROR, "[SchedulerException] Cannot initialize for reason " + ex);
        }
    }

    @Override
    public String getModuleDescription() {
        return "SCHEDULER CONTROLLER 1.0.2";
    }

    @Override
    public boolean canHandleMessage(JSONMessage pMessage) {
        return true;
    }

    public static String getPluginLogFolder() {
        return "scheduler-controller";
    }
}
