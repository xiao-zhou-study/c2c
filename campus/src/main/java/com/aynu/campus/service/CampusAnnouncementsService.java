package com.aynu.campus.service;

import com.aynu.campus.domain.dto.CampusAnnouncementsDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aynu.campus.domain.po.CampusAnnouncementsPO;

public interface CampusAnnouncementsService extends IService<CampusAnnouncementsPO> {

    void saveCampusAnnouncements(CampusAnnouncementsDTO dto);

    PageDTO<CampusAnnouncementsPO> getCampusAnnouncementsList(PageQuery pageQuery,String keyword,Boolean isPublished);
}
