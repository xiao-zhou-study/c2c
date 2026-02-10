package com.aynu.notification.controller;


import com.aynu.notification.service.UserBroadcastStatusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户公告已读状态表接口")
@RestController
@RequestMapping("/user_broadcast_status")
@RequiredArgsConstructor
public class UserBroadcastStatusController {

    private final UserBroadcastStatusService userBroadcastStatusService;

    /**
     * 添加公告为已读
     *
     * @param broadcastIds 公告ID列表
     */
    @PostMapping("/read")
    public void read(@RequestBody List<Long> broadcastIds) {
        userBroadcastStatusService.read(broadcastIds);
    }

}
