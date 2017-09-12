package com.sys.repository;

import com.bcd.rdb.repository.BaseRepository;
import com.sys.bean.MenuBean;
import org.springframework.stereotype.Repository;


/**
 * 菜单操作
 *
 * @author Aaric
 * @since 2017-04-28
 */
@Repository
public interface MenuRepository extends BaseRepository<MenuBean, Long> {
}
