package id.co.vsi.scheduler.handler;

import id.co.vsi.common.log.LogType;
import id.co.vsi.common.log.SystemLog;
import id.co.vsi.scheduler.common.Common;
import id.co.vsi.scheduler.enums.ResponseCodeSCH;
import static id.co.vsi.scheduler.plugin.SchedulerPlugin.cScheduler;
import id.co.vsi.systemcore.isocore.SystemException;
import id.co.vsi.systemcore.jasoncore.JSONMessage;
import id.co.vsi.systemcore.jasoncore.JSONPlugin;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

/**
 *
 * $Rev:: $:Revision of last commit $Author:: $:Author of last commit $Date::
 * $:Date of last commit
 *
 */
public class InfoJobHandler extends Common {

    public InfoJobHandler(JSONPlugin pParentPlugin) {
        super(pParentPlugin);
    }

    @Override
    public boolean canHandleMessage(JSONMessage pMessage) {
        return pMessage.getString("MC").equalsIgnoreCase("90003");
    }

    @Override
    public JSONMessage handleMessage(JSONMessage pRequestMessage) {
        JSONObject tResponseMessage = new JSONObject();

        try {
            JSONObject tRequest = new JSONObject(pRequestMessage.toString());

            tResponseMessage = performInfoScheduler(tRequest);
        } catch (JSONException e) {
            tResponseMessage = constructErrorJSONException(pRequestMessage, e);
        } catch (SystemException e) {
            tResponseMessage = constructErrorSystemException(pRequestMessage, e);
        }

        tResponseMessage.put("DT", new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()));
        tResponseMessage.put("MT", typeResponse(tResponseMessage));

        return new JSONMessage(tResponseMessage.toString());
    }

    private JSONObject performInfoScheduler(final JSONObject pRequest) {
        JSONObject tResponseMessage = new JSONObject(pRequest.toString());
        JSONObject tMPO = new JSONObject();

        try {
            for (String group : cScheduler.getJobGroupNames()) {
                for (JobKey jobKey : cScheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                    List<Trigger> tTriggers = (List<Trigger>) cScheduler.getTriggersOfJob(jobKey);

                    Date nextFireTime = tTriggers.get(0).getNextFireTime();
                    Date previousFireTime = tTriggers.get(0).getPreviousFireTime();
                    String tHourMinute = "";

                    String tType = cScheduler.getJobDetail(jobKey).getDescription();

                    if (tType.contains("TYPE_1")) {
                        tHourMinute = formatDateTime(String.valueOf(nextFireTime), "EEE MMM d HH:mm:ss z yyyy", "HH:mm");
                    } else if (tType.contains("TYPE_2")) {
                        //NOTES: Cant get previous fire time value so EVERY_TIME jsonvalue cant be told
//                        Date tType2NextFireTime = new Date(nextFireTime.getTime() - previousFireTime.getTime());
//                        tHourMinute = formatDateTime(String.valueOf(tType2NextFireTime), "EEE MMM d HH:mm:ss z yyyy", "HH:mm");
                    }

                    String tJobDesc = cScheduler.getJobDetail(jobKey).getDescription();

                    JSONObject tListMPO = new JSONObject();
                    if (jobKey.getName().equals("901")) {
                        //SID 901 reserved for netman job, build different MPO
                        tListMPO.put("ID", jobKey.getName());
                        tListMPO.put("NAME", cScheduler.getJobDetail(jobKey).getDescription());
                        tListMPO.put("START_TIME", tHourMinute);
                        tListMPO.put("NEXT_FIRE", nextFireTime);
                    } else {
                        //SID other than 901
                        tListMPO.put("ID", jobKey.getName());
                        tListMPO.put("NAME", tJobDesc.substring(0, tJobDesc.length() - (getMCDest(tJobDesc).length() + 1)));
                        tListMPO.put("MC_DEST", getMCDest(tJobDesc));
                        tListMPO.put("IP", getIpPort(tJobDesc).split(":")[0]);
                        tListMPO.put("PORT", getIpPort(tJobDesc).split(":")[1]);
                        if (tType.contains("TYPE_1")) {
                            //Every day at HH:mm
                            tListMPO.put("START_TIME", tHourMinute);
                        } else if (tType.contains("TYPE_2")) {
                            //Every m minutes
                            //NOTES: Cant get previous fire time value so EVERY_TIME jsonvalue cant be told
//                            Integer tTime = (Integer.valueOf(tHourMinute.split(":")[0]) * 60) + Integer.valueOf(tHourMinute.split(":")[1]);
//                            tListMPO.put("EVERY_TIME", tTime.toString());
                        }

                        tListMPO.put("NEXT_FIRE", nextFireTime);
                    }

                    tMPO.put(jobKey.getName(), tListMPO);
                }
            }

            tResponseMessage.put("MPO", tMPO);
            tResponseMessage.put("RC", selectResponseCode(ResponseCodeSCH.SUCCESS));
        } catch (SchedulerException ex) {
            SystemLog.getSingleton().log(this, LogType.ERROR, ex.getMessage());
            tResponseMessage.put("RC", selectResponseCode(ResponseCodeSCH.ERROR_SCHEDULER));
        }

        return tResponseMessage;
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
}
