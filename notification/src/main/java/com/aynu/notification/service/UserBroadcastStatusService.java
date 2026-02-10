package com.aynu.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aynu.notification.domain.po.UserBroadcastStatusPO;

import java.util.List;

public interface UserBroadcastStatusService extends IService<UserBroadcastStatusPO> {

    void read(List<Long> broadcastIds);
}
