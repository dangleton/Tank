/**
 * 
 */
package com.intuit.tank.okhttpclient;

import java.util.HashMap;
import java.util.Map;

import com.intuit.tank.http.TankHttpLogger;
import com.intuit.tank.logging.LogEventType;
import com.intuit.tank.logging.LoggingProfile;
import com.intuit.tank.vm.settings.AgentConfig;
import com.intuit.tank.vm.settings.TankConfig;

/**
 * @author denisa
 *
 */
public class MockLogUtil implements TankHttpLogger {

    /* (non-Javadoc)
     * @see com.intuit.tank.http.TankHttpLogger#getLogMessage(java.lang.String)
     */
    @Override
    public Map<String,String> getLogMessage(String msg) {
    	Map<String,String> map = new HashMap<String, String>();
    	map.put("Message", msg);
        return map;
    }

    /* (non-Javadoc)
     * @see com.intuit.tank.http.TankHttpLogger#getLogMessage(java.lang.String, com.intuit.tank.logging.LogEventType)
     */
    @Override
    public Map<String,String> getLogMessage(String msg, LogEventType type) {
    	Map<String,String> map = new HashMap<String, String>();
    	map.put("Message", msg);
        return map;
    }

    /* (non-Javadoc)
     * @see com.intuit.tank.http.TankHttpLogger#getLogMessage(java.lang.String, com.intuit.tank.logging.LogEventType, com.intuit.tank.logging.LoggingProfile)
     */
    @Override
    public Map<String,String> getLogMessage(String msg, LogEventType type, LoggingProfile profile) {
    	Map<String,String> map = new HashMap<String, String>();
    	map.put("Message", msg);
        return map;
    }

    /* (non-Javadoc)
     * @see com.intuit.tank.http.TankHttpLogger#isTextMimeType(java.lang.String)
     */
    @Override
    public boolean isTextMimeType(String mimeType) {
        return false;
    }

    /* (non-Javadoc)
     * @see com.intuit.tank.http.TankHttpLogger#getAgentConfig()
     */
    @Override
    public AgentConfig getAgentConfig() {
        return new TankConfig().getAgentConfig();
    }

}
