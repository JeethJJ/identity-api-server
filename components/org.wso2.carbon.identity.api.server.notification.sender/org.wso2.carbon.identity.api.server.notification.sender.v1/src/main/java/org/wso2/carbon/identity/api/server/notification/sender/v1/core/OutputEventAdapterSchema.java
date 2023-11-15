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

package org.wso2.carbon.identity.api.server.notification.sender.v1.core;


import java.util.List;

/**
 * this class is used to transfer the event adapter type details to the UI. UI renders the
 * properties according to the properties specified here.
 */
public class OutputEventAdapterSchema {

    private final String type;
    private String usageTips;
    private final List<String> supportedMessageFormats;
    private final List<Property> staticPropertyList;
    private final List<Property> dynamicPropertyList;

    public OutputEventAdapterSchema(String type, String usageTips, List<String> supportedMessageFormats,
                                    List<Property> staticPropertyList,
                                    List<Property> dynamicPropertyList) {
        this.type = type;
        this.usageTips = usageTips;
        this.supportedMessageFormats = supportedMessageFormats;
        this.staticPropertyList = staticPropertyList;
        this.dynamicPropertyList = dynamicPropertyList;
    }

    public String getType() {
        return type;
    }

    public String getUsageTips() {
        return usageTips;
    }

    public List<String> getSupportedMessageFormats() {
        return supportedMessageFormats;
    }

    public List<Property> getStaticPropertyList() {
        return staticPropertyList;
    }

    public List<Property> getDynamicPropertyList() {

        return dynamicPropertyList;
    }

}
