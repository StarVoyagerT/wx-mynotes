package com.mynotes.contentcenter.domain.entity.content;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "mid_user_share")
@Builder
public class MidUserShare {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * share.id
     */
    @Column(name = "share_id")
    private Integer shareId;

    /**
     * user.id
     */
    @Column(name = "user_id")
    private Integer userId;
}