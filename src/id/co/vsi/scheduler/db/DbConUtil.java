package id.co.vsi.scheduler.db;

import id.co.vsi.common.constants.ResponseCode;
import id.co.vsi.common.database.pool.ConnectionPoolManager;
import id.co.vsi.common.database.pool.DatabaseType;
import id.co.vsi.common.log.LogType;
import id.co.vsi.common.log.SystemLog;
import id.co.vsi.common.settings.SystemConfig;
import id.co.vsi.scheduler.common.Common;
import id.co.vsi.systemcore.isocore.SystemException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * $Rev:: 700                                                                   $:Revision of last commit
 * $Author:: didik                                                              $:Author of last commit
 * $Date:: 2013-09-25 10:30:29 +0700 (Wed, 25 Sep 2013)                         $:Date of last commit
 *
 */
public class DbConUtil{

    public static ConnectionPoolManager cDbPostgresSql;

    /**
     *
     * @param pQuery SQL query
     * @param pModuleNameSpace
     * @param pUseLog
     * @return given result from SQL query request in JSONArray
     * @throws JSONException
     * getResultConnection[0] = Db Connection
     * getResultConnection[1] = Db Statement
     * getResultConnection[2] = ResultSet
     * getResultConnection[3] = UpdateRows
     */
    public JSONArray executeQuery(final String pQuery, final String pModuleNameSpace, final boolean pUseLog){
        JSONArray  tResult  = new JSONArray();
        String     tQueryString = pQuery;

        if (cDbPostgresSql == null){
            final String tDbConnectionString = SystemConfig.getNameSpace(pModuleNameSpace).getStringParameter(Common.cDbConnectionString);
            cDbPostgresSql = new DatabaseType().initDB(tDbConnectionString, DatabaseType.POSTGRESQL);
        }

        Connection tConnection =  null;

        try {
            tConnection = cDbPostgresSql.getConnection();

            final Statement tStatement = tConnection.createStatement();

            if (pUseLog) {
                SystemLog.getSingleton().log(this, LogType.DB, tConnection + " = " + cDbPostgresSql.getActiveConnections() + ", query = [" + tQueryString + "]");
            }

            tStatement.execute(tQueryString);

            int tUpdateRows = tStatement.getUpdateCount();

            if (tUpdateRows < 0) {
                ResultSet tResultSet = tStatement.getResultSet();

                int x = 0;

                while (tResultSet.next()) {
                    Map<String, Object> tResultSetRecordHashMap = new HashMap<String, Object>();

                    for (int i = 0; i < tResultSet.getMetaData().getColumnCount(); i++) {
                        String tColumnLabel = tResultSet.getMetaData().getColumnLabel(i + 1);
                        Object tColumnValue = tResultSet.getObject(i + 1);

                        tResultSetRecordHashMap.put(tColumnLabel, tColumnValue);
                    }

                    tResult.put(tResultSetRecordHashMap);

                    x++;
                }

                tUpdateRows = x;

                if (tResultSet != null) {
                    tResultSet.close();
                }
            }

            if (tStatement != null) {
                tStatement.close();
            }

            if (tConnection != null) {
                tConnection.close();
            }

            if (pUseLog) {
                SystemLog.getSingleton().log(this, LogType.DB, "Result : " + tResult);
            }
        } catch (SQLException ex) {
            if (tConnection != null) {
                try {
                    tConnection.close();
                } catch (SQLException ex1) {
                    SystemLog.getSingleton().log(this, LogType.DB_ERROR, "Error in executing query (SQLException): " + tQueryString + ", SQLException : " + ex1);
                }
            }

            if (pUseLog) {
                SystemLog.getSingleton().log(this, LogType.DB_ERROR, "Error in executing query: " + tQueryString + ", SQLException : " + ex);
            }
            throw new SystemException(ResponseCode.ERROR_DATABASE, "Error in executing query: " + tQueryString, ex, this);
        }

        return tResult;
    }
    
    public String executeQueryString(final String pQuery, final String pModuleNameSpace, final boolean pUseLog){
        String tResult  = "";
        String tQueryString = pQuery;

        if (cDbPostgresSql == null){
            final String tDbConnectionString = SystemConfig.getNameSpace(pModuleNameSpace).getStringParameter(Common.cDbConnectionString);
            cDbPostgresSql = new DatabaseType().initDB(tDbConnectionString, DatabaseType.POSTGRESQL);
        }

        Connection tConnection =  null;

        try {
            tConnection = cDbPostgresSql.getConnection();

            final Statement tStatement = tConnection.createStatement();

            if (pUseLog) {
                SystemLog.getSingleton().log(this, LogType.DB, tConnection + " = " + cDbPostgresSql.getActiveConnections() + ", query = [" + tQueryString + "]");
            }

            tStatement.execute(tQueryString);

            int tUpdateRows = tStatement.getUpdateCount();

            if (tUpdateRows < 0) {
                ResultSet tResultSet = tStatement.getResultSet();

                int x = 0;

                while (tResultSet.next()) {
                    Map<String, Object> tResultSetRecordHashMap = new HashMap<String, Object>();

                    for (int i = 0; i < tResultSet.getMetaData().getColumnCount(); i++) {
                        String tColumnLabel = tResultSet.getMetaData().getColumnLabel(i + 1);
                        Object tColumnValue = tResultSet.getObject(i + 1);

                        tResultSetRecordHashMap.put(tColumnLabel, tColumnValue);
                    }

                    x++;
                }

                tUpdateRows = x;

                if (tResultSet != null) {
                    tResultSet.close();
                }
            }

            if (tStatement != null) {
                tStatement.close();
            }

            if (tConnection != null) {
                tConnection.close();
            }

            if (pUseLog) {
                SystemLog.getSingleton().log(this, LogType.DB, "Result : " + tResult);
            }
        } catch (SQLException ex) {
            if (tConnection != null) {
                try {
                    tConnection.close();
                } catch (SQLException ex1) {
                    SystemLog.getSingleton().log(this, LogType.DB_ERROR, "Error in executing query (SQLException): " + tQueryString + ", SQLException : " + ex1);
                }
            }

            if (pUseLog) {
                SystemLog.getSingleton().log(this, LogType.DB_ERROR, "Error in executing query: " + tQueryString + ", SQLException : " + ex);
            }
            throw new SystemException(ResponseCode.ERROR_DATABASE, "Error in executing query: " + tQueryString, ex, this);
        }

        return tResult;
    }
}
