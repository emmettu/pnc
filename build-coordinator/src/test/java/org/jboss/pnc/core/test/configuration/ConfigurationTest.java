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

package org.jboss.pnc.core.test.configuration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.DockerEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.EnvironmentDriverFactory;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class ConfigurationTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(Configuration.class)
                .addClass(DockerEnvironmentDriverModuleConfig.class)
                .addClass(OpenshiftEnvironmentDriverModuleConfig.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties");

        logger.debug(jar.toString(true));
        return jar;
    }

    @Inject
    private Configuration configuration;

    @Test
    public void isEnvDriverEnabled() throws CoreException, ConfigurationParseException {
        OpenshiftEnvironmentDriverModuleConfig openShiftConfig = configuration.getModuleConfig(new PncConfigProvider<>(OpenshiftEnvironmentDriverModuleConfig.class));
        DockerEnvironmentDriverModuleConfig dockerConfig = configuration.getModuleConfig(new PncConfigProvider<>(DockerEnvironmentDriverModuleConfig.class));

        Assert.assertTrue("All environment driver disabled.", !openShiftConfig.isDisabled() || !dockerConfig.isDisabled());
    }
}
