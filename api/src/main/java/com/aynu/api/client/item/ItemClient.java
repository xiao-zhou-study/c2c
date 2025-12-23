package com.aynu.api.client.item;


import com.aynu.api.client.item.fallback.ItemClientFallback;
import com.aynu.api.dto.item.ItemsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "item-service", fallbackFactory = ItemClientFallback.class)
public interface ItemClient {


    /**
     * 获取物品信息及出借人Id
     *
     * @param id 物品id
     * @return 物品信息及出借人Id
     */
    @GetMapping("/items/{id}")
    ItemsVO getById(@PathVariable Long id);

}
