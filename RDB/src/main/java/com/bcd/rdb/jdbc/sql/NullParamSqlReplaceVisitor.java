package com.bcd.rdb.jdbc.sql;

import com.bcd.base.exception.BaseRuntimeException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Sql空值替换访问器
 * 支持的操作符有 = >  <  >=  <=  <>  like  in(?,?,?)  in(:paramList)
 * 支持如下两种替换方式
 * 1、JdbcParameter格式,参数以 ? 方式传递,使用构造方法
 * @see #NullParamSqlReplaceVisitor(String, List)
 * 2、JdbcNamedParameter格式,参数以 :param1 方式传递
 * @see #NullParamSqlReplaceVisitor(String, Map)
 * 参数中val不支持数组,只支持List
 *
 * 性能方面:
 * 根据sql的复杂程度,sql越复杂,性能越低
 * 1表1条件 10w次 3.5秒
 * 2表3条件 10w次 10s 左右
 * 3表8条件 10w次 13s 左右
 * 8表8条件 10w次 25s 左右
 *
 * 线程安全方面:
 * 非线程安全
 *
 */
@SuppressWarnings("unchecked")
public class NullParamSqlReplaceVisitor extends StatementVisitorAdapter{

    private String sql;

    private Map<String,Object> paramMap;

    private List<Object> paramList;

    private String newSql;

    NullParamSqlReplaceVisitor(String sql, Map<String, Object> paramMap) {
        if(sql==null||paramMap==null){
            throw BaseRuntimeException.getException("Param Can Not Be Null");
        }
        this.sql = sql;
        this.paramMap = paramMap;
        this.paramList=null;
    }

    NullParamSqlReplaceVisitor(String sql, List<Object> paramList) {
        if(sql==null||paramList==null){
            throw BaseRuntimeException.getException("Param Can Not Be Null");
        }
        this.sql=sql;
        this.paramList = paramList;
        this.paramMap=null;
    }

    public String parseSql(){
        try {
            CCJSqlParserUtil.parse(sql).accept(this);
        } catch (JSQLParserException e) {
            throw BaseRuntimeException.getException(e);
        }
        return newSql;
    }

    @Override
    public void visit(Select select) {
        //定义JdbcParameter模式时候 paramList 对应的索引
        int[] paramListIndex=new int[]{0};
        super.visit(select);
        select.getSelectBody().accept(new SelectVisitorAdapter(){
            @Override
            public void visit(PlainSelect plainSelect) {
                super.visit(plainSelect);
                //获取条件对象
                Expression where= plainSelect.getWhere();
                //自定义反解析器解析sql,在解析条件中按照自己的逻辑重新组装where
                ExpressionDeParser parser= new ExpressionDeParser(){
                    @Override
                    public void visit(EqualsTo equalsTo) {
                        //重写 =
                        replaceOrElse(equalsTo,()->super.visit(equalsTo));
                    }

                    @Override
                    public void visit(GreaterThan greaterThan) {
                        //重写 >
                        replaceOrElse(greaterThan,()->super.visit(greaterThan));
                    }

                    @Override
                    public void visit(GreaterThanEquals greaterThanEquals) {
                        //重写 >=
                        replaceOrElse(greaterThanEquals,()->super.visit(greaterThanEquals));
                    }

                    @Override
                    public void visit(MinorThan minorThan) {
                        //重写 <
                        replaceOrElse(minorThan,()->super.visit(minorThan));
                    }

                    @Override
                    public void visit(MinorThanEquals minorThanEquals) {
                        //重写 <=
                        replaceOrElse(minorThanEquals,()->super.visit(minorThanEquals));
                    }

                    @Override
                    public void visit(NotEqualsTo notEqualsTo) {
                        //重写 <>
                        replaceOrElse(notEqualsTo,()->super.visit(notEqualsTo));
                    }

                    @Override
                    public void visit(InExpression inExpression) {
                        //重写 in
                        ItemsList itemsList= inExpression.getRightItemsList();
                        if(paramList!=null){
                            //JdbcParameter参数模式的逻辑
                            //定义是否是JdbcParameter模式
                            boolean[] isJdbcParam=new boolean[]{true};
                            //定义临时in参数集合
                            List<String> inParamList=new ArrayList<>();
                            itemsList.accept(new ItemsListVisitorAdapter(){
                                @Override
                                public void visit(ExpressionList expressionList) {
                                    expressionList.getExpressions().forEach(expression->{
                                        expression.accept(new ExpressionVisitorAdapter(){
                                            @Override
                                            public void visit(JdbcParameter parameter) {
                                                //自己组装in参数集合
                                                Object param=paramList.get(paramListIndex[0]++);
                                                if(param!=null){
                                                    inParamList.add("?");
                                                    super.visit(parameter);
                                                }
                                            }

                                            @Override
                                            public void visit(JdbcNamedParameter parameter) {
                                                isJdbcParam[0]=false;
                                            }
                                        });
                                    });
                                    super.visit(expressionList);
                                }
                            });
                            if(isJdbcParam[0]){
                                //如果是jdbcParam模式,自己拼装in条件
                                if(inParamList.size()>0){
                                    Expression leftExpression= inExpression.getLeftExpression();
                                    leftExpression.accept(new ExpressionVisitorAdapter(){
                                        @Override
                                        public void visit(Column column) {
                                            super.visit(column);
                                            getBuffer().append(column.getColumnName());
                                        }
                                    });
                                    getBuffer().append(" IN (");
                                    getBuffer().append(inParamList.stream().reduce((e1,e2)->e1+","+e2).get());
                                    getBuffer().append(")");
                                }else{
                                    getBuffer().append("1=1");
                                }

                            }else{
                                //其他模式则不进行任何特殊处理
                                super.visit(inExpression);
                            }
                        }else if(paramMap!=null){
                            //JdbcNamedParameter参数模式的逻辑
                            //定义是否是JdbcNamedParameter模式
                            boolean[] isJdbcNamedParam=new boolean[]{true};
                            //定义是否参数为空
                            boolean[] isParamEmpty=new boolean[]{false};
                            itemsList.accept(new ItemsListVisitorAdapter(){
                                @Override
                                public void visit(ExpressionList expressionList) {
                                    expressionList.getExpressions().forEach(expression->{
                                        expression.accept(new ExpressionVisitorAdapter(){
                                            @Override
                                            public void visit(JdbcParameter parameter) {
                                                isJdbcNamedParam[0]=false;
                                            }
                                            @Override
                                            public void visit(JdbcNamedParameter parameter) {
                                                //根据参数名称从map中取出参数,如果为null,则标记参数为空
                                                if(paramMap!=null){
                                                    String paramName=parameter.getName();
                                                    Object param=paramMap.get(paramName);
                                                    if(param==null){
                                                        isParamEmpty[0]=true;
                                                    }else{
                                                        //此模式下遇到集合参数,排除掉集合中所有为Null的参数,再判断是否为空
                                                        if(param instanceof List){
                                                            int size=((List) param).size();
                                                            if(size==0){
                                                                isParamEmpty[0]=true;
                                                            }else {
                                                                long count=((List) param).stream().filter(e->e!=null).count();
                                                                if(count==0){
                                                                    isParamEmpty[0]=true;
                                                                }

                                                            }
                                                        }else if(param.getClass().isArray()){
                                                            //此模式下遇到数组,排除掉数组中所有为Null的参数,再判断是否为空
                                                            int len= Array.getLength(param);
                                                            if(len==0){
                                                                isParamEmpty[0]=true;
                                                            }else{
                                                                List<Object> validList=new ArrayList<>();
                                                                for(int i=0;i<=len-1;i++){
                                                                    Object val=Array.get(param,i);
                                                                    if(val!=null){
                                                                        validList.add(val);
                                                                    }
                                                                }
                                                                if(validList.size()==0){
                                                                    isParamEmpty[0]=true;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                super.visit(parameter);
                                            }
                                        });
                                    });
                                    super.visit(expressionList);
                                }
                            });
                            if(isJdbcNamedParam[0]){
                                if(isParamEmpty[0]){
                                    getBuffer().append("1=1");
                                }else{
                                    super.visit(inExpression);
                                }
                            }else{
                                super.visit(inExpression);
                            }
                        }

                    }

                    @Override
                    public void visit(LikeExpression likeExpression) {
                        //重写 like
                        replaceOrElse(likeExpression,()->super.visit(likeExpression));
                    }

                    /**
                     * 检查每个操作的左右操作符,如果变量参数为null,则用1=1替代此条件
                     * @param binaryExpression
                     * @param runnable
                     */
                    private void replaceOrElse(BinaryExpression binaryExpression, Runnable runnable){
                        Expression leftExpression= binaryExpression.getLeftExpression();
                        Expression rightExpression= binaryExpression.getRightExpression();
                        if(paramList!=null){
                            boolean isLeftParam=leftExpression instanceof JdbcParameter;
                            boolean isRightParam=rightExpression instanceof JdbcParameter;
                            if(isLeftParam ||isRightParam){
                                Object param=paramList.get(paramListIndex[0]++);
                                if(param==null){
                                    getBuffer().append("1=1");
                                    return;
                                }
                            }
                        }else if(paramMap != null){
                            boolean isLeftParam=leftExpression instanceof JdbcNamedParameter;
                            boolean isRightParam=rightExpression instanceof JdbcNamedParameter;
                            if(isLeftParam||isRightParam){
                                String paramName=isLeftParam?((JdbcNamedParameter)leftExpression).getName():((JdbcNamedParameter)rightExpression).getName();
                                Object param=paramMap.get(paramName);
                                if(param==null){
                                    getBuffer().append("1=1");
                                    return;
                                }
                            }
                        }
                        runnable.run();
                    }
                };
                where.accept(parser);

                String allSql=plainSelect.toString();
                String whereSql=where.toString();
                int index=allSql.indexOf(whereSql);
                StringBuilder newSb=new StringBuilder();
                newSb.append(allSql.substring(0,index));
                newSb.append(parser.getBuffer());
                newSb.append(allSql.substring(index+whereSql.length()));
                newSql=newSb.toString();
            }
        });
    }

}
