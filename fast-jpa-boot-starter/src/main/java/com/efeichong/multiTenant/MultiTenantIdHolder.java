package com.efeichong.multiTenant;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lxk
 * @date 2020/12/29
 * @description 提供多租户id
 */
public class MultiTenantIdHolder {

    private static final ThreadLocal<Tenant> context = new ThreadLocal<>();

    @Data
    @AllArgsConstructor
    protected static class Tenant {
        private String tenantId;
        private Boolean nonValueGet;

    }

    public static String getTenantId() {
        return context.get() == null ? null : context.get().getTenantId();
    }

    public static void setTenantId(String tenantId) {
        context.set(new Tenant(tenantId, false));
    }

    public static void setTenantId(String tenantId, boolean nonValueGet) {
        context.set(new Tenant(tenantId, nonValueGet));
    }

    public static boolean nonValueGet(){
        return context.get() != null && context.get().getNonValueGet();
    }

    public static void remove() {
        context.remove();
    }

}
