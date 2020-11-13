package com.bcd.rdb.jdbc.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;


public class LimitSqlReplaceVisitor extends SelectVisitorAdapter implements StatementParser{

    private Statement statement;
    /**
     * 从0开始
     */
    private int pageNum;
    private int pageSize;

    public LimitSqlReplaceVisitor(Statement statement,int pageNum, int pageSize) {
        this.statement=statement;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    @Override
    public Statement parse() {
        SelectBody selectBody=((Select)statement).getSelectBody();
        selectBody.accept(this);
        return statement;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if(plainSelect.getLimit()==null){
            plainSelect.setLimit(getLimit(pageNum,pageSize));
        }
        super.visit(plainSelect);
    }

    public static void main(String[] args) throws JSQLParserException {
        Statement statement=CCJSqlParserUtil.parse("select count(*),a.id,b.name from A a inner join B b on a.id=b.id limit 1,2");
        new LimitSqlReplaceVisitor(statement,0,100).parse();
        System.out.println(statement.toString());
    }

    private static Limit getLimit(int pageNum,int pageSize){
        Limit limit=new Limit();
        limit.setOffset(new LongValue(pageNum*pageSize));
        limit.setRowCount(new LongValue(pageSize));
        limit.setLimitAll(false);
        limit.setLimitNull(false);
        return limit;
    }
}
