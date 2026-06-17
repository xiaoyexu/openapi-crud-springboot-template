package com.xuxiaoye.api.interfaces;

@FunctionalInterface
public interface OwnerChecker<T> {
    boolean isOwner(T targetId, String owner);
}
