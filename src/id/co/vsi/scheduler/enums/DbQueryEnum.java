package id.co.vsi.scheduler.enums;

import static id.co.vsi.scheduler.common.Common.cDbTableName;

/**
 *
 * $Rev:: 736 $:Revision of last commit $Author:: didik $:Author of last commit
 * $Date:: 2013-10-08 22:25:54 +0700 (Tue, 08 Oct 2013) $:Date of last commit
 *
 */
public enum DbQueryEnum {

    GET_SCHEDULE
            ("SELECT scheduler_id AS id, scheduler_name AS name, scheduler_start_time AS start_time, scheduler_every_time AS every_time, scheduler_status AS status, scheduler_type AS type, scheduler_message_json AS msg_json, scheduler_address_ip AS ip, scheduler_address_port AS port FROM " + cDbTableName),
    GET_NOW
            ("SELECT now()"),
    INSERT_JOB
            ("INSERT INTO " + cDbTableName + " (scheduler_id, scheduler_name, scheduler_start_time, scheduler_every_time, scheduler_description, scheduler_status, scheduler_type, scheduler_message_json, scheduler_address_ip, scheduler_address_port, scheduler_created_at) VALUES ('<SCHEDULER_ID>', '<SCHEDULER_NAME>', '<SCHEDULER_START_TIME>', '<SCHEDULER_EVERY_TIME>', '<SCHEDULER_DESCRIPTION>', '<SCHEDULER_STATUS>', '<SCHEDULER_TYPE>', '<SCHEDULER_MESSAGE_JSON>', '<SCHEDULER_ADDRESS_IP>', '<SCHEDULER_ADDRESS_PORT>', '<SCHEDULER_CREATED_AT>')"),
    UPDATE_JOB
            ("UPDATE " + cDbTableName + " SET scheduler_name = '<SCHEDULER_NAME>', scheduler_start_time = '<SCHEDULER_START_TIME>', scheduler_every_time = '<SCHEDULER_EVERY_TIME>', scheduler_description = '<SCHEDULER_DESCRIPTION>', scheduler_status = '<SCHEDULER_STATUS>', scheduler_type = '<SCHEDULER_TYPE>', scheduler_message_json = '<SCHEDULER_MESSAGE_JSON>', scheduler_address_ip = '<SCHEDULER_ADDRESS_IP>', scheduler_address_port = '<SCHEDULER_ADDRESS_PORT>', scheduler_updated_at = '<SCHEDULER_UPDATED_AT>' WHERE scheduler_id = '<SCHEDULER_ID>'"),
    DELETE_JOB
            ("DELETE FROM " + cDbTableName + " WHERE scheduler_id = '<SCHEDULER_ID>'");
    
    private final String mDBQuery;

    private DbQueryEnum(String pDBQuery) {
        mDBQuery = pDBQuery;
    }

    public String getDBQuery() {
        return mDBQuery;
    }

    public static DbQueryEnum parseDBQueryString(String pTypeString) {
        DbQueryEnum tRetDBQuery = null;
        for (DbQueryEnum tDBQuery : values()) {
            if (tDBQuery.getDBQuery().equals(pTypeString)) {
                tRetDBQuery = tDBQuery;
                break;
            }
        }
        return tRetDBQuery;
    }
}
