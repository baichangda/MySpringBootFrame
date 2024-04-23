package com.bcd.base.support_spring_exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.bcd.base.result.Result;
import com.bcd.base.util.ExceptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常捕获流程为
 * 1、{@link org.springframework.web.servlet.DispatcherServlet#doDispatch(HttpServletRequest, HttpServletResponse)}
 * 2、{@link org.springframework.web.servlet.DispatcherServlet#processDispatchResult(HttpServletRequest, HttpServletResponse, HandlerExecutionChain, ModelAndView, Exception)}
 * 3、{@link org.springframework.web.servlet.DispatcherServlet#processHandlerException(HttpServletRequest, HttpServletResponse, Object, Exception)}
 * 其中会使用{@link org.springframework.web.servlet.DispatcherServlet#handlerExceptionResolvers}来处理异常
 * 所以需要跟踪如何初始化这个属性
 * 属性初始化过程如下、反推
 * 1、{@link org.springframework.web.servlet.DispatcherServlet#initHandlerExceptionResolvers(ApplicationContext)}
 * 2、{@link org.springframework.web.servlet.DispatcherServlet#initStrategies(ApplicationContext)}
 * 3、{@link org.springframework.web.servlet.DispatcherServlet#refresh()}
 * 其中步骤1中会扫描所有{@link HandlerExceptionResolver}的bean
 * <p>
 * <p>
 * 方法1、定义一个类、继承{@link WebMvcConfigurer}、注册为spring bean、这个方法是替换掉默认的异常解析器
 * 生效过程为
 * 1、{@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration}生效
 * 2、{@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.EnableWebMvcConfiguration#handlerExceptionResolver(ContentNegotiationManager)}
 * 3、{@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.EnableWebMvcConfiguration#configureHandlerExceptionResolvers(List)}
 * 4、{@link org.springframework.web.servlet.config.annotation.WebMvcConfigurerComposite#configureHandlerExceptionResolvers(List)}
 * 其中依赖于变量{@link org.springframework.web.servlet.config.annotation.WebMvcConfigurerComposite#delegates}来源、逆向反推调用过程如下
 * 5、{@link org.springframework.web.servlet.config.annotation.WebMvcConfigurerComposite#addWebMvcConfigurers(List)}
 * 6、{@link org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration#setConfigurers(List)}
 * 此时方法通过{@link org.springframework.beans.factory.annotation.Autowired}依赖注入、寻找其参数{@link WebMvcConfigurer}bean定义
 * 发现只有如下子类加入到spring容器中
 * 7、{@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#configureHandlerExceptionResolvers(List)}
 * 到此结束、其默认实现为空实现
 * 自定义类会在步骤6中被扫描到、并调用其中方法
 * <p>
 * ------------------------------------------------------------------------------------------------------------------------
 * <p>
 * 或者另一种方法、自定义一个类继承{@link AbstractHandlerExceptionResolver}、注册为spring bean、这个方法是新增一个异常解析器并提升优先级
 * 重写{@link AbstractHandlerExceptionResolver#shouldApplyTo(HttpServletRequest, Object)}为 {return true}
 * 重写{@link AbstractHandlerExceptionResolver#getOrder()} 为 {return -1}、因为默认的异常处理器是0、需要自定义的优先级高于它
 * 在http请求时候、会有如下初始化过程
 * 1、{@link org.springframework.web.servlet.DispatcherServlet#onRefresh(ApplicationContext)}
 * 2、{@link org.springframework.web.servlet.DispatcherServlet#initStrategies(ApplicationContext)}
 * 3、{@link org.springframework.web.servlet.DispatcherServlet#initHandlerExceptionResolvers(ApplicationContext)}
 * 其中会扫描出所有{@link HandlerExceptionResolver}子类、并按照{@link org.springframework.core.Ordered}排序
 * 处理器顺序如下
 * {@link org.springframework.boot.web.servlet.error.DefaultErrorAttributes} 不做返回处理、只设置错误信息到attribute、优先级{@link Ordered#HIGHEST_PRECEDENCE}
 * {@link org.springframework.web.servlet.handler.HandlerExceptionResolverComposite} 包含3个子处理器、优先级0
 * {@link org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver} 处理 {@link org.springframework.web.bind.annotation.ExceptionHandler}
 * {@link org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver} 处理 {@link org.springframework.web.bind.annotation.ResponseStatus}
 * {@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver} 处理 spring一些自定义异常
 * {@link AbstractHandlerExceptionResolver}自定义、优先级{@link Ordered#LOWEST_PRECEDENCE}
 */
@Component
@SuppressWarnings("unchecked")
public class MyHandlerExceptionResolver extends AbstractHandlerExceptionResolver {
    private final static Logger logger = LoggerFactory.getLogger(MyHandlerExceptionResolver.class);
    private final HttpMessageConverter<Object> converter;

    public MyHandlerExceptionResolver(MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        this.converter = mappingJackson2HttpMessageConverter;
    }

    @Override
    protected boolean shouldApplyTo(HttpServletRequest request, Object handler) {
        return true;
    }

    @Override
    public int getOrder() {
        /**
         * 小于0即可
         */
        return -1;
    }

    @Override
    public ModelAndView doResolveException(HttpServletRequest request,
                                           HttpServletResponse response, Object object, Exception exception) {
        //1、判断response
        if (response.isCommitted()) {
            return new ModelAndView();
        }
        //2、打印异常
        logger.error("Error", exception);

        //3、使用异常handler处理异常
        try {
            handle(response, exception);
        } catch (IOException e) {
            logger.error("Error", e);
        }
        return new ModelAndView();
    }

    public enum ExceptionCode {
        not_login(401, "请先登陆"),
        arg_error(501, "参数错误、请联系开发人员");
        final int code;
        final String msg;

        ExceptionCode(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

    public void handle(HttpServletResponse response, Throwable throwable) throws IOException {
        Throwable realException = ExceptionUtil.parseException(throwable);
        Result<?> result;
        if (realException instanceof NotLoginException) {
            result = Result.fail(ExceptionCode.not_login.code).message(ExceptionCode.not_login.msg);
        } else if (realException instanceof MethodArgumentNotValidException) {
            final BindingResult bindingResult = ((MethodArgumentNotValidException) realException).getBindingResult();
            final List<ObjectError> allErrors = bindingResult.getAllErrors();
            final List<Map<String, String>> errorList = allErrors.stream().map(e -> {
                Map<String, String> msgMap = new HashMap<>();
                final String defaultMessage = e.getDefaultMessage();
                final String field = ((FieldError) e).getField();
                msgMap.put("field", field);
                msgMap.put("msg", defaultMessage);
                return msgMap;
            }).collect(Collectors.toList());
            result = Result.fail(ExceptionCode.arg_error.code, errorList).message(ExceptionCode.arg_error.msg);
        } else {
            result = Result.from(realException);
        }
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(response);
        converter.write(result,
                MediaType.APPLICATION_JSON,
                servletServerHttpResponse);
    }
}