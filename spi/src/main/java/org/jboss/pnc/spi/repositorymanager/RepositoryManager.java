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
package org.jboss.pnc.spi.repositorymanager;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryDeletion;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryPromotion;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositoryManager {

    /**
     * Create a new repository session tuned to the parameters of that build collection and the build that will use this
     * repository session.
     *
     * @param buildExecution The build execution currently running
     * @return The new repository session
     * @throws RepositoryManagerException If there is a problem creating the repository
     */
    RepositorySession createBuildRepository(BuildExecution buildExecution)
            throws RepositoryManagerException;

    /**
     * Add the repository containing output associated with the specified {@link BuildRecord} to the membership of the
     * repository group with the given ID.
     * Note that the operation won't start until monitoring starts for the returned {@link RunningRepositoryPromotion} instance.
     *
     * @param buildRecord The build output to promote
     * @param toGroup The ID of the repository group where the build output should be promoted
     *
     * @return An object representing the running promotion process, with callbacks for result and error.
     *
     * @throws RepositoryManagerException If there is a problem promoting the build
     */
    RunningRepositoryPromotion promoteBuild(BuildRecord buildRecord, String toGroup)
            throws RepositoryManagerException;

    /**
     * Used to purge the artifacts that were output from a given build (including the specific hosted repository which was used
     * for that build).
     * Note that the operation won't start until monitoring starts for the returned {@link RunningRepositoryDeletion} instance.
     *
     * @param buildRecord The build whose artifacts/repositories should be removed
     * @return An object representing the running deletion, with callbacks for result and error.
     *
     * @throws RepositoryManagerException If there is a problem deleting the build
     */
    RunningRepositoryDeletion deleteBuild(BuildRecord buildRecord) throws RepositoryManagerException;

    boolean canManage(RepositoryType managerType);

}
