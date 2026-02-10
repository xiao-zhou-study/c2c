package com.aynu.notification.service;

import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.notification.domain.dto.SystemBroadcastsDTO;
import com.aynu.notification.domain.po.SystemBroadcastsPO;
import com.aynu.notification.domain.vo.SystemBroadcastsVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SystemBroadcastsService extends IService<SystemBroadcastsPO> {

    void createSystemBroadcasts(SystemBroadcastsDTO dto);

    PageDTO<SystemBroadcastsPO> getSystemBroadcastsList(PageQuery pageQuery,
                                                        String title,
                                                        Integer category,
                                                        Boolean isActive,
                                                        Long startTime,
                                                        Long endTime);

    List<SystemBroadcastsVO> getUserSystemBroadcastsList();

    SystemBroadcastsPO getSystemBroadcastsDetail(Long id);
}
