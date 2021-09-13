package com.bcd.sys.repository;

import com.bcd.base.support_jpa.repository.BaseRepository;
import com.bcd.sys.bean.MenuBean;
import org.springframework.stereotype.Repository;


@Repository
public interface MenuRepository extends BaseRepository<MenuBean, Long> {
}
