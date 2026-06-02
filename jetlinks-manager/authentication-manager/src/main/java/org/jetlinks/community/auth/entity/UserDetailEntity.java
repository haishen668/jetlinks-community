/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 *  javax.persistence.Column
 *  javax.persistence.Table
 *  javax.validation.constraints.Email
 *  javax.validation.constraints.NotBlank
 *  org.hibernate.validator.constraints.URL
 *  org.hswebframework.web.api.crud.entity.GenericEntity
 */
package org.jetlinks.community.auth.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.hswebframework.web.api.crud.entity.GenericEntity;

@Table(name="s_user_detail")
public class UserDetailEntity
extends GenericEntity<String> {
    @Column(nullable=false)
    @NotBlank(message="\u59d3\u540d\u4e0d\u80fd\u4e3a\u7a7a")
    private @NotBlank(message="\u59d3\u540d\u4e0d\u80fd\u4e3a\u7a7a") String name;
    @Column(length=2048, name="tree_path")
    @Schema(description="\u521b\u5efa\u4eba\u6811\u7ed3\u6784")
    private String treePath;
    @Column
    @Email(message="\u90ae\u4ef6\u683c\u5f0f\u9519\u8bef")
    private @Email(message="\u90ae\u4ef6\u683c\u5f0f\u9519\u8bef") String email;
    @Column(length=32)
    private String telephone;
    @Column(length=2000)
    @URL(message="\u5934\u50cf\u683c\u5f0f\u9519\u8bef")
    private @URL(message="\u5934\u50cf\u683c\u5f0f\u9519\u8bef") String avatar;
    @Column(length=2000)
    private String description;

    public String getName() {
        return this.name;
    }

    public String getTreePath() {
        return this.treePath;
    }

    public String getEmail() {
        return this.email;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getDescription() {
        return this.description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

