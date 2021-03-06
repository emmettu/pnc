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
package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.integration.utils.AuthResource;
import org.jboss.pnc.integration.utils.JsonUtils;
import org.jboss.pnc.rest.restmodel.ProductReleaseRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductReleaseRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/products/";
    private static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/products/%d/product-versions/";
    private static final String PRODUCT_MILESTONE_REST_ENDPOINT = "/pnc-rest/rest/product-milestones/";
    private static final String PRODUCT_RELEASE_REST_ENDPOINT = "/pnc-rest/rest/product-releases/";
    private static final String PRODUCT_RELEASE_PRODUCTVERSION_REST_ENDPOINT = "/pnc-rest/rest/product-releases/product-versions/%d";
    private static final String PRODUCT_RELEASE_SPECIFIC_REST_ENDPOINT = PRODUCT_RELEASE_REST_ENDPOINT + "%d";

    private static int productId;
    private static int productVersionId;
    private static int productMilestoneId;
    private static int productReleaseId;
    private static int newProductReleaseId;
    private static final String newProductReleaseVersion = "1.0.1.Beta1";
    private static final String updatedProductReleaseVersion = "1.0.1.GA";

    private static AuthenticationProvider authProvider;
    private static String access_token = "no-auth";

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        if (AuthResource.authEnabled()) {
            Configuration configuration = new Configuration();
            AuthenticationModuleConfig config = configuration.getModuleConfig(new PncConfigProvider<AuthenticationModuleConfig>(AuthenticationModuleConfig.class));
            InputStream is = BuildRecordRestTest.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            access_token = authProvider.getTokenString();
        }
    }

    @Test
    @InSequence(1)
    public void prepareProductIdAndProductVersionId() throws IOException {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("content[0].id", value -> productId = Integer.valueOf(value)));
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("content[0].id", value -> productVersionId = Integer.valueOf(value)));

        // Need to create a new product milestone to ensure one to one relation
        JsonTemplateBuilder productMilestoneTemplate = JsonTemplateBuilder.fromResource("productMilestone_template");
        productMilestoneTemplate.addValue("_productVersionId", String.valueOf(productVersionId));
        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body(productMilestoneTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(PRODUCT_MILESTONE_REST_ENDPOINT);
        Assertions.assertThat(response.statusCode()).isEqualTo(201);
        String location = response.getHeader("Location");
        productMilestoneId = Integer.valueOf(location.substring(location.lastIndexOf(PRODUCT_MILESTONE_REST_ENDPOINT)
                + PRODUCT_MILESTONE_REST_ENDPOINT.length()));
    }

    @Test
    @InSequence(2)
    public void prepareProductReleaseId() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when().get(String.format(PRODUCT_RELEASE_REST_ENDPOINT))
                .then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("content[0].id", value -> productReleaseId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(3)
    public void shouldGetSpecificProductRelease() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_RELEASE_SPECIFIC_REST_ENDPOINT, productReleaseId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("content.id"));
    }

    @Test
    @InSequence(4)
    public void shouldCreateNewProductRelease() throws IOException {
        JsonTemplateBuilder productReleaseTemplate = JsonTemplateBuilder.fromResource("productRelease_template");
        productReleaseTemplate.addValue("_productReleaseVersion", String.valueOf(newProductReleaseVersion));
        productReleaseTemplate.addValue("_productVersionId", String.valueOf(productVersionId));
        productReleaseTemplate.addValue("_productMilestoneId", String.valueOf(productMilestoneId));

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body(productReleaseTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(PRODUCT_RELEASE_REST_ENDPOINT);
        Assertions.assertThat(response.statusCode()).isEqualTo(201);

        String location = response.getHeader("Location");
        logger.info("Found location in Response header: " + location);

        newProductReleaseId = Integer.valueOf(location.substring(location.lastIndexOf(PRODUCT_RELEASE_REST_ENDPOINT)
                + PRODUCT_RELEASE_REST_ENDPOINT.length()));
        logger.info("Created id of product release: " + newProductReleaseId);

    }

    @Test
    @InSequence(5)
    public void shouldUpdateProductRelease() throws Exception {

        logger.info("### newProductReleaseId: " + newProductReleaseId);

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_RELEASE_SPECIFIC_REST_ENDPOINT, newProductReleaseId));

        ProductReleaseRest productReleaseRest = response.body().jsonPath().getObject("content", ProductReleaseRest.class);

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(productReleaseRest.getId()).isEqualTo(newProductReleaseId);
        Assertions.assertThat(productReleaseRest.getVersion()).isEqualTo(newProductReleaseVersion);

        productReleaseRest.setVersion(updatedProductReleaseVersion);

        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token).body(JsonUtils.toJson(productReleaseRest))
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(PRODUCT_RELEASE_SPECIFIC_REST_ENDPOINT, newProductReleaseId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token).contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_RELEASE_SPECIFIC_REST_ENDPOINT, newProductReleaseId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt("content.id")).isEqualTo(newProductReleaseId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("content.version")).isEqualTo(updatedProductReleaseVersion);

    }

    @Test
    @InSequence(6)
    public void shouldGetAllProductReleaseOfProductVersion() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_RELEASE_PRODUCTVERSION_REST_ENDPOINT, productVersionId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("content.id"));
    }

    private String loadJsonFromFile(String resource) throws IOException {
        return IoUtils.readFileOrResource(resource, resource + ".json", getClass().getClassLoader());
    }
}
