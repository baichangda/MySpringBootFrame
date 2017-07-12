package com.base.em.dao;

import com.base.repository.BaseRepository;
import com.base.em.dto.EnumItemBean;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/5/18.
 */
@Repository
public interface EnumItemRepository extends BaseRepository<EnumItemBean,Long> {
}
