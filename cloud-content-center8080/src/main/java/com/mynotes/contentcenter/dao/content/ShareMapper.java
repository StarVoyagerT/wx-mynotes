package com.mynotes.contentcenter.dao.content;

import com.mynotes.contentcenter.domain.entity.content.Share;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ShareMapper extends Mapper<Share> {
    List<Share> selectByTitle(@Param("title") String title);
}