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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordSetRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

import static org.jboss.pnc.spi.datastore.predicates.BuildRecordSetPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordSetPredicates.withPerformedInProductMilestoneId;

@Stateless
public class BuildRecordSetProvider extends AbstractProvider<BuildRecordSet, BuildRecordSetRest> {

    public BuildRecordSetProvider() {
    }

    @Inject
    public BuildRecordSetProvider(BuildRecordSetRepository buildRecordSetRepository,
            RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        super(buildRecordSetRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
    }

    @Override
    protected Function<? super BuildRecordSet, ? extends BuildRecordSetRest> toRESTModel() {
        return buildRecordSet -> new BuildRecordSetRest(buildRecordSet);
    }

    @Override
    protected Function<? super BuildRecordSetRest, ? extends BuildRecordSet> toDBModelModel() {
        return buildRecordSet -> buildRecordSet.toBuildRecordSet();
    }

    public CollectionInfo<BuildRecordSetRest> getAllForPerformedInProductMilestone(int pageIndex, int pageSize,
            String sortingRsql, String query, Integer milestoneId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withPerformedInProductMilestoneId(milestoneId));
    }

    public CollectionInfo<BuildRecordSetRest> getAllForBuildRecord(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer recordId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildRecordId(recordId));
    }

}
