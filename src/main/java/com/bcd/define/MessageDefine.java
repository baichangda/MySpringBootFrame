package com.bcd.define;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.ErrorMessage;
import com.bcd.base.message.Message;

/**
 * Created by Administrator on 2017/9/28.
 */
public class MessageDefine {
   public final static ErrorMessage ERROR_DATE_CONVERT_FAILED = ErrorMessage.getMessage(I18NData.getI18NData("DateConvert.convert.FAILED"));

   public final static ErrorMessage ERROR_CHANGE_LOCALE= ErrorMessage.getMessage(I18NData.getI18NData("I18NController.changeLocale.FAILED"));

   public final static Message SUCCESS_CHANGE_LOCALE= Message.getMessage(I18NData.getI18NData("I18NController.changeLocale.SUCCEEDED"));
}
