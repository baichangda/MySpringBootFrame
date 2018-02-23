package com.bcd.define;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.BaseErrorMessage;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2017/9/28.
 */
public class ErrorDefine {
   public static BaseErrorMessage ERROR_DATE_CONVERT_FAILED =BaseErrorMessage.getMessage(I18NData.getI18NData("DateConvert.convert.FAILED"));
}
