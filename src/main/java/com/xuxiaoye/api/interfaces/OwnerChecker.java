package com.xuxiaoye.api.interfaces;

@FunctionalInterface
public interface OwnerChecker {
    boolean isOwner(String targetId, String owner);
}
