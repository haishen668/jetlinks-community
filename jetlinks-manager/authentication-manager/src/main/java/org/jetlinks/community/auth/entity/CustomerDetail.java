/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hswebframework.web.system.authorization.api.entity.UserEntity
 */
package org.jetlinks.community.auth.entity;

import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.jetlinks.community.auth.entity.UserDetail;

public class CustomerDetail
extends UserEntity {
    private UserDetail userDetail;
    private UserEntity creator;

    public UserDetail getUserDetail() {
        return this.userDetail;
    }

    public UserEntity getCreator() {
        return this.creator;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    public void setCreator(UserEntity creator) {
        this.creator = creator;
    }
}

