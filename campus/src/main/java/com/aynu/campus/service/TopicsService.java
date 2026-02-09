package com.aynu.campus.service;

import cn.hutool.db.PageResult;
import com.aynu.campus.domain.dto.TopicsDTO;
import com.aynu.campus.domain.vo.TopicsVO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aynu.campus.domain.po.TopicsPO;

public interface TopicsService extends IService<TopicsPO> {

    void addOrUpdateTopic(TopicsDTO dto);

    void deleteTopic(Long id);

    PageDTO<TopicsVO> getTopicList(PageQuery pageQuery, String keyword);

    TopicsVO getTopicDetail(Long id);
}
