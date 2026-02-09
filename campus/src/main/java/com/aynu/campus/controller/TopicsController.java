package com.aynu.campus.controller;


import com.aynu.campus.domain.dto.TopicsDTO;
import com.aynu.campus.domain.vo.TopicsVO;
import com.aynu.campus.service.TopicsService;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "讨论区话题表接口")
@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicsController {

    private final TopicsService topicsService;

    /**
     * 新建或修改话题
     *
     * @param dto 话题DTO
     */
    @PostMapping("/addOrUpdate")
    public void addOrUpdateTopic(@RequestBody TopicsDTO dto) {
        topicsService.addOrUpdateTopic(dto);
    }

    /**
     * 删除话题
     *
     * @param id 话题ID
     */
    @DeleteMapping("/delete")
    public void deleteTopic(Long id) {
        topicsService.deleteTopic(id);
    }

    /**
     * 根据分页获取话题列表
     *
     * @param pageQuery 分页参数
     * @param keyword   关键字
     * @return 话题列表
     */
    @GetMapping("/list")
    public PageDTO<TopicsVO> getTopicList(PageQuery pageQuery, String keyword) {
        return topicsService.getTopicList(pageQuery, keyword);
    }

    /**
     * 根据ID获取话题详情
     *
     * @param id 话题Id
     * @return 话题详情
     */
    @GetMapping("/getTopicDetail")
    public TopicsVO getTopicDetail(Long id) {
        return topicsService.getTopicDetail(id);
    }


}
