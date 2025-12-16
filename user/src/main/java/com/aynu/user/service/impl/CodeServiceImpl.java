package com.aynu.user.service.impl;

import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.RandomUtils;
import com.aynu.common.utils.StringUtils;
import com.aynu.user.service.ICodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.aynu.common.constants.ErrorInfo.Msg.INVALID_VERIFY_CODE;
import static com.aynu.user.constants.UserConstants.USER_VERIFY_CODE_KEY;
import static com.aynu.user.constants.UserConstants.USER_VERIFY_CODE_TTL;

@Slf4j
@Service
public class CodeServiceImpl implements ICodeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendVerifyCode(String phone) {
        String key = USER_VERIFY_CODE_KEY + phone;
        // 1.查看code是否存在
        String code = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(code)) {
            // 2.生成随机验证码
            code = RandomUtils.randomNumbers(4);
            // 3.保存到redis
            stringRedisTemplate.opsForValue().set(USER_VERIFY_CODE_KEY + phone, code, USER_VERIFY_CODE_TTL);

        }
    }

    @Override
    public void verifyCode(String phone, String code) {
        String cacheCode = stringRedisTemplate.opsForValue().get(USER_VERIFY_CODE_KEY + phone);
        if (!StringUtils.equals(cacheCode, code)) {
            // 验证码错误
            throw new BadRequestException(INVALID_VERIFY_CODE);
        }
    }
}
