package id.co.vsi.scheduler.handler;

import id.co.vsi.common.log.LogType;
import id.co.vsi.common.log.SystemLog;
import id.co.vsi.scheduler.common.Common;
import id.co.vsi.scheduler.db.DbQuery;
import id.co.vsi.scheduler.enums.ResponseCodeSCH;
import id.co.vsi.scheduler.jobs.JobTrigger;
import static id.co.vsi.scheduler.jobs.JobTrigger.cJobIdentity;
import static id.co.vsi.scheduler.jobs.JobTrigger.cTriggerIdentity;
import static id.co.vsi.scheduler.plugin.SchedulerPlugin.cScheduler;
import id.co.vsi.systemcore.isocore.SystemException;
import id.co.vsi.systemcore.jasoncore.JSONMessage;
import id.co.vsi.systemcore.jasoncore.JSONPlugin;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.json.JSONException;
import org.json.JSONObject;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import static id.co.vsi.scheduler.common.Common.convertCronExpressionByMinuteHour;


/**
 *
 * $Rev::                                                                       $:Revision of last commit
 * $Author::                                                                    $:Author of last commit
 * $Date::                                                                      $:Date of last commit
 *
 */

public class CreateJobHandler extends Common{

    public CreateJobHandler(JSONPlugin pParentPlugin) {
        super(pParentPlugin);
    }

    @Override
    public boolean canHandleMessage(JSONMessage pMessage) {
        return pMessage.getString("MC").equalsIgnoreCase("90001");
    }

    @Override
    public JSONMessage handleMessage(JSONMessage pRequestMessage) {
        JSONObject tResponseMessage = new JSONObject();

        try {
            JSONObject tRequest = new JSONObject(pRequestMessage.toString());

            tResponseMessage = performCreateScheduler(tRequest);
        } catch (JSONException e) {
            tResponseMessage = constructErrorJSONException(pRequestMessage, e);
        } catch (SystemException e){
            tResponseMessage = constructErrorSystemException(pRequestMessage, e);
        }

        tResponseMessage.put("DT", new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()));
        tResponseMessage.put("MT", typeResponse(tResponseMessage));

        return new JSONMessage(tResponseMessage.toString());
    }

    private JSONObject performCreateScheduler(final JSONObject pRequest){
        JSONObject tResponseMessage = new JSONObject(pRequest.toString());
        JSONObject tMPI = pRequest.getJSONObject("MPI");
        String tType = "TYPE_" + tMPI.getString("TYPE");
        String tIpPort = tMPI.getString("IP") + ":" + tMPI.getString("PORT");
        
        try {
            JobKey jobKey = JobKey.jobKey(tMPI.getString("ID"), cConfigHelper.getStringParameter(cJobGroup, "ADS"));

            if (cScheduler.checkExists(jobKey)) {
                SystemLog.getSingleton().log(this, LogType.ERROR, "[Schedule Already Running] for ID:" + tMPI.getString("ID"));
                return tResponseMessage.put("RC", selectResponseCode(ResponseCodeSCH.ERROR_ALREADY));
            }

            /*Create a new Job*/
            JobDetail job = JobBuilder.newJob(JobTrigger.class)
                    .withIdentity(jobKey)
                    .withDescription(cJobIdentity + tMPI.getString("NAME") + " " + tType + " " + tIpPort + " " + tMPI.getString("MC_DEST"))
                    .storeDurably()
                    .build();

            String cronExpression = "";
            if (tMPI.getString("TYPE").equals("1")) {
                cronExpression = convertCronExpressionByMinuteHour(tMPI.getString("START_TIME"));
            } else if (tMPI.getString("TYPE").equals("2")) {
                cronExpression = convertCronExpressionByMinutes(tMPI.getString("EVERY_TIME"));
            }
            
            Trigger newTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(tMPI.getString("ID"), cConfigHelper.getStringParameter(cJobGroup, "ADS"))
                    .withDescription(cJobIdentity + tMPI.getString("NAME") + " " + tType + " " + tIpPort + " " + tMPI.getString("MC_DEST"))
                    .withSchedule(cronSchedule(cronExpression))
                    .build();

            /*Register this job to the scheduler*/
            cScheduler.scheduleJob(job, newTrigger);
            new DbQuery().insertJob(pRequest);
            
            if (tMPI.getString("TYPE").equals("1")) {
                SystemLog.getSingleton().log(this, LogType.TRACE, "[Add Schedule] ID:" + tMPI.getString("ID") + ", NAME:[" + tMPI.getString("NAME") + "], FIRE AT:[" + tMPI.getString("START_TIME") + "], CRON EXPRESSION:[" + cronExpression + "]");
            } else if (tMPI.getString("TYPE").equals("2")) {
                SystemLog.getSingleton().log(this, LogType.TRACE, "[Add Schedule] ID:" + tMPI.getString("ID") + ", NAME:[" + tMPI.getString("NAME") + "], FIRE EVERY:[" + tMPI.getString("EVERY_TIME") + "], CRON EXPRESSION:[" + cronExpression + "]");
            }
            tResponseMessage.put("RC", selectResponseCode(ResponseCodeSCH.SUCCESS));
        } catch (SchedulerException ex) {
            SystemLog.getSingleton().log(this, LogType.ERROR, ex.getMessage());
            tResponseMessage.put("RC", selectResponseCode(ResponseCodeSCH.ERROR_SCHEDULER));
        }

        return tResponseMessage;
    }
}
