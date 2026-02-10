package com.aynu.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.aynu.order.domain.po.BorrowOrdersPO;

@Mapper
public interface BorrowOrdersMapper extends BaseMapper<BorrowOrdersPO> {

}
