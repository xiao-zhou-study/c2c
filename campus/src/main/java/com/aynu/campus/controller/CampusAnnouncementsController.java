package com.aynu.campus.controller;


import com.aynu.campus.domain.dto.CampusAnnouncementsDTO;
import com.aynu.campus.domain.po.CampusAnnouncementsPO;
import com.aynu.campus.service.CampusAnnouncementsService;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "校园公告极简表接口")
@RestController
@RequestMapping("/campus_announcements")
@RequiredArgsConstructor
public class CampusAnnouncementsController {

    private final CampusAnnouncementsService campusAnnouncementsService;

    /**
     * 创建或修改校园公告
     *
     * @param dto 创建或修改的校园公告信息
     */
    @PostMapping("/save")
    public void saveCampusAnnouncements(@RequestBody CampusAnnouncementsDTO dto) {
        campusAnnouncementsService.saveCampusAnnouncements(dto);
    }

    /**
     * 删除校园公告
     *
     * @param id 校园公告ID
     */
    @DeleteMapping("/delete")
    public void deleteCampusAnnouncements(@RequestParam Long id) {
        campusAnnouncementsService.removeById(id);
    }

    /**
     * 分页获取校园公告列表
     *
     * @param pageQuery 分页查询参数
     * @return 校园公告列表
     */
    @GetMapping("/list")
    public PageDTO<CampusAnnouncementsPO> getCampusAnnouncementsList(PageQuery pageQuery,
                                                                     String keyword,
                                                                     Boolean isPublished) {
        return campusAnnouncementsService.getCampusAnnouncementsList(pageQuery, keyword, isPublished);
    }

    /**
     * 根据ID获取校园公告详情
     *
     * @param id 校园公告ID
     * @return 校园公告详情
     */
    @GetMapping("/get")
    public CampusAnnouncementsPO getCampusAnnouncementsById(@RequestParam Long id) {
        return campusAnnouncementsService.getById(id);
    }


}
