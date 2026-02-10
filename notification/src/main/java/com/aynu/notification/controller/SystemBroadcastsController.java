package com.aynu.notification.controller;


import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.notification.domain.dto.SystemBroadcastsDTO;
import com.aynu.notification.domain.po.SystemBroadcastsPO;
import com.aynu.notification.domain.vo.SystemBroadcastsVO;
import com.aynu.notification.service.SystemBroadcastsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "全员广播公告内容表接口")
@RestController
@RequestMapping("/system_broadcasts")
@RequiredArgsConstructor
public class SystemBroadcastsController {

    private final SystemBroadcastsService systemBroadcastsService;

    /**
     * 创建或修改全员广播公告内容
     *
     * @param dto 全员广播公告内容
     */
    @PostMapping("/add")
    public void createSystemBroadcasts(@RequestBody SystemBroadcastsDTO dto) {
        systemBroadcastsService.createSystemBroadcasts(dto);
    }

    /**
     * 删除全员广播公告内容
     *
     * @param id 全员广播公告内容ID
     */
    @DeleteMapping("/delete")
    public void deleteSystemBroadcasts(@RequestParam Long id) {
        systemBroadcastsService.removeById(id);
    }

    /**
     * 管理员分页获取全员广播公告内容列表
     *
     * @param pageQuery 分页查询参数
     * @param title     全员广播公告内容标题
     * @param category  全员广播公告内容类别
     * @param isActive  全员广播公告内容是否有效
     * @param startTime 全员广播公告内容开始时间
     * @param endTime   全员广播公告内容结束时间
     * @return 全员广播公告内容列表
     */
    @GetMapping("/list")
    public PageDTO<SystemBroadcastsPO> getSystemBroadcastsList(PageQuery pageQuery,
                                                               String title,
                                                               Integer category,
                                                               Boolean isActive,
                                                               Long startTime,
                                                               Long endTime) {
        return systemBroadcastsService.getSystemBroadcastsList(pageQuery,
                title,
                category,
                isActive,
                startTime,
                endTime);
    }

    /**
     * 用户获取近三月的通知列表
     *
     * @return 近三月的通知列表
     */
    @GetMapping("/user/list")
    public List<SystemBroadcastsVO> getUserSystemBroadcastsList() {
        return systemBroadcastsService.getUserSystemBroadcastsList();
    }

    /**
     * 查看公告详情
     *
     * @param id 全员广播公告内容ID
     * @return 全员广播公告内容详情
     */
    @GetMapping("/detail")
    public SystemBroadcastsPO getSystemBroadcastsDetail(@RequestParam Long id) {
        return systemBroadcastsService.getSystemBroadcastsDetail(id);
    }

}
