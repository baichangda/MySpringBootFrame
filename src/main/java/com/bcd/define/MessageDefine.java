package com.bcd.define;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.ErrorMessage;

/**
 * Created by Administrator on 2017/9/28.
 */
public class ErrorDefine {
   public static ErrorMessage ERROR_DATE_CONVERT_FAILED = ErrorMessage.getMessage(I18NData.getI18NData("DateConvert.convert.FAILED"));

   public static ErrorMessage ERROR_CHANGE_LOCALE= ErrorMessage.getMessage(I18NData.getI18NData("I18NController.changeLocale.FAILED"));
}
