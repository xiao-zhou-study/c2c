package com.aynu.notification.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.UserContext;
import com.aynu.notification.domain.po.UserBroadcastStatusPO;
import com.aynu.notification.mapper.UserBroadcastStatusMapper;
import com.aynu.notification.service.UserBroadcastStatusService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserBroadcastStatusServiceImpl extends ServiceImpl<UserBroadcastStatusMapper, UserBroadcastStatusPO> implements UserBroadcastStatusService {

    @Override
    @Transactional
    public void read(List<Long> broadcastIds) {
        if (CollUtil.isEmpty(broadcastIds)) {
            throw new BadRequestException("广播ID列表不能为空");
        }

        Long userId = UserContext.getUser();

        List<UserBroadcastStatusPO> collect = broadcastIds.stream()
                .map(id -> {
                    UserBroadcastStatusPO userBroadcastStatusPO = new UserBroadcastStatusPO();
                    userBroadcastStatusPO.setUserId(userId);
                    userBroadcastStatusPO.setBroadcastId(id);
                    userBroadcastStatusPO.setReadAt(System.currentTimeMillis());

                    return userBroadcastStatusPO;
                })
                .collect(Collectors.toList());
        saveBatch(collect);
    }
}
