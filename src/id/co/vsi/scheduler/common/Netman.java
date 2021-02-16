package id.co.vsi.scheduler.common;

import id.co.vsi.common.log.LogType;
import id.co.vsi.common.log.SystemLog;
import static id.co.vsi.scheduler.common.Common.cConfigHelper;
import static id.co.vsi.scheduler.common.Common.cJobGroup;
import static id.co.vsi.scheduler.jobs.JobTrigger.cJobIdentity;
import static id.co.vsi.scheduler.jobs.JobTrigger.cTriggerIdentity;
import id.co.vsi.scheduler.jobs.JobTriggerNetman;
import static id.co.vsi.scheduler.plugin.SchedulerPlugin.cScheduler;
import org.json.JSONObject;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public class Netman {
    
    public void initSchedulerNetman(){
        JSONObject tMPI = new JSONObject();
        tMPI.put("ID", "901");
        tMPI.put("NAME", "PSQL NETMAN");

        try {
            JobKey jobKey = JobKey.jobKey(tMPI.getString("ID"), cConfigHelper.getStringParameter(cJobGroup, "ADS"));

            if (cScheduler.checkExists(jobKey)) {
                SystemLog.getSingleton().log(this, LogType.ERROR, "[Schedule Already Running] for ID:" + tMPI.getString("ID"));
            }

            /*Create a new Job*/
            JobDetail job = JobBuilder.newJob(JobTriggerNetman.class)
                    .withIdentity(jobKey)
                    .withDescription(cJobIdentity + tMPI.getString("NAME"))
                    .storeDurably()
                    .build();

            String cronExpression = "0/10 0/1 * 1/1 * ? *";
            Trigger newTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(tMPI.getString("ID"), cConfigHelper.getStringParameter(cJobGroup, "ADS"))
                    .withDescription(cTriggerIdentity + tMPI.getString("NAME"))
                    .withSchedule(cronSchedule(cronExpression))
                    .build();

            /*Register this job to the scheduler*/
            cScheduler.scheduleJob(job, newTrigger);

            SystemLog.getSingleton().log(this, LogType.TRACE, "[Initialize Schedule] ID:" + tMPI.getString("ID") + ", NAME:[" + tMPI.getString("NAME") + "], " + "CRON EXPRESSION:[" + cronExpression + "]");

        } catch (SchedulerException ex) {
            SystemLog.getSingleton().log(this, LogType.ERROR, ex.getMessage());
        }
    }        
}
