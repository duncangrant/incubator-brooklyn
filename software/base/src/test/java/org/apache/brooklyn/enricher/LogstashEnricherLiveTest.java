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

import static org.testng.Assert.assertEquals;

import org.apache.brooklyn.api.entity.EntitySpec;
import org.apache.brooklyn.api.sensor.EnricherSpec;
import org.apache.brooklyn.core.effector.EffectorTasks;
import org.apache.brooklyn.core.entity.Attributes;
import org.apache.brooklyn.core.entity.Entities;
import org.apache.brooklyn.core.entity.EntityPredicates;
import org.apache.brooklyn.core.entity.lifecycle.Lifecycle;
import org.apache.brooklyn.core.test.BrooklynAppLiveTestSupport;
import org.apache.brooklyn.entity.software.base.VanillaSoftwareProcess;
import org.apache.brooklyn.entity.software.base.VanillaSoftwareProcessImpl;
import org.apache.brooklyn.entity.software.base.VanillaSoftwareProcessSshDriver;
import org.apache.brooklyn.location.byon.FixedListMachineProvisioningLocation;
import org.apache.brooklyn.location.ssh.SshMachineLocation;
import org.apache.brooklyn.test.Asserts;
import org.apache.brooklyn.util.time.Duration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class LogstashEnricherLiveTest extends BrooklynAppLiveTestSupport {
    private static final String LOCATION_SPEC =
            "byon(privateKeyFile=/Users/andrea/Cloudsoft/Projects/logstash/logstash_id_rsa,hosts=\"192.168.200.10\"," +
                    "user=\"vagrant\")";
    private FixedListMachineProvisioningLocation location;

    @Override
    @BeforeMethod(alwaysRun=true)
    public void setUp() throws Exception {
        super.setUp();
        location = (FixedListMachineProvisioningLocation) mgmt.getLocationRegistry().resolve(LOCATION_SPEC);
    }

    @Test(groups = "Live")
    public void testLogstashEnricher() {
        String launchCmd = "nohup bash -c \"echo \\$\\$ > $PID_FILE; while true; do sleep 1000; done\" &";
        EntitySpec<VanillaSoftwareProcess> procSpec = EntitySpec.create(VanillaSoftwareProcess.class)
                .configure(VanillaSoftwareProcess.LAUNCH_COMMAND, launchCmd)
                .enricher(EnricherSpec.create(LogstashEnricher.class));
        VanillaSoftwareProcess vanillaSoftwareProcess = app.createAndManageChild(procSpec);
        app.start(ImmutableList.of(location));

        vanillaSoftwareProcess.sensors().set(Attributes.LOG_FILE_LOCATION, "/tmp/vanilla.log");
        waitHealthy(vanillaSoftwareProcess);

        SshMachineLocation machine = EffectorTasks.getSshMachine(vanillaSoftwareProcess);
        String pidFile = getPidFile(vanillaSoftwareProcess);
        String catCmd = "cat " + pidFile;
        int catCmdCode = machine.execCommands("read pid process", ImmutableList.of(catCmd));

        assertEquals(catCmdCode, 0);
    }

    private String getPidFile(VanillaSoftwareProcess proc) {
        VanillaSoftwareProcessImpl impl = (VanillaSoftwareProcessImpl)Entities.deproxy(proc);
        return ((VanillaSoftwareProcessSshDriver)impl.getDriver()).getPidFile();
    }

    private void waitFailed(VanillaSoftwareProcess proc) {
        Asserts.eventually(ImmutableMap.of("timeout", Duration.FIVE_MINUTES), Suppliers.ofInstance(proc), EntityPredicates.attributeEqualTo(Attributes.SERVICE_STATE_ACTUAL, Lifecycle.ON_FIRE));
    }

    private void waitHealthy(VanillaSoftwareProcess proc) {
        Asserts.eventually(ImmutableMap.of("timeout", Duration.FIVE_MINUTES), Suppliers.ofInstance(proc), EntityPredicates.attributeEqualTo(Attributes.SERVICE_STATE_ACTUAL, Lifecycle.RUNNING));
    }
}
