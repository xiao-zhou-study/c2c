package com.aynu.item.service;

import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.item.domain.dto.ItemsDTO;
import com.aynu.item.domain.po.Items;
import com.aynu.item.domain.vo.ItemsVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 物品信息表，存储租赁物品的核心信息（逻辑外键关联用户/分类表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
public interface IItemsService extends IService<Items> {

    /**
     * 添加物品
     * @param itemsDTO 物品信息DTO
     * @return 物品ID
     */
    Long add(ItemsDTO itemsDTO);

    /**
     * 更新物品信息
     * @param id 物品ID
     * @param itemsDTO 物品信息DTO
     * @return 是否成功
     */
    boolean update(Long id, ItemsDTO itemsDTO);

    /**
     * 删除物品
     * @param id 物品ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 根据ID获取物品详情
     * @param id 物品ID
     * @return 物品详情
     */
    ItemsVO getByIdWithDetail(Long id);

    /**
     * 更新物品状态
     * @param id 物品ID
     * @param status 状态值
     * @param remark 操作备注
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status, String remark);

    /**
     * 批量更新物品状态
     * @param ids 物品ID列表
     * @param status 状态值
     * @param remark 操作备注
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status, String remark);

    /**
     * 根据分类分页查询物品
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param status 状态
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param conditionLevel 成色
     * @param isDeposit 是否需要押金
     * @param location 位置
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageDTO<ItemsVO> listByCategory(String keyword,
                                    Long categoryId,
                                    Long status,
                                    BigDecimal minPrice,
                                    BigDecimal maxPrice,
                                    String conditionLevel,
                                    Boolean isDeposit,
                                    String location,
                                    PageQuery query);

    /**
     * 根据用户ID获取物品列表
     * @param userId 用户ID
     * @return 物品列表
     */
    List<ItemsVO> getByUserId(Long userId);

    /**
     * 获取物品统计信息
     * @return 统计信息
     */
    Object getStats();
}
