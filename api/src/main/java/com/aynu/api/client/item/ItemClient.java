package com.aynu.api.client.item;


import com.aynu.api.client.item.fallback.ItemClientFallback;
import com.aynu.api.dto.item.ItemsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "item-service", fallbackFactory = ItemClientFallback.class)
public interface ItemClient {

    /**
     * 根据id查询商品
     *
     * @param id 商品id
     * @return 商品信息
     */
    @GetMapping("/items/{id}")
    ItemsVO getById(@PathVariable Long id);

    @GetMapping("/items/list")
    List<ItemsVO> listByIds(@RequestParam("ids") Iterable<Long> ids);


    @PutMapping("/items/batch/status")
    void batchUpdateStatus(@RequestParam Iterable<Long> ids, @RequestParam Integer status);
}
