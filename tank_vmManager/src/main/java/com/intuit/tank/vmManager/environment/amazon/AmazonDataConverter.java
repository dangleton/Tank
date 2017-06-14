/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.vmManager.environment.amazon;

/*
 * #%L
 * VmManager
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.SpotInstanceStateFault;
import com.intuit.tank.vm.api.enumerated.VMProvider;
import com.intuit.tank.vm.api.enumerated.VMRegion;
import com.intuit.tank.vm.vmManager.VMInformation;

/**
 * AmazonDataConverter
 * 
 * @author dangleton
 * 
 */
public class AmazonDataConverter {
    private static final Logger LOG = LogManager.getLogger(AmazonDataConverter.class);

    public List<VMInformation> processStateChange(List<InstanceStateChange> changes) {
        List<VMInformation> output = new ArrayList<VMInformation>();
        try {
            for (InstanceStateChange instance : changes) {
                VMInformation info = new VMInformation();
                info.setInstanceId(instance.getInstanceId());
                info.setState(instance.getCurrentState().getName());
                output.add(info);
            }

            return output;
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param data
     * @param instance
     * @param region
     * @return
     */
    public VMInformation instanceToVmInformation(Reservation data, Instance instance, VMRegion region) {
        VMInformation info = new VMInformation();
        info.setProvider(VMProvider.Amazon);
        info.setRequestId(data.getRequesterId());
        info.setImageId(instance.getImageId());
        info.setInstanceId(instance.getInstanceId());
        info.setKeyName(instance.getKeyName());
        // info.setLaunchTime();
        info.setRegion(region);
        info.setPlatform(instance.getPlatform());
        info.setPrivateDNS(instance.getPrivateDnsName());
        info.setPublicDNS(instance.getPublicDnsName());
        info.setState(instance.getState().getName());
        info.setSize(instance.getInstanceType());
        return info;
    }

    /**
     * @param reservation
     * @param region
     * @return
     */
    public List<VMInformation> processReservation(Reservation reservation, VMRegion region) {
        List<VMInformation> output = new ArrayList<VMInformation>();
        try {
            for (Instance instance : reservation.getInstances()) {
                VMInformation info = instanceToVmInformation(reservation, instance, region);
                output.add(info);
            }

            return output;
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param asynchEc2Client
     * @param reservation
     * @param region
     * @return
     */
    public List<VMInformation> processSpotReservation(AmazonEC2AsyncClient ec2, RequestSpotInstancesResult result,
            VMRegion region) {

        try {
            List<VMInformation> output = new ArrayList<VMInformation>();
            ArrayList<String> spotInstanceRequestIds = new ArrayList<String>();

            // Add all of the request ids to the hashset, so we can determine when they hit the
            // active state.
            for (SpotInstanceRequest requestResponse : result.getSpotInstanceRequests()) {
                LOG.info("Created Spot Request: " + requestResponse.getSpotInstanceRequestId());
                spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
            }
            boolean anyOpen = false;
            long maxWait = System.currentTimeMillis() + (1000 * 60 * 3);
            do {
                DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
                describeRequest.setSpotInstanceRequestIds(spotInstanceRequestIds);

                anyOpen = false;

                try {
                    // Retrieve all of the requests we want to monitor.
                    DescribeSpotInstanceRequestsResult describeResult = ec2
                            .describeSpotInstanceRequests(describeRequest);
                    List<SpotInstanceRequest> describeResponses = describeResult.getSpotInstanceRequests();

                    // Look through each request and determine if they are all in
                    // the active state.
                    for (SpotInstanceRequest describeResponse : describeResponses) {
                        // If the state is open, it hasn't changed since we attempted
                        // to request it. There is the potential for it to transition
                        // almost immediately to closed or cancelled so we compare
                        // against open instead of active.
                        if (describeResponse.getState().equals("open")) {
                            anyOpen = true;
                        } else if (describeResponse.getState().equals("active")) {
                            VMInformation info = requestToVmInformation(describeResponse, region);
                            output.add(info);
                        }
                    }
                } catch (AmazonServiceException e) {
                    // If we have an exception, ensure we don't break out of
                    // the loop. This prevents the scenario where there was
                    // blip on the wire.
                    anyOpen = true;
                }

                try {
                    // Sleep for 15 seconds.
                    Thread.sleep(15 * 1000);
                } catch (Exception e) {
                    // Do nothing because it woke up early.
                }
            } while (anyOpen && System.currentTimeMillis() < maxWait);

            return output;
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param reqest
     * @param region
     * @return
     */
    public VMInformation requestToVmInformation(SpotInstanceRequest reqest, VMRegion region) {
        VMInformation info = new VMInformation();
        info.setProvider(VMProvider.Amazon);
        info.setRequestId(reqest.getSpotInstanceRequestId());
        info.setImageId(reqest.getLaunchSpecification().getImageId());
        info.setInstanceId(reqest.getInstanceId());
        info.setKeyName(reqest.getLaunchSpecification().getKeyName());
        // info.setLaunchTime();
        info.setRegion(region);
        // info.setPlatform(instance.getLaunchSpecification());
        // for (InstanceNetworkInterfaceSpecification spec : instance.getLaunchSpecification().getNetworkInterfaces()) {
        // spec.get
        // if (spec.isAssociatePublicIpAddress()) {
        // info.setPublicDNS(spec.get);
        // }
        // }
        // info.setPrivateDNS(instance.getLaunchSpecification().getNetworkInterfaces());
        // info.setPublicDNS(instance.getPublicDnsName());
        info.setState(reqest.getState());
        info.setSize(reqest.getLaunchSpecification().getInstanceType());
        return info;
    }

}
