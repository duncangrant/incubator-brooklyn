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

import org.apache.brooklyn.api.entity.Entity;
import org.apache.brooklyn.core.effector.EffectorTasks;
import org.apache.brooklyn.util.core.task.DynamicTasks;
import org.apache.brooklyn.util.core.task.ssh.SshTasks;
import org.apache.brooklyn.util.core.task.system.ProcessTaskFactory;
import org.apache.brooklyn.util.ssh.BashCommands;

public class LogstashShared {

    private Entity entity;

    public LogstashShared(Entity entity) {
        this.entity = entity;
    }

    public void stop() {

    }

    public boolean install(String downloadLocation) {
        String installJava = BashCommands.alternatives(
                BashCommands.installJava8(),
                BashCommands.installJava7());

        String command = BashCommands.chainGroup(
                installJava,
                BashCommands.INSTALL_WGET,
                "wget --quiet -O - " + downloadLocation + " | tar zxv ");

        return execCommandOnMachine("Install logstash agent", command);
    }

    public boolean customize(String customize, String configLoc) {
        String command = "echo \"" + customize + "\" > " + configLoc;

        return execCommandOnMachine("Customize logstash agent", command);
    }

    public boolean launch(String configLoc) {
        String command = "nohup logstash-1.5.4/bin/logstash -f " + configLoc + " > console.out 2>&1 &";
        return execCommandOnMachine("Launch logstash agent", command);
    }

    private boolean execCommandOnMachine(String description, String launchCommand) {
        ProcessTaskFactory<Integer> taskFactory = SshTasks.newSshExecTaskFactory(EffectorTasks.getSshMachine(entity), launchCommand)
                .summary(description)
                .allowingNonZeroExitCode();
        return DynamicTasks.queueIfPossible(taskFactory).orSubmitAsync(entity).getTask().getUnchecked() == 0;
    }
}
