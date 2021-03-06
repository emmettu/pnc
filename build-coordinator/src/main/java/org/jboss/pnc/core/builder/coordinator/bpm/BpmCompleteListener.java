/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.core.builder.coordinator.bpm;

import org.jboss.pnc.spi.BuildStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmCompleteListener {

    private static final Logger logger = LoggerFactory.getLogger(BpmCompleteListener.class);

    Map<Long, BpmListener> listeners = new HashMap<>(); //TODO evict from map if there is no response from BPM server in specified time-out

    public void subscribe(BpmListener bpmListener) {
        logger.debug("Subscribing listener for coordinating task id [{}].", bpmListener.getTaskId());
        listeners.put(bpmListener.getTaskId(), bpmListener);
    }

    public void notifyCompleted(long taskId, BuildStatus buildStatus) {
        logger.debug("Coordinating task id [{}] completed.", taskId);
        BpmListener bpmListener = listeners.get(taskId);
        listeners.remove(taskId);
        bpmListener.onComplete(buildStatus);
    }
}
