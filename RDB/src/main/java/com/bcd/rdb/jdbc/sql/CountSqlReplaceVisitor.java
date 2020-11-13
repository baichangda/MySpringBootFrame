package com.bcd.rdb.jdbc.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.Collections;
import java.util.List;

public class CountSqlReplaceVisitor extends SelectVisitorAdapter implements StatementParser{

    private final static List<SelectItem> countSelectItems= Collections.singletonList(getCountExpressionItem());

    private Statement statement;

    public CountSqlReplaceVisitor(Statement statement) {
        this.statement=statement;
    }

    @Override
    public Statement parse(){
        SelectBody selectBody=((Select)statement).getSelectBody();
        selectBody.accept(this);
        return statement;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        plainSelect.setSelectItems(countSelectItems);
        super.visit(plainSelect);
    }

    public static void main(String[] args) throws JSQLParserException {
        Statement statement =CCJSqlParserUtil.parse("select count(*),a.id,b.name from A a inner join B b on a.id=b.id");
        new CountSqlReplaceVisitor(statement).parse();
        System.out.println(statement.toString());
    }

    private static SelectExpressionItem getCountExpressionItem(){
        Function function= new Function();
        function.setName("count");
        function.setAllColumns(true);
        SelectExpressionItem item=new SelectExpressionItem();
        item.setExpression(function);
        return item;
    }
}
