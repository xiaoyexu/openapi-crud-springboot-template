package com.xuxiaoye.api.services.db;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xuxiaoye.api.services.db.dto.entity.User;
import com.xuxiaoye.api.services.db.mapper.UserDBMapper;

public class UserDBService extends ServiceImpl<UserDBMapper, User> {
    public User getUserByAccountNameAndPassword(String username, String password) {
        LambdaUpdateWrapper<User> query = new LambdaUpdateWrapper<User>()
                .eq(User::getAccountName, username)
                .eq(User::getPasswordHash, password);
        return this.getOne(query);
    }

    public User getUserByIdAndRefreshToken(String id, String refreshToken) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query
                .eq(User::getId, id)
                .eq(User::getRefreshToken, refreshToken);
        return this.getOne(query);
    }
}
