/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.client.v1.project;

/*
 * #%L
 * Project Rest Client
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.intuit.tank.api.model.v1.project.ProjectContainer;
import com.intuit.tank.api.model.v1.project.ProjectTO;
import com.intuit.tank.api.service.v1.project.ProjectService;
import com.intuit.tank.rest.BaseRestClient;
import com.intuit.tank.rest.RestServiceException;
import com.intuit.tank.rest.util.ServiceConsants;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * ProjectClientV1
 * 
 * @author dangleton
 * 
 */
public class ProjectServiceClientV1 extends BaseRestClient {

    private static final String SERVICE_BASE_URL = ServiceConsants.REST_SERVICE_CONTEXT
            + ProjectService.SERVICE_RELATIVE_PATH;

    /**
     * 
     * @param serviceUrl
     */
    public ProjectServiceClientV1(String serviceUrl) {
        super(serviceUrl, null, null);
    }

    /**
     * 
     * @param serviceUrl
     */
    public ProjectServiceClientV1(String serviceUrl, final String proxyServer, final Integer proxyPort) {
        super(serviceUrl, proxyServer, proxyPort);
    }

    /**
     * 
     * @return
     */
    protected String getServiceBaseUrl() {
        return SERVICE_BASE_URL;
    }

    /**
     * @{inheritDoc
     */
    public void deleteProject(int projectId) throws RestServiceException {
        WebResource webResource = client.resource(urlBuilder.buildUrl(
                ProjectService.METHOD_DELETE, projectId));
        ClientResponse response = webResource.delete(ClientResponse.class);
        exceptionHandler.checkStatusCode(response);
    }

    /**
     * @{inheritDoc
     */
    public ProjectTO getProject(int projectId) throws RestServiceException {
        WebResource webResource = client.resource(urlBuilder
                .buildUrl(ProjectService.METHOD_PROJECT, projectId));
        ClientResponse response = webResource.accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            return null;
        }
        exceptionHandler.checkStatusCode(response);
        ProjectTO container = response.getEntity(ProjectTO.class);
        return container;
    }

    /**
     * @{inheritDoc
     */
    public Integer runProject(int projectId) throws RestServiceException {
        WebResource webResource = client.resource(urlBuilder
                .buildUrl(ProjectService.METHOD_RUN, projectId));
        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN)
                .post(ClientResponse.class);
        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            return null;
        }
        exceptionHandler.checkStatusCode(response);
        Integer ret = response.getEntity(Integer.class);
        return ret;
    }

    /**
     * 
     * @return
     */
    public List<ProjectTO> getProjects() {
        WebResource webResource = client.resource(urlBuilder
                .buildUrl(ProjectService.METHOD_PROJECTS));
        ClientResponse response = webResource.accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        exceptionHandler.checkStatusCode(response);
        ProjectContainer container = response.getEntity(ProjectContainer.class);
        return container.getProjects();
    }

    public String downloadTestScriptForProject(Integer projectId) {
        WebResource webResource = client.resource(urlBuilder.buildUrl(ProjectService.METHOD_PROJECT_SCRIPT_DOWNLOAD,
                projectId));
        ClientResponse response = webResource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
        exceptionHandler.checkStatusCode(response);
        return response.getEntity(String.class);
    }

}
