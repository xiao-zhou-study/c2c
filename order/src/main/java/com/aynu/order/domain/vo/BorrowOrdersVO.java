package com.aynu.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BorrowOrdersVO implements Serializable {
    /**
     * 订单 ID（唯一商户单号，主键）
     */
    @Schema(description = "订单 ID（唯一商户单号，主键）")
    private String id;

    /**
     * 关联物品 ID
     */
    @Schema(description = "关联物品 ID")
    private Long itemId;

    /**
     * 物品名称
     */
    @Schema(description = "物品名称")
    private String itemName;

    /**
     * 物品图片Url
     */
    @Schema(description = "物品图片Url")
    private List<String> itemImageUrl;

    /**
     * 买家 ID
     */
    @Schema(description = "买家 ID")
    private Long buyerId;

    /**
     * 买家名称
     */
    @Schema(description = "买家名称")
    private String buyerName;

    /**
     * 买家头像Url
     */
    @Schema(description = "买家头像Url")
    private String buyerAvatarUrl;

    /**
     * 卖家 ID
     */
    @Schema(description = "卖家 ID")
    private Long sellerId;

    /**
     * 卖家名称
     */
    @Schema(description = "卖家名称")
    private String sellerName;

    /**
     * 卖家头像Url
     */
    @Schema(description = "卖家头像Url")
    private String sellerAvatarUrl;

    /**
     * 订单标题快照
     */
    @Schema(description = "订单标题快照")
    private String title;

    /**
     * 价格（元）
     */
    @Schema(description = "价格（元）")
    private BigDecimal price;

    /**
     * 1-待确认，2-待付款，3-交易中/服务中，4-待评价，5-已完成，6-已取消，7-已拒绝，8-争议中
     */
    @Schema(description = "1-待确认，2-待付款，3-交易中/服务中，4-待评价，5-已完成，6-已取消，7-已拒绝，8-争议中")
    private Integer status;

    /**
     * 备注/用途说明
     */
    @Schema(description = "备注/用途说明")
    private String purpose;

    /**
     * 确认时间戳
     */
    @Schema(description = "确认时间戳")
    private Long confirmTime;

    /**
     * 支付成功时间戳
     */
    @Schema(description = "支付成功时间戳")
    private Long payTime;

    /**
     * 第三方支付流水号
     */
    @Schema(description = "第三方支付流水号")
    private String payTradeNo;

    /**
     * 实际交付/开始时间戳
     */
    @Schema(description = "实际交付/开始时间戳")
    private Long borrowTime;

    /**
     * 取消/拒绝原因
     */
    @Schema(description = "取消/拒绝原因")
    private String cancelReason;

    /**
     * 乐观锁版本号
     */
    @Schema(description = "乐观锁版本号")
    private Integer version;

    /**
     * 创建时间戳
     */
    @Schema(description = "创建时间戳")
    private Long createdAt;

    /**
     * 更新时间戳
     */
    @Schema(description = "更新时间戳")
    private Long updatedAt;
}
