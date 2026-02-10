package com.aynu.api.client.item.fallback;

import com.aynu.api.client.item.ItemClient;
import com.aynu.api.dto.item.ItemsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

@Slf4j
public class ItemClientFallback implements FallbackFactory<ItemClient> {

    @Override
    public ItemClient create(Throwable cause) {
        log.error("查询物品服务异常", cause);
        return new ItemClient() {

            @Override
            public ItemsVO getById(Long id) {
                return null;
            }

            @Override
            public List<ItemsVO> listByIds(List<Long> ids) {
                return List.of();
            }
        };
    }
}
