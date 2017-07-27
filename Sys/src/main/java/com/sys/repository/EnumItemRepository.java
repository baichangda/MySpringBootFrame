package com.sys.repository;

import com.base.repository.BaseRepository;
import com.base.em.bean.EnumItemBean;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/5/18.
 */
@Repository
public interface EnumItemRepository extends BaseRepository<EnumItemBean,Long> {
}
