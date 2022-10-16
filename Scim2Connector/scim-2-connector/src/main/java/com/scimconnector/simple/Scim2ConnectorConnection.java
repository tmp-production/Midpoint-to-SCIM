/*
 * Copyright (c) 2010-2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scimconnector.simple;

import org.identityconnectors.common.logging.Log;

public class Scim2ConnectorConnection {

    private static final Log LOG = Log.getLog(Scim2ConnectorConnection.class);

    private Scim2ConnectorConfiguration configuration;

    public Scim2ConnectorConnection(Scim2ConnectorConfiguration configuration) {
        this.configuration = configuration;
    }

    public void dispose() {
        //todo implement
    }
}