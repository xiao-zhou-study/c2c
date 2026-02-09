package com.aynu.campus.controller;


import com.aynu.campus.domain.dto.CommentsDTO;
import com.aynu.campus.domain.vo.CommentsVO;
import com.aynu.campus.service.CommentsService;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "话题评论表接口")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentsController {

    private final CommentsService commentsService;

    /**
     * 新增评论
     *
     * @param dto 评论信息
     */
    @PostMapping
    public void addComment(@RequestBody CommentsDTO dto) {
        commentsService.addComment(dto);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete")
    public void deleteComment(@RequestParam Long id) {
        commentsService.removeById(id);
    }

    /**
     * 根据topicId分页获取评论
     *
     * @param pageQuery 分页参数
     * @param topicId   话题ID
     * @return 评论列表
     */
    @GetMapping("/page")
    public PageDTO<CommentsVO> getCommentsByTopicId(PageQuery pageQuery, Long topicId) {
        return commentsService.getCommentsByTopicId(pageQuery, topicId);
    }

}
