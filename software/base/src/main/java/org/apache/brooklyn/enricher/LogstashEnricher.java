/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.brooklyn.enricher;

import org.apache.brooklyn.api.entity.EntityLocal;
import org.apache.brooklyn.api.sensor.Enricher;
import org.apache.brooklyn.api.sensor.SensorEvent;
import org.apache.brooklyn.api.sensor.SensorEventListener;
import org.apache.brooklyn.core.enricher.AbstractEnricher;
import org.apache.brooklyn.core.entity.Attributes;

public class LogstashEnricher extends AbstractEnricher implements Enricher {

    private LogstashShared logstashShared;

    @Override
    public void setEntity(EntityLocal entity) {
        super.setEntity(entity);
        subscribeLogFileCreated();
    }

    private void subscribeLogFileCreated() {
        subscribe(entity, Attributes.LOG_FILE_LOCATION, logFileCreatedEventHandler);
    }

    private final SensorEventListener<Object> logFileCreatedEventHandler = new SensorEventListener<Object>() {
        @Override
        public void onEvent(SensorEvent<Object> event) {
            Object value = event.getValue();

            logstashShared = new LogstashShared(entity);
            logstashShared.install("https://download.elastic.co/logstash/logstash/logstash-1.5.4.tar.gz");

            if (value != null) {
                logstashShared.customize("input {file {path=>\\\"" + value + "\\\"}}\n" +
                        "output {file {path=>\\\"/tmp/out.txt\\\"}}", "~/test.conf");

                logstashShared.launch("~/test.conf");
            }
        }
    };

}
