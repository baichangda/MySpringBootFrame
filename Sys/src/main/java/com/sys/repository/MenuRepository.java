package com.sys.repository;

import com.base.repository.BaseRepository;
import com.sys.bean.MenuBean;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜单操作
 *
 * @author Aaric
 * @since 2017-04-28
 */
@Repository
public interface MenuRepository extends BaseRepository<MenuBean, Long> {
}
