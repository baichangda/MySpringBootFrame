package com.bcd.base.support_jdbc.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.Collections;
import java.util.List;

/**
 * Sql 查询字段替换为count(*) 访问器
 */
public class CountSqlReplaceVisitor extends SelectVisitorAdapter {

    private final static List<SelectItem<?>> countSelectItems = Collections.singletonList(getCountExpressionItem());

    private final Statement statement;

    public CountSqlReplaceVisitor(Statement statement) {
        this.statement = statement;
    }

    public static void main(String[] args) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("select count(*),a.id,b.name from A a inner join B b on a.id=b.id");
        new CountSqlReplaceVisitor(statement).parse();
        System.out.println(statement.toString());
    }

    private static SelectItem<Function> getCountExpressionItem() {
        Function function = new Function();
        function.setName("count");
        function.setAllColumns(true);
        SelectItem<Function> item = new SelectItem<>();
        item.setExpression(function);
        return item;
    }

    public void parse() {
        PlainSelect plainSelect = ((Select) statement).getPlainSelect();
        plainSelect.accept(this);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        plainSelect.setSelectItems(countSelectItems);
        super.visit(plainSelect);
    }
}
