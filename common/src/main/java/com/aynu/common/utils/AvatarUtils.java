package com.aynu.common.utils;

import cn.hutool.core.util.RandomUtil;

/**
 * 头像工具类
 */
public class AvatarUtils {

    // 获取随机默认头像
    public static String getRandomAvatar() {
        return "https://api.multiavatar.com/" + RandomUtil.randomInt(1, 10) + ".png";
    }
}
