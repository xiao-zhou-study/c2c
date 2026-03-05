package com.aynu.user.mq;

import cn.hutool.core.collection.CollUtil;
import com.aynu.common.constants.MqConstants;
import com.aynu.common.domain.dto.UpdateStatsDTO;
import com.aynu.user.service.IUsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserStatsUpdateListener {
    private final IUsersService usersService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.stats.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.USER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.USER_UPDATE_STATS))
    public void listenUserRegister(UpdateStatsDTO updateStatsDTO) {

        if (updateStatsDTO == null) {
            log.error("更新失败");
            return;
        }
        usersService.updateUserStats(updateStatsDTO.getUserId(), updateStatsDTO.getType(), updateStatsDTO.getIsAdd());
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.stats.order.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.USER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.USER_UPDATE_ORDER_STATS))
    public void listenUserOrder(List<UpdateStatsDTO> updateStatsDTOs) {

        if (CollUtil.isEmpty(updateStatsDTOs)) {
            log.error("更新失败");
            return;
        }

        updateStatsDTOs.forEach(this::listenUserRegister);
    }
}
