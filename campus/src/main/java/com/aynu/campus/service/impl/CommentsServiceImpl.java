package com.aynu.campus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.campus.domain.dto.CommentsDTO;
import com.aynu.campus.domain.po.CommentsPO;
import com.aynu.campus.domain.vo.CommentsVO;
import com.aynu.campus.mapper.CommentsMapper;
import com.aynu.campus.service.CommentsService;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.utils.UserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, CommentsPO> implements CommentsService {

    private final UserClient userClient;

    @Override
    public void addComment(CommentsDTO dto) {
        Long userId = UserContext.getUser();

        CommentsPO commentsPO = BeanUtil.toBean(dto, CommentsPO.class);
        commentsPO.setUserId(userId);
        commentsPO.setCreateTime(System.currentTimeMillis());
        save(commentsPO);
    }

    @Override
    public PageDTO<CommentsVO> getCommentsByTopicId(PageQuery pageQuery, Long topicId) {
        Page<CommentsPO> pageResult = lambdaQuery().eq(CommentsPO::getTopicId, topicId)
                .page(pageQuery.toMpPage("create_time", false));
        List<CommentsPO> records = pageResult.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(pageResult);
        }

        Set<Long> userIds = records.stream()
                .map(CommentsPO::getUserId)
                .collect(Collectors.toSet());

        List<UserDTO> userDTOS = new ArrayList<>();
        if (CollUtil.isNotEmpty(userIds)) {
            userDTOS = userClient.queryUserByIds(userIds);
        }

        Map<Long, UserDTO> userMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userDTOS)) {
            userMap = userDTOS.stream()
                    .collect(Collectors.toMap(UserDTO::getId, userDTO -> userDTO));
        }

        Map<Long, UserDTO> finalUserMap = userMap;
        List<CommentsVO> collect = records.stream()
                .map(record -> {
                    UserDTO userDTO = finalUserMap.getOrDefault(record.getUserId(), new UserDTO());
                    CommentsVO commentsVO = BeanUtil.toBean(record, CommentsVO.class);
                    commentsVO.setUsername(userDTO.getUsername());
                    commentsVO.setAvatar(userDTO.getAvatarUrl());
                    return commentsVO;
                })
                .collect(Collectors.toList());

        return PageDTO.of(pageResult, collect);
    }
}
