/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.component.log.it;

import org.apache.camel.builder.RouteBuilder;
import org.jboss.logging.Logger;

public class MdcLogRoutes extends RouteBuilder {
    @Override
    public void configure() {
        onException(Exception.class)
                .handled(exchange -> {
                    Logger LOG = Logger.getLogger(MdcLogRoutes.class);
                    LOG.error("Caught exception");
                    return true;
                });

        from("direct:mdcLog").routeId("mdc-log")
                .to("mdcLog:mdc");

        from("direct:mdcLogFromException").routeId("mdc-log-from-exception")
                .throwException(new Exception("Forced"));
    }
}
