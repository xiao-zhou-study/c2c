package com.aynu.auth.mq;

import com.aynu.api.dto.user.UserDTO;
import com.aynu.auth.service.IUserRolesService;
import com.aynu.common.constants.MqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserChangeListener {

    private final IUserRolesService userRolesService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.register.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.USER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.USER_NEW_KEY))
    public void listenUserRegister(UserDTO dto) {

        if (dto == null || dto.getId() == null) {
            log.error("创建用户失败");
            return;
        }
        userRolesService.addUserRole(dto.getId(),dto.getRole());
    }


}
