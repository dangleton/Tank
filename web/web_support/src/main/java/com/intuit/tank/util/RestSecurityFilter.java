package com.intuit.tank.util;

import static org.picketlink.idm.model.basic.BasicModel.getRole;
import static org.picketlink.idm.model.basic.BasicModel.getUser;
import static org.picketlink.idm.model.basic.BasicModel.grantRole;

/*
 * #%L
 * JSF Support Beans
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.picketlink.Identity;
import org.picketlink.authentication.Authenticator.AuthenticationStatus;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Role;

import com.intuit.tank.auth.TankUser;
import com.intuit.tank.dao.UserDao;
import com.intuit.tank.project.User;
import com.intuit.tank.vm.common.TankConstants;
import com.intuit.tank.vm.settings.TankConfig;

public class RestSecurityFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(RestSecurityFilter.class);

    private TankConfig config;

    @Inject
    private Identity identity;

    @Inject
    private IdentityManager identityManager;

    @Inject
    private RelationshipManager relationshipManager;

    private UserDao userDao = new UserDao();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        config = new TankConfig();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if (config.isRestSecurityEnabled() && shouldSecure((HttpServletRequest) request)) {
            User user = getUser((HttpServletRequest) request);
            if (user == null) {
                // send 401 unauthorized and return
                HttpServletResponse resp = (HttpServletResponse) response;
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return; // break filter chain, requested JSP/servlet will not be executed
            } else {
                addUserToIdentity(user);
            }
        }
        chain.doFilter(request, response);
    }

    private boolean shouldSecure(HttpServletRequest request) {
        boolean ret = true;
        String path = request.getServletPath() + request.getPathInfo();
        ret = (!path.startsWith("/rest/v1/agent-service"));
        return ret;
    }

    public User getUser(HttpServletRequest req) {
        User user = null;
        // firsttry the session
        if (identity != null) {
            org.picketlink.idm.model.basic.User picketLinkUser = (org.picketlink.idm.model.basic.User) identity
                    .getAccount();
            if (picketLinkUser != null) {
                if (picketLinkUser instanceof TankUser) {
                    user = ((TankUser) picketLinkUser).getUserEntity();
                } else {
                    user = userDao.findByUserName(picketLinkUser.getLoginName());
                }
            }
        }
        if (user == null) {
            String authHeader = req.getHeader("authorization");
            try {
                if (authHeader != null) {
                    String[] split = StringUtils.split(authHeader, ' ');
                    if (split.length == 2) {
                        String s = new String(Base64.decodeBase64(split[1]), "UTF-8");
                        String[] upass = StringUtils.split(s, ":", 2);
                        if (upass.length == 2) {
                            String name = upass[0];
                            String token = upass[1];
                            UserDao userDao = new UserDao();
                            user = userDao.findByApiToken(token);
                            if (user == null || !user.getName().equals(name)) {
                                user = userDao.authenticate(name, token);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Error getting user: " + e, e);
            }
        }

        return user;
    }

    @Override
    public void destroy() {

    }

    private void addUserToIdentity(User user) {
        org.picketlink.idm.model.basic.User idmuser = BasicModel.getUser(identityManager, user.getName());
        if (idmuser == null) {
            idmuser = new org.picketlink.idm.model.basic.User(user.getName());
            idmuser.setId(Integer.toString(user.getId()));
            idmuser.setCreatedDate(user.getCreated());
            idmuser.setEmail(user.getEmail());
            idmuser.setAttribute(new Attribute<String>("name", user.getName()));
            identityManager.add(idmuser);
            for (com.intuit.tank.project.Group g : user.getGroups()) {
                Role role = getRole(identityManager, g.getName());
                if (role == null) {
                    role = new Role(g.getName());
                    identityManager.add(role);
                }
                grantRole(relationshipManager, idmuser, role);
            }
        }
    }

}
