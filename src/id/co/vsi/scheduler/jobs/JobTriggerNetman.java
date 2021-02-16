package id.co.vsi.scheduler.jobs;

import static id.co.vsi.scheduler.common.Common.cConfigHelper;
import static id.co.vsi.scheduler.common.Common.cJobGroup;
import static id.co.vsi.scheduler.common.Common.cModuleNameSpace;
import id.co.vsi.scheduler.db.DbQuery;
import static id.co.vsi.scheduler.plugin.SchedulerPlugin.cSchObj;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;
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
 * $Rev::                                                                       $:Revision of last commit
 * $Author::                                                                    $:Author of last commit
 * $Date::                                                                      $:Date of last commit
 *
 */

public class JobTriggerNetman implements Job{

    public static final String cJobIdentity     = "Job ";
    public static final String cTriggerIdentity = "Trigger ";

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        new DbQuery().getDateNow();
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

    public JobDetail jobDetail() throws JSONException{
        JobDetail jJob = JobBuilder
                .newJob(JobTriggerNetman.class)
                .withIdentity(cSchObj.getString("id"), cConfigHelper.getStringParameter(cJobGroup, "ADS"))
                .withDescription(cJobIdentity + cSchObj.getString("name"))
                .build();

        return jJob;
    }

    public Trigger trigger(final String pCronExpression) throws JSONException{
        Trigger tTrigger = TriggerBuilder
                .newTrigger()
                .withIdentity(cSchObj.getString("id"), cConfigHelper.getStringParameter(cJobGroup, "ADS"))
                .withDescription(cTriggerIdentity + cSchObj.getString("name"))
                .withSchedule(cronSchedule(pCronExpression))
                .build();

        return tTrigger;
    }

}
