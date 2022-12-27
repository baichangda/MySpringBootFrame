package com.bcd.sys.bean;

import com.bcd.base.support_jdbc.anno.Table;
import com.bcd.base.support_jdbc.anno.Transient;
import com.bcd.base.support_jdbc.bean.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单表
 */
@Getter
@Setter
@Table("t_sys_menu")
public class MenuBean extends BaseBean {
    //field
    @Schema(description = "父菜单id")
    public Long parentId;

    @NotBlank(message = "[菜单名称]不能为空")
    @Size(max = 50, message = "[菜单名称]长度不能超过50")
    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50)
    public String name;

    @Size(max = 256, message = "[url地址]长度不能超过256")
    @Schema(description = "url地址", maxLength = 256)
    public String url;

    @Size(max = 256, message = "[图标]长度不能超过256")
    @Schema(description = "图标", maxLength = 256)
    public String icon;

    @NotNull(message = "[排序]不能为空")
    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    public Integer orderNum;

    @Transient
    public List<MenuBean> children = new ArrayList<>();
}
