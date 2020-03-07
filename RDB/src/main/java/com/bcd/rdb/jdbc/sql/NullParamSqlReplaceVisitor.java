package com.bcd.rdb.jdbc.sql;

import com.bcd.base.exception.BaseRuntimeException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
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
 *
 * 性能方面:
 * 根据sql的复杂程度,sql越复杂,性能越低
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
        super.visit(select);
        select.getSelectBody().accept(new SelectVisitorAdapter(){
            @Override
            public void visit(PlainSelect plainSelect) {
                super.visit(plainSelect);
                //获取条件对象
                Expression where= plainSelect.getWhere();
                //记录每个条件对应的合并符
                Map<Expression,BinaryExpression> expressionToConcat=new HashMap<>();
                ExpressionDeParser parser= new ExpressionDeParser(){
                    @Override
                    public void visit(OrExpression orExpression) {
                        expressionToConcat.put(orExpression.getLeftExpression(),orExpression);
                        expressionToConcat.put(orExpression.getRightExpression(),orExpression);
                        super.visit(orExpression);
                    }

                    @Override
                    public void visit(AndExpression andExpression) {
                        expressionToConcat.put(andExpression.getLeftExpression(),andExpression);
                        expressionToConcat.put(andExpression.getRightExpression(),andExpression);
                        super.visit(andExpression);
                    }

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
                        boolean [] needReplace=new boolean[]{false};
                        //重写 in
                        ItemsList itemsList= inExpression.getRightItemsList();
                        if(paramList!=null){
                            //JdbcParameter参数模式的逻辑
                            //定义是否是JdbcParameter模式
                            itemsList.accept(new ItemsListVisitorAdapter(){
                                @Override
                                public void visit(ExpressionList expressionList) {
                                    List<Expression> list= expressionList.getExpressions();
                                    for(int i=0;i<=list.size()-1;i++){
                                        Expression expression= list.get(i);
                                        if(expression instanceof JdbcParameter){
                                           Object param=paramList.get (((JdbcParameter) expression).getIndex()-1);
                                           if(param==null){
                                               list.remove(i);
                                               i--;
                                           }
                                        }
                                    }
                                    if(list.size()==0){
                                        needReplace[0]=true;
                                    }else{
                                        super.visit(expressionList);
                                    }
                                }
                            });
                        }else if(paramMap!=null){
                            itemsList.accept(new ItemsListVisitorAdapter(){
                                @Override
                                public void visit(ExpressionList expressionList) {
                                    expressionList.getExpressions().forEach(expression->{
                                        expression.accept(new ExpressionVisitorAdapter(){
                                            @Override
                                            public void visit(JdbcNamedParameter parameter) {
                                                //根据参数名称从map中取出参数,如果为null,则标记参数为空
                                                String paramName=parameter.getName();
                                                Object param=paramMap.get(paramName);
                                                if(param==null){
                                                    needReplace[0]=true;
                                                }else{
                                                    //此模式下遇到集合参数,排除掉集合中所有为Null的参数,再判断是否为空
                                                    if(param instanceof List){
                                                        int size=((List) param).size();
                                                        if(size==0){
                                                            needReplace[0]=true;
                                                        }else {
                                                            long count=((List) param).stream().filter(Objects::nonNull).count();
                                                            if(count==0){
                                                                needReplace[0]=true;
                                                            }

                                                        }
                                                    }else if(param.getClass().isArray()){
                                                        //此模式下遇到数组,排除掉数组中所有为Null的参数,再判断是否为空
                                                        int len= Array.getLength(param);
                                                        if(len==0){
                                                            needReplace[0]=true;
                                                        }else{
                                                            List<Object> validList=new ArrayList<>();
                                                            for(int i=0;i<=len-1;i++){
                                                                Object val=Array.get(param,i);
                                                                if(val!=null){
                                                                    validList.add(val);
                                                                }
                                                            }
                                                            if(validList.isEmpty()){
                                                                needReplace[0]=true;
                                                            }
                                                        }
                                                    }
                                                }
                                                if(!needReplace[0]){
                                                    super.visit(parameter);
                                                }
                                            }
                                        });
                                    });
                                    if(!needReplace[0]){
                                        super.visit(expressionList);
                                    }
                                }
                            });
                        }
                        if(needReplace[0]) {
                            BinaryExpression concat= expressionToConcat.get(inExpression);
                            if (concat instanceof AndExpression) {
                                if (concat.getRightExpression() == inExpression) {
                                    concat.setRightExpression(getSqlExpressionForTrue());
                                } else {
                                    concat.setLeftExpression(getSqlExpressionForTrue());
                                }
                            } else {
                                if (concat.getRightExpression() == inExpression) {
                                    concat.setRightExpression(getSqlExpressionForFalse());
                                } else {
                                    concat.setLeftExpression(getSqlExpressionForFalse());
                                }
                            }
                        }else{
                            super.visit(inExpression);
                        }
                    }

                    @Override
                    public void visit(LikeExpression likeExpression) {
                        //重写 like
                        replaceOrElse(likeExpression,()->super.visit(likeExpression));
                    }

                    /**
                     * 检查每个操作的左右操作符
                     * 如果变量参数为null
                     * 如果条件左边是and,则用1=1替代此条件
                     * 如果条件左边是or,则用1=0替代此条件
                     * @param binaryExpression
                     */
                    private void replaceOrElse(BinaryExpression binaryExpression,Runnable runnable){

                        Expression leftExpression= binaryExpression.getLeftExpression();
                        Expression rightExpression= binaryExpression.getRightExpression();
                        Object param;
                        if(paramList!=null){
                            if(rightExpression instanceof JdbcParameter){
                                param=paramList.get(((JdbcParameter) rightExpression).getIndex()-1);
                            }else if(leftExpression instanceof JdbcParameter){
                                param=paramList.get(((JdbcParameter) leftExpression).getIndex()-1);
                            }else{
                                runnable.run();
                                return;
                            }
                        }else if(paramMap != null){
                            if(rightExpression instanceof JdbcNamedParameter){
                                param=paramMap.get(((JdbcNamedParameter) rightExpression).getName());
                            }else if(leftExpression instanceof JdbcNamedParameter){
                                param=paramMap.get(((JdbcNamedParameter) leftExpression).getName());
                            }else{
                                runnable.run();
                                return;
                            }
                        }else{
                            return;
                        }
                        if(param==null){
                            BinaryExpression concat= expressionToConcat.get(binaryExpression);
                            if (concat instanceof AndExpression) {
                                if (concat.getRightExpression() == binaryExpression) {
                                    concat.setRightExpression(getSqlExpressionForTrue());
                                } else {
                                    concat.setLeftExpression(getSqlExpressionForTrue());
                                }
                            } else {
                                if (concat.getRightExpression() == binaryExpression) {
                                    concat.setRightExpression(getSqlExpressionForFalse());
                                } else {
                                    concat.setLeftExpression(getSqlExpressionForFalse());
                                }
                            }
                        }else{
                            runnable.run();
                        }
                    }
                };
                where.accept(parser);
                newSql=plainSelect.toString();
            }
        });
    }

    private static Expression getSqlExpressionForTrue(){
        return new HexValue("1=1");
    }

    private static Expression getSqlExpressionForFalse(){
        return new HexValue("1=0");
    }
}
