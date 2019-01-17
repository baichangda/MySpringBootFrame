package com.bcd.sys.rdb.repository;

import com.bcd.rdb.repository.BaseRepository;
import com.bcd.sys.rdb.bean.MenuBean;
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
