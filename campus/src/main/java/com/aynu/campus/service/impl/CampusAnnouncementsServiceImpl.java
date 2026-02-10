package com.aynu.campus.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.aynu.campus.domain.dto.CampusAnnouncementsDTO;
import com.aynu.campus.domain.po.CampusAnnouncementsPO;
import com.aynu.campus.mapper.CampusAnnouncementsMapper;
import com.aynu.campus.service.CampusAnnouncementsService;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class CampusAnnouncementsServiceImpl extends ServiceImpl<CampusAnnouncementsMapper, CampusAnnouncementsPO> implements CampusAnnouncementsService {

    @Override
    public void saveCampusAnnouncements(CampusAnnouncementsDTO dto) {
        Long id = dto.getId();
        CampusAnnouncementsPO po;
        if (id == null) {
            po = new CampusAnnouncementsPO();
            po.setCreateTime(System.currentTimeMillis());
        } else {
            po = getById(id);
            if (po == null) {
                throw new BadRequestException("记录不存在");
            }
        }
        BeanUtils.copyProperties(dto, po, "id", "createTime");
        po.setUpdateTime(System.currentTimeMillis());
        saveOrUpdate(po);
    }

    @Override
    public PageDTO<CampusAnnouncementsPO> getCampusAnnouncementsList(PageQuery pageQuery,
                                                                     String keyword,
                                                                     Boolean isPublished) {
        Page<CampusAnnouncementsPO> pageResult = lambdaQuery().like(StringUtils.hasText(keyword),
                        CampusAnnouncementsPO::getTitle,
                        keyword)
                .eq(isPublished != null, CampusAnnouncementsPO::getIsPublished, isPublished)
                .page(pageQuery.toMpPage("create_time", false));

        List<CampusAnnouncementsPO> records = pageResult.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(pageResult);
        }


        return PageDTO.of(pageResult);
    }
}
