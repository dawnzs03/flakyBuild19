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
package org.apache.camel.quarkus.component.jackson.avro.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.AvroLibrary;

public class JacksonAvroRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:marshal-pojo")
                .marshal().avro(AvroLibrary.Jackson, Pojo.class, "avroSchemaResolver");

        from("direct:unmarshal-pojo")
                .unmarshal().avro(AvroLibrary.Jackson, Pojo.class, "avroSchemaResolver");

        from("direct:unmarshal-json-node")
                .unmarshal().avro(AvroLibrary.Jackson, JsonNode.class, "avroSchemaResolver");

        from("direct:unmarshal-defined-dataformat")
                .unmarshal("jacksonAvroDataFormat");

        from("direct:marshal-pojo-list")
                .marshal("jacksonAvroDataFormatForList");

        from("direct:unmarshal-pojo-list")
                .unmarshal("jacksonAvroDataFormatForList");
    }
}
