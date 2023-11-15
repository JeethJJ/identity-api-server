/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.api.server.notification.sender.v1.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.api.server.notification.sender.v1.core.EventAdapterUtil;
import org.wso2.carbon.identity.api.server.notification.sender.v1.core.OutputEventAdapter;
import org.wso2.carbon.identity.api.server.notification.sender.v1.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.identity.api.server.notification.sender.v1.core.exception.OutputEventAdapterException;

import java.util.Map;

/**
 * This class represents the Output Adapter Runtime.
 */
public class OutputAdapterRuntime {
    private static final Log log = LogFactory.getLog(OutputAdapterRuntime.class);
    private final OutputEventAdapter outputEventAdapter;
    private final String name;
    private volatile boolean connected = false;
    private final DecayTimer timer = new DecayTimer();
    private volatile long nextConnectionTime;


    public OutputAdapterRuntime(OutputEventAdapter outputEventAdapter, String name) throws OutputEventAdapterException {
        this.outputEventAdapter = outputEventAdapter;
        this.name = name;
        synchronized (this) {
            outputEventAdapter.init();
//            try {
//                outputEventAdapter.connect();
//                connected = true;
//            } catch (ConnectionUnavailableException e) {
//                connected = false;
//                outputEventAdapter.disconnect();
//                log.error("Error initializing connecting on " + this.name + ",
//                reconnection will be tried on next event arrival.", e);
//            } catch (OutputEventAdapterRuntimeException e) {
//                connected = false;
//                outputEventAdapter.disconnect();
//                log.error("Error initializing connecting on " + this.name + ",
//                reconnection will be tried on next event arrival.", e);
//            }
        }
    }

    public void publish(Object message, Map<String, String> dynamicProperties) {
        try {
            try {
                if (connected) {
                    outputEventAdapter.publish(message, dynamicProperties);
                } else {
                    if (nextConnectionTime <= System.currentTimeMillis()) {
                        synchronized (this) {
                            if (!connected) {
                                if (nextConnectionTime <= System.currentTimeMillis()) {
                                    outputEventAdapter.connect();
                                    outputEventAdapter.publish(message, dynamicProperties);
                                    connected = true;
                                    timer.reset();
                                } else {
                                    logAndDrop(message);
                                }
                            } else {
                                outputEventAdapter.publish(message, dynamicProperties);
                            }
                        }
                    } else {
                        logAndDrop(message);
                    }
                }
            } catch (ConnectionUnavailableException e) {
                connected = false;
                if (nextConnectionTime <= System.currentTimeMillis()) {
                    synchronized (this) {
                        if (nextConnectionTime <= System.currentTimeMillis()) {
                            outputEventAdapter.disconnect();
                            timer.incrementPosition();
                            nextConnectionTime = System.currentTimeMillis() + timer.returnTimeToWait();
                            if (timer.returnTimeToWait() == 0) {
                                log.error("Connection unavailable for Output Adopter '" + name
                                        + "' reconnecting.", e);
                                publish(message, dynamicProperties);
                            } else {
                                log.error("Connection unavailable for Output Adopter '" + name
                                        + "' reconnection will be retried in " + (timer.returnTimeToWait())
                                        + " milliseconds.", e);
                            }
                        } else {
                            logAndDrop(message);
                        }
                    }
                } else {
                    logAndDrop(message);
                }
            }
        } catch (Throwable e) {
            EventAdapterUtil.logAndDrop(name, message, null, e, log, PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getTenantId());
        }
    }

    private void logAndDrop(Object message) {
        log.error("Event dropped, Output Adapter '" + name + "' suspended, Adapter will be active after "
                + (nextConnectionTime - System.currentTimeMillis()) + " milliseconds.");
        if (log.isDebugEnabled()) {
            log.debug("Output Adapter '" + name + "' suspended, dropping event: /n" + message + "/n");
        }
    }

    public void destroy() {
        try {
            outputEventAdapter.disconnect();
        } finally {
            outputEventAdapter.destroy();
        }
    }

    public boolean isPolled() {
        return outputEventAdapter.isPolled();
    }
}
