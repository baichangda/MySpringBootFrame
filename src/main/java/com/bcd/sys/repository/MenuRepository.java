package com.bcd.sys.repository;

import com.bcd.base.support_jpa.repository.BaseRepository;
import com.bcd.sys.bean.MenuBean;
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
