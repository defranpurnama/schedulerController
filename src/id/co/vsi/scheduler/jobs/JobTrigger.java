package id.co.vsi.scheduler.jobs;

import id.co.vsi.common.log.LogType;
import id.co.vsi.common.log.SystemLog;
import static id.co.vsi.scheduler.common.Common.cConfigHelper;
import static id.co.vsi.scheduler.common.Common.cJobGroup;
import static id.co.vsi.scheduler.common.Common.cMessagingRequest;
import id.co.vsi.scheduler.common.SyncronizedStan;
import id.co.vsi.scheduler.conn.ConUtil;
import static id.co.vsi.scheduler.plugin.SchedulerPlugin.cSchObj;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 *
 * $Rev:: $:Revision of last commit $Author:: $:Author of last commit $Date::
 * $:Date of last commit
 *
 */
public class JobTrigger implements Job {

    public static final String cJobIdentity = "Job ";
    public static final String cTriggerIdentity = "Trigger ";

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        JSONObject tMessagingReqObj = null;

        try {
            String tJobDesc = getJobDetail(jec.getJobDetail()).get("jobDescription").toString();

            tMessagingReqObj = new JSONObject();
            tMessagingReqObj.put("SID", getJobDetail(jec.getJobDetail()).get("jobKeyName").toString());
            tMessagingReqObj.put("MT", "2100");
            tMessagingReqObj.put("ST", SyncronizedStan.getInstance().getStan());
            tMessagingReqObj.put("DT", new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()));
            tMessagingReqObj.put("MC", getMCDest(tJobDesc));
            tMessagingReqObj.put("IP", getIpPort(tJobDesc).split(":")[0]);
            tMessagingReqObj.put("PORT", getIpPort(tJobDesc).split(":")[1]);

            new ConUtil().sendJsonToService(cConfigHelper, tMessagingReqObj);
        } catch (JSONException ex) {
            SystemLog.getSingleton().log(this, LogType.ERROR, ex.getMessage());
        }
    }
    
    private String getMCDest(String pJobDesc) {
        String[] tArrayTemp = pJobDesc.split(" ");
        String tResult = tArrayTemp[tArrayTemp.length - 1];
        return tResult;
    }
    
    private String getIpPort(String pJobDesc) {
        String[] tArrayTemp = pJobDesc.split(" ");
        String tIpPort = "";
        String tRegexIpPort = "([0-9]{1,3})([\\.]{1})([0-9]{1,3})([\\.]{1})([0-9]{1,3})([\\.]{1})([0-9]{1,3})([\\:]{1})([0-9]{1,})";
        for (String tArrayIndexValue : tArrayTemp) {
            if (tArrayIndexValue.matches(tRegexIpPort)) {
                return tArrayIndexValue;
            }
        }
        return tIpPort;
    }

    public static Map getJobDetail(JobDetail jobDetail) {
        Map<String, Object> tResult = new LinkedHashMap<String, Object>();
        tResult.put("jobDescription", jobDetail.getDescription());

        JobKey jobKey = jobDetail.getKey();
        tResult.put("jobKeyGroup", jobKey.getGroup());
        tResult.put("jobKeyName", jobKey.getName());

        jobDetail.getJobClass();
        tResult.put("jobClass", jobDetail.getJobClass());
        tResult.put("concurrentExectionDisallowed",
                jobDetail.isConcurrentExectionDisallowed());
        tResult.put("persistJobDataAfterExecution",
                jobDetail.isPersistJobDataAfterExecution());
        tResult.put("isDurable", jobDetail.isDurable());
        tResult.put("requestsRecovery", jobDetail.requestsRecovery());

        return tResult;
    }

    public JobDetail jobDetail(final JSONObject pMsgJson, final String pIpPort) throws JSONException {
        JobDetail jJob = JobBuilder
                .newJob(JobTrigger.class)
                .withIdentity(cSchObj.getString("id"), cConfigHelper.getStringParameter(cJobGroup, "ADS"))
                .withDescription(cTriggerIdentity + cSchObj.getString("name") + " " + pIpPort + " " + pMsgJson.getString("MC"))
                .build();

        return jJob;
    }

    public Trigger trigger(final String pCronExpression, final JSONObject pMsgJson, final String pIpPort) throws JSONException {
        Trigger tTrigger = TriggerBuilder
                .newTrigger()
                .withIdentity(cSchObj.getString("id"), cConfigHelper.getStringParameter(cJobGroup, "ADS"))
                .withDescription(cTriggerIdentity + cSchObj.getString("name") + " " + pIpPort + " " + pMsgJson.getString("MC"))
                .withSchedule(cronSchedule(pCronExpression))
                .build();

        return tTrigger;
    }

}
