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
package com.bcd.config.shiro.anno;

import com.bcd.base.config.shiro.anno.RequiresNotePermissions;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.subject.Subject;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * Checks to see if a @{@link RequiresPermissions RequiresPermissions} annotation is
 * declared, and if so, performs a permission check to see if the calling <code>Subject</code> is allowed continued
 * access.
 *
 * @since 0.9.0
 */
public class NotePermissionAnnotationHandler extends AuthorizingAnnotationHandler {

    /**
     * Default no-argument constructor that ensures this handler looks for
     * {@link RequiresPermissions RequiresPermissions} annotations.
     */
    public NotePermissionAnnotationHandler() {
        super(RequiresNotePermissions.class);
    }

    /**
     * Returns the annotation {@link RequiresPermissions#value value}, from which the Permission will be constructed.
     *
     * @param a the RequiresPermissions annotation being inspected.
     * @return the annotation's <code>value</code>, from which the Permission will be constructed.
     */
    protected String[] getAnnotationValue(Annotation a) {
        RequiresNotePermissions rpAnnotation = (RequiresNotePermissions) a;
        return Arrays.stream(rpAnnotation.value()).map(e->e.getCode()).toArray(len->new String[len]);
    }

    /**
     * Ensures that the calling <code>Subject</code> has the Annotation's specified permissions, and if not, throws an
     * <code>AuthorizingException</code> indicating access is denied.
     *
     * @param a the RequiresPermission annotation being inspected to check for one or more permissions
     * @throws AuthorizationException
     *          if the calling <code>Subject</code> does not have the permission(s) necessary to
     *          continue access or execution.
     */
    public void assertAuthorized(Annotation a) throws AuthorizationException {
        if (!(a instanceof RequiresNotePermissions)) return;

        RequiresNotePermissions rpAnnotation = (RequiresNotePermissions) a;
        String[] perms = getAnnotationValue(a);
        Subject subject = getSubject();

        if (perms.length == 1) {
            subject.checkPermission(perms[0]);
            return;
        }
        if (Logical.AND.equals(rpAnnotation.logical())) {
            getSubject().checkPermissions(perms);
            return;
        }
        if (Logical.OR.equals(rpAnnotation.logical())) {
            // Avoid processing exceptions unnecessarily - "delay" throwing the exception by calling hasRole first
            boolean hasAtLeastOnePermission = false;
            for (String permission : perms) if (getSubject().isPermitted(permission)) hasAtLeastOnePermission = true;
            // Cause the exception if none of the role match, note that the exception message will be a bit misleading
            if (!hasAtLeastOnePermission) getSubject().checkPermission(perms[0]);
            
        }
    }
}
