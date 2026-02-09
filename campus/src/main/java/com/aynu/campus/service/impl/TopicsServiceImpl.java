package com.aynu.campus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.campus.domain.dto.TopicsDTO;
import com.aynu.campus.domain.po.CategoriesPO;
import com.aynu.campus.domain.po.TopicsPO;
import com.aynu.campus.domain.vo.CommentsCountVO;
import com.aynu.campus.domain.vo.TopicsVO;
import com.aynu.campus.mapper.CategoriesMapper;
import com.aynu.campus.mapper.CommentsMapper;
import com.aynu.campus.mapper.TopicsMapper;
import com.aynu.campus.service.TopicsService;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.utils.UserContext;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
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
public class TopicsServiceImpl extends ServiceImpl<TopicsMapper, TopicsPO> implements TopicsService {

    private final CategoriesMapper categoriesMapper;

    private final UserClient userClient;

    private final CommentsMapper commentsMapper;


    @Override
    public void addOrUpdateTopic(TopicsDTO dto) {
        Long userId = UserContext.getUser();
        Long id = dto.getId();
        TopicsPO topic = lambdaQuery().eq(TopicsPO::getId, id)
                .one();
        if (topic == null) {
            topic = new TopicsPO();
            topic.setCategoryId(dto.getCategoryId());
            topic.setUserId(userId);
            topic.setTitle(dto.getTitle());
            topic.setContent(dto.getContent());
            topic.setViewCount(0L);
            topic.setCreateTime(System.currentTimeMillis());
        } else {
            topic.setCategoryId(dto.getCategoryId());
            topic.setTitle(dto.getTitle());
            topic.setContent(dto.getContent());
        }
        topic.setUpdateTime(System.currentTimeMillis());
        saveOrUpdate(topic);
    }

    @Override
    public void deleteTopic(Long id) {
        removeById(id);
    }

    @Override
    public PageDTO<TopicsVO> getTopicList(PageQuery pageQuery, String keyword) {
        Page<TopicsPO> pageResult = lambdaQuery().page(pageQuery.toMpPage("create_time", false));

        List<TopicsPO> records = pageResult.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(pageResult);
        }

        Set<Long> categoryIds = records.stream()
                .map(TopicsPO::getCategoryId)
                .collect(Collectors.toSet());

        List<CategoriesPO> categoriesPOList = new ArrayList<>();
        if (CollUtil.isNotEmpty(categoryIds)) {
            LambdaQueryChainWrapper<CategoriesPO> wrapper = new LambdaQueryChainWrapper<>(categoriesMapper);
            categoriesPOList = wrapper.in(CategoriesPO::getId, categoryIds)
                    .list();
        }

        Map<Long, CategoriesPO> categoryMap = new HashMap<>();
        if (CollUtil.isNotEmpty(categoriesPOList)) {
            categoryMap = categoriesPOList.stream()
                    .collect(Collectors.toMap(CategoriesPO::getId, categoriesPO -> categoriesPO));

        }

        Set<Long> userIds = records.stream()
                .map(TopicsPO::getUserId)
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

        Set<Long> topicIds = records.stream()
                .map(TopicsPO::getId)
                .collect(Collectors.toSet());

        List<CommentsCountVO> topicCounts = new ArrayList<>();
        if (CollUtil.isNotEmpty(topicIds)) {
            topicCounts = commentsMapper.getCountByIds(topicIds);
        }

        Map<Long, Integer> topicCountMap = new HashMap<>();
        if (CollUtil.isNotEmpty(topicCounts)) {
            topicCountMap = topicCounts.stream()
                    .collect(Collectors.toMap(CommentsCountVO::getTopicId, CommentsCountVO::getCount));

        }

        Map<Long, CategoriesPO> finalCategoryMap = categoryMap;
        Map<Long, UserDTO> finalUserMap = userMap;
        Map<Long, Integer> finalTopicCountMap = topicCountMap;
        List<TopicsVO> collect = records.stream()
                .map(record -> {
                    CategoriesPO categoriesPO = finalCategoryMap.getOrDefault(record.getCategoryId(),
                            new CategoriesPO());
                    UserDTO userDTO = finalUserMap.getOrDefault(record.getUserId(), new UserDTO());
                    Integer count = finalTopicCountMap.getOrDefault(record.getId(), 0);

                    TopicsVO topicsVO = BeanUtil.toBean(record, TopicsVO.class);
                    topicsVO.setCategoryName(categoriesPO.getName());
                    topicsVO.setUserNickname(userDTO.getUsername());
                    topicsVO.setUserAvatar(userDTO.getAvatarUrl());
                    topicsVO.setCommentCount(count);
                    return topicsVO;
                })
                .collect(Collectors.toList());

        return PageDTO.of(pageResult, collect);
    }

    @Override
    public TopicsVO getTopicDetail(Long id) {
        TopicsPO topic = lambdaQuery().eq(TopicsPO::getId, id)
                .one();

        Long categoryId = topic.getCategoryId();

        CategoriesPO categories = new CategoriesPO();
        LambdaQueryChainWrapper<CategoriesPO> wrapper = new LambdaQueryChainWrapper<>(categoriesMapper);
        categories = wrapper.eq(CategoriesPO::getId, categoryId)
                .one();

        Long userId = topic.getUserId();
        UserDTO userDTO = new UserDTO();
        userDTO = userClient.queryUserById(userId);

        TopicsVO topicsVO = BeanUtil.toBean(topic, TopicsVO.class);
        topicsVO.setCategoryName(categories.getName());
        topicsVO.setUserNickname(userDTO.getUsername());
        topicsVO.setUserAvatar(userDTO.getAvatarUrl());

        return topicsVO;
    }

}
