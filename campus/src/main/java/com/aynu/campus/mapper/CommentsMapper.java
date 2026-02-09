package com.aynu.campus.mapper;

import com.aynu.campus.domain.vo.CommentsCountVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.aynu.campus.domain.po.CommentsPO;

import java.util.List;
import java.util.Set;

@Mapper
public interface CommentsMapper extends BaseMapper<CommentsPO> {

    List<CommentsCountVO> getCountByIds(Set<Long> topicIds);
}
