package com.aynu.campus.service;

import com.aynu.campus.domain.dto.CommentsDTO;
import com.aynu.campus.domain.vo.CommentsVO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aynu.campus.domain.po.CommentsPO;

public interface CommentsService extends IService<CommentsPO> {

    void addComment(CommentsDTO dto);

    PageDTO<CommentsVO> getCommentsByTopicId(PageQuery pageQuery, Long topicId);
}
