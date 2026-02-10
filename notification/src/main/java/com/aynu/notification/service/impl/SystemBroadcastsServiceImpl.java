package com.aynu.notification.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.utils.UserContext;
import com.aynu.notification.domain.dto.SystemBroadcastsDTO;
import com.aynu.notification.domain.po.SystemBroadcastsPO;
import com.aynu.notification.domain.po.UserBroadcastStatusPO;
import com.aynu.notification.domain.vo.SystemBroadcastsVO;
import com.aynu.notification.enmus.CategoryEnum;
import com.aynu.notification.mapper.SystemBroadcastsMapper;
import com.aynu.notification.mapper.UserBroadcastStatusMapper;
import com.aynu.notification.service.SystemBroadcastsService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemBroadcastsServiceImpl extends ServiceImpl<SystemBroadcastsMapper, SystemBroadcastsPO> implements SystemBroadcastsService {

    private final UserBroadcastStatusMapper userBroadcastStatusMapper;

    @Override
    public void createSystemBroadcasts(SystemBroadcastsDTO dto) {
        SystemBroadcastsPO po;
        if (dto.getId() != null) {
            po = getById(dto.getId());
        } else {
            po = new SystemBroadcastsPO();
            po.setCreatedAt(System.currentTimeMillis());
        }

        BeanUtils.copyProperties(dto, po, "id", "createdAt");

        po.setCategory(CategoryEnum.of(dto.getCategory())
                .getValue());
        po.setUpdatedAt(System.currentTimeMillis());

        saveOrUpdate(po);
    }

    @Override
    public PageDTO<SystemBroadcastsPO> getSystemBroadcastsList(PageQuery pageQuery,
                                                               String title,
                                                               Integer category,
                                                               Boolean isActive,
                                                               Long startTime,
                                                               Long endTime) {
        LambdaQueryChainWrapper<SystemBroadcastsPO> wrapper = lambdaQuery();

        if (StringUtils.hasText(title)) {
            wrapper.like(SystemBroadcastsPO::getTitle, title);
        }

        if (category != null && CategoryEnum.of(category) != null) {
            wrapper.eq(SystemBroadcastsPO::getCategory, category);
        }

        if (isActive != null) {
            wrapper.eq(SystemBroadcastsPO::getIsActive, isActive);
        }

        if (startTime != null) {
            wrapper.ge(SystemBroadcastsPO::getCreatedAt, startTime);
        }

        if (endTime != null) {
            wrapper.le(SystemBroadcastsPO::getCreatedAt, endTime);
        }

        wrapper.eq(SystemBroadcastsPO::getTargetType, 1);

        Page<SystemBroadcastsPO> pageResult = wrapper.page(pageQuery.toMpPage("created_at", false));

        List<SystemBroadcastsPO> records = pageResult.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(pageResult);
        }
        return PageDTO.of(pageResult);
    }

    @Override
    public List<SystemBroadcastsVO> getUserSystemBroadcastsList() {
        Long userId = UserContext.getUser();

        long timestamp = ZonedDateTime.now()
                .minusMonths(3)
                .toInstant()
                .toEpochMilli();

        List<SystemBroadcastsPO> list = lambdaQuery().eq(SystemBroadcastsPO::getIsActive, true)
                .and(wrapper -> wrapper.eq(SystemBroadcastsPO::getTargetType, 1)
                        .or()
                        .eq(SystemBroadcastsPO::getTargetUserId, userId))
                .ge(SystemBroadcastsPO::getCreatedAt, timestamp)
                .list();

        LambdaQueryChainWrapper<UserBroadcastStatusPO> wrapper = new LambdaQueryChainWrapper<>(userBroadcastStatusMapper);
        List<UserBroadcastStatusPO> userReadList = wrapper.eq(UserBroadcastStatusPO::getUserId, userId)
                .list();
        Set<Long> readList = userReadList.stream()
                .map(UserBroadcastStatusPO::getBroadcastId)
                .collect(Collectors.toSet());


        return list.stream()
                .map(record -> SystemBroadcastsVO.builder()
                        .id(record.getId())
                        .title(record.getTitle())
                        .content(record.getContent())
                        .category(record.getCategory())
                        .createdAt(record.getCreatedAt())
                        .isRead(readList.contains(record.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public SystemBroadcastsPO getSystemBroadcastsDetail(Long id) {
        return lambdaQuery().eq(SystemBroadcastsPO::getId, id)
                .one();
    }
}
