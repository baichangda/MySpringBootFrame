package com.bcd.base.support_jdbc.sql;

import com.bcd.base.exception.BaseRuntimeException;
import lombok.Getter;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sql空值替换访问器
 * 支持的操作符有 = >  <  >=  <=  <>  like  in(?,?,?)  in(:paramList)
 * 支持如下两种替换方式
 * 1、JdbcParameter格式,参数以 ? 方式传递,使用构造方法
 *
 * @see #NullParamSqlReplaceVisitor(Statement, List)
 * 2、JdbcNamedParameter格式,参数以 :param1 方式传递
 * @see #NullParamSqlReplaceVisitor(Statement, Map)
 * <p>
 * 性能方面:
 * 根据sql的复杂程度,sql越复杂,性能越低
 * <p>
 * 线程安全方面:
 * 非线程安全
 */
@Getter
@SuppressWarnings("unchecked")
public class NullParamSqlReplaceVisitor extends ExpressionVisitorAdapter {

    private final static Expression trueExpression = getSqlExpressionForTrue();
    private final static Expression falseExpression = getSqlExpressionForFalse();
    private final Statement statement;
    private final Map<String, Object> paramMap;
    private final Map<String, Object> newParamMap = new LinkedHashMap<>();
    private final List<Object> paramList;
    private final List<Object> newParamList = new ArrayList<>();
    ItemsListVisitorAdapterForMap itemsListVisitorAdapterForMap = new ItemsListVisitorAdapterForMap();
    ItemsListVisitorAdapterForList itemsListVisitorAdapterForList = new ItemsListVisitorAdapterForList();
    //记录每个条件对应的合并符
    Map<Expression, BinaryExpression> expressionToConcat = new HashMap<>();
    private PlainSelect selectBody;

    NullParamSqlReplaceVisitor(Statement statement, Map<String, Object> paramMap) {
        if (statement == null || paramMap == null) {
            throw BaseRuntimeException.getException("Param Can Not Be Null");
        }
        this.statement = statement;
        this.paramMap = paramMap;
        this.paramList = null;
    }

    NullParamSqlReplaceVisitor(Statement statement, List<Object> paramList) {
        if (statement == null || paramList == null) {
            throw BaseRuntimeException.getException("Param Can Not Be Null");
        }
        this.statement = statement;
        this.paramList = paramList;
        this.paramMap = null;
    }

    private static Expression getSqlExpressionForTrue() {
        return new HexValue("1=1");
    }

    private static Expression getSqlExpressionForFalse() {
        return new HexValue("1=0");
    }

    public Statement parse() {
        selectBody = (PlainSelect) ((Select) statement).getSelectBody();
        if (selectBody != null) {
            selectBody.getWhere().accept(this);
        }
        return statement;
    }

    @Override
    public void visit(OrExpression orExpression) {
        expressionToConcat.put(orExpression.getLeftExpression(), orExpression);
        expressionToConcat.put(orExpression.getRightExpression(), orExpression);
        super.visit(orExpression);
    }

    @Override
    public void visit(AndExpression andExpression) {
        expressionToConcat.put(andExpression.getLeftExpression(), andExpression);
        expressionToConcat.put(andExpression.getRightExpression(), andExpression);
        super.visit(andExpression);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        //重写 =
        replaceOrElse(equalsTo, () -> super.visit(equalsTo));
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        //重写 >
        replaceOrElse(greaterThan, () -> super.visit(greaterThan));
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        //重写 >=
        replaceOrElse(greaterThanEquals, () -> super.visit(greaterThanEquals));
    }

    @Override
    public void visit(MinorThan minorThan) {
        //重写 <
        replaceOrElse(minorThan, () -> super.visit(minorThan));
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        //重写 <=
        replaceOrElse(minorThanEquals, () -> super.visit(minorThanEquals));
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        //重写 <>
        replaceOrElse(notEqualsTo, () -> super.visit(notEqualsTo));
    }

    @Override
    public void visit(InExpression inExpression) {
        boolean needReplace = false;
        //重写 in
        ItemsList itemsList = inExpression.getRightItemsList();
        if (paramList != null) {
            //JdbcParameter参数模式的逻辑
            //定义是否是JdbcParameter模式
            int size1 = newParamList.size();
            itemsList.accept(itemsListVisitorAdapterForList);
            int size2 = newParamList.size();
            if (size1 == size2) {
                needReplace = true;
            }
        } else if (paramMap != null) {
            int size1 = newParamMap.size();
            itemsList.accept(itemsListVisitorAdapterForMap);
            int size2 = newParamMap.size();
            if (size1 == size2) {
                needReplace = true;
            }
        }

        if (needReplace) {
            BinaryExpression concat = expressionToConcat.get(inExpression);
            if (concat instanceof AndExpression) {
                if (concat.getRightExpression() == inExpression) {
                    concat.setRightExpression(trueExpression);
                } else {
                    concat.setLeftExpression(trueExpression);
                }
            } else {
                if (concat.getRightExpression() == inExpression) {
                    concat.setRightExpression(falseExpression);
                } else {
                    concat.setLeftExpression(falseExpression);
                }
            }
        } else {
            super.visit(inExpression);
        }
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        //重写 like
        replaceOrElse(likeExpression, () -> super.visit(likeExpression));
    }

    /**
     * 检查每个操作的左右操作符
     * 如果变量参数为null
     * 如果条件左边是and,则用1=1替代此条件
     * 如果条件左边是or,则用1=0替代此条件
     *
     * @param binaryExpression
     * @param runnable
     */
    private void replaceOrElse(BinaryExpression binaryExpression, Runnable runnable) {
        Expression leftExpression = binaryExpression.getLeftExpression();
        Expression rightExpression = binaryExpression.getRightExpression();
        Object param;
        if (paramList != null) {
            if (rightExpression instanceof JdbcParameter) {
                param = paramList.get(((JdbcParameter) rightExpression).getIndex() - 1);
            } else if (leftExpression instanceof JdbcParameter) {
                param = paramList.get(((JdbcParameter) leftExpression).getIndex() - 1);
            } else {
                runnable.run();
                return;
            }
            if (param == null) {
                BinaryExpression concat = expressionToConcat.get(binaryExpression);
                if (concat == null) {
                    //此时说明是where后面只有一个条件、此时直接设置where为null
                    selectBody.setWhere(null);
                } else {
                    if (concat instanceof AndExpression) {
                        if (concat.getRightExpression() == binaryExpression) {
                            concat.setRightExpression(trueExpression);
                        } else {
                            concat.setLeftExpression(trueExpression);
                        }
                    } else {
                        if (concat.getRightExpression() == binaryExpression) {
                            concat.setRightExpression(falseExpression);
                        } else {
                            concat.setLeftExpression(falseExpression);
                        }
                    }
                }
            } else {
                newParamList.add(param);
                runnable.run();
            }
        } else if (paramMap != null) {
            String paramName;
            if (rightExpression instanceof JdbcNamedParameter) {
                paramName = ((JdbcNamedParameter) rightExpression).getName();
            } else if (leftExpression instanceof JdbcNamedParameter) {
                paramName = ((JdbcNamedParameter) leftExpression).getName();
            } else {
                runnable.run();
                return;
            }
            param = paramMap.get(paramName);

            if (param == null) {
                BinaryExpression concat = expressionToConcat.get(binaryExpression);
                if (concat == null) {
                    //此时说明是where后面只有一个条件、此时直接设置where为null
                    selectBody.setWhere(null);
                } else {
                    if (concat instanceof AndExpression) {
                        if (concat.getRightExpression() == binaryExpression) {
                            concat.setRightExpression(trueExpression);
                        } else {
                            concat.setLeftExpression(trueExpression);
                        }
                    } else {
                        if (concat.getRightExpression() == binaryExpression) {
                            concat.setRightExpression(falseExpression);
                        } else {
                            concat.setLeftExpression(falseExpression);
                        }
                    }
                }
            } else {
                newParamMap.put(paramName, param);
                runnable.run();
            }
        }
    }

    class ItemsListVisitorAdapterForList extends ItemsListVisitorAdapter {
        @Override
        public void visit(ExpressionList expressionList) {
            List<Expression> list = expressionList.getExpressions();
            for (int i = 0; i <= list.size() - 1; i++) {
                Expression expression = list.get(i);
                if (expression instanceof JdbcParameter) {
                    Object param = paramList.get(((JdbcParameter) expression).getIndex() - 1);
                    if (param == null) {
                        list.remove(i);
                        i--;
                    } else {
                        newParamList.add(param);
                    }
                }
            }
        }
    }

    class ItemsListVisitorAdapterForMap extends ItemsListVisitorAdapter {
        @Override
        public void visit(ExpressionList expressionList) {
            expressionList.getExpressions().forEach(expression -> expression.accept(new ExpressionVisitorAdapter() {
                @Override
                public void visit(JdbcNamedParameter parameter) {
                    //根据参数名称从map中取出参数,如果为null,则标记参数为空
                    String paramName = parameter.getName();
                    Object param = paramMap.get(paramName);
                    if (param != null) {
                        //此模式下遇到集合参数,排除掉集合中所有为Null的参数,再判断是否为空
                        if (param instanceof List) {
                            int size = ((List) param).size();
                            if (size != 0) {
                                List validList = (List) ((List) param).stream().filter(Objects::nonNull).collect(Collectors.toList());
                                if (!validList.isEmpty()) {
                                    newParamMap.put(paramName, validList);
                                }

                            }
                        } else if (param.getClass().isArray()) {
                            //此模式下遇到数组,排除掉数组中所有为Null的参数,再判断是否为空
                            int len = Array.getLength(param);
                            if (len != 0) {
                                List<Object> validList = new ArrayList<>();
                                for (int i = 0; i < len; i++) {
                                    Object val = Array.get(param, i);
                                    if (val != null) {
                                        validList.add(val);
                                    }
                                }
                                if (!validList.isEmpty()) {
                                    newParamMap.put(paramName, validList);
                                }
                            }
                        }
                    }
                }
            }));
        }
    }
}
