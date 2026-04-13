package com.aynu.order.mapper;

import com.aynu.order.domain.dto.BorrowOrdersCountDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.aynu.order.domain.po.BorrowOrdersPO;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BorrowOrdersMapper extends BaseMapper<BorrowOrdersPO> {

    BigDecimal getTotalAmount();

    BigDecimal getTodayAmount(long todayStartTime, long todayEndTime);

    List<BorrowOrdersCountDTO> getBorrowOrdersCount();

    Boolean existsPendingOrder(Long buyerId, Long itemId);

}
