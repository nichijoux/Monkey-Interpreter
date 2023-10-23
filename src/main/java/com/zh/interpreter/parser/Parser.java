package com.zh.interpreter.parser;

import com.zh.interpreter.ast.Expression;
import com.zh.interpreter.ast.OperatorPriority;
import com.zh.interpreter.ast.Program;
import com.zh.interpreter.ast.Statement;
import com.zh.interpreter.ast.expression.*;
import com.zh.interpreter.ast.expression.literal.*;
import com.zh.interpreter.ast.statement.BlockStatement;
import com.zh.interpreter.ast.statement.ExpressionStatement;
import com.zh.interpreter.ast.statement.LetStatement;
import com.zh.interpreter.ast.statement.ReturnStatement;
import com.zh.interpreter.lexer.Lexer;
import com.zh.interpreter.token.Token;
import com.zh.interpreter.token.TokenType;
import com.zh.interpreter.utils.ReflectUtils;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "DuplicatedCode"})
public class Parser {
    /**
     * 词法解析器
     */
    private final Lexer lexer;

    /**
     * 当前token
     */
    private Token currentToken;

    /**
     * 下一个token
     */
    private Token nextToken;

    /**
     * 错误信息
     */
    private final List<String> errors;

    /**
     * 前缀运算符解析函数映射,前缀运算符接收参数为空,返回值为Expression
     */
    private static final Map<TokenType, Method> prefixParseFunctionMap;

    /**
     * 中缀运算符解析函数映射,中缀运算符接受参数为Expression,返回值为Expression
     * 参数为所解析的中缀运算符左侧的内容
     */
    private static final Map<TokenType, Method> infixParseFunctionMap;

    /**
     * 运算符优先级表
     */
    private static final Map<TokenType, OperatorPriority> priorityMap;

    static {
        prefixParseFunctionMap = new HashMap<>();
        infixParseFunctionMap = new HashMap<>();
        priorityMap = new HashMap<>();
        // 注册优先级表
        priorityMap.put(TokenType.ASSIGN, OperatorPriority.ASSIGN);
        priorityMap.put(TokenType.EQ, OperatorPriority.EQUALS);
        priorityMap.put(TokenType.NEQ, OperatorPriority.EQUALS);
        priorityMap.put(TokenType.LT, OperatorPriority.LESS_OR_GREATER);
        priorityMap.put(TokenType.GT, OperatorPriority.LESS_OR_GREATER);
        priorityMap.put(TokenType.LT_EQ, OperatorPriority.LESS_OR_GREATER);
        priorityMap.put(TokenType.GT_EQ, OperatorPriority.LESS_OR_GREATER);
        priorityMap.put(TokenType.PLUS, OperatorPriority.SUM);
        priorityMap.put(TokenType.PLUS_EQ, OperatorPriority.ASSIGN);
        priorityMap.put(TokenType.MINUS, OperatorPriority.SUM);
        priorityMap.put(TokenType.MINUS_EQ, OperatorPriority.ASSIGN);
        priorityMap.put(TokenType.ASTERISK, OperatorPriority.PRODUCT);
        priorityMap.put(TokenType.ASTERISK_EQ, OperatorPriority.ASSIGN);
        priorityMap.put(TokenType.SLASH, OperatorPriority.PRODUCT);
        priorityMap.put(TokenType.SLASH_EQ, OperatorPriority.ASSIGN);
        priorityMap.put(TokenType.PERCENT, OperatorPriority.PRODUCT);
        priorityMap.put(TokenType.PERCENT_EQ, OperatorPriority.ASSIGN);
        priorityMap.put(TokenType.LPAREN, OperatorPriority.CALL);
        priorityMap.put(TokenType.LBRACKET, OperatorPriority.INDEX);
        priorityMap.put(TokenType.DOT, OperatorPriority.DOT);
        priorityMap.put(TokenType.QUESTION, OperatorPriority.QUESTION);
        // 注册解析函数
        try {
            // 前缀解析函数
            registerPrefixParse(TokenType.IDENTIFIER, Parser.class.getDeclaredMethod("parseIdentifier"));
            registerPrefixParse(TokenType.NULL, Parser.class.getDeclaredMethod("parseNullLiteral"));
            registerPrefixParse(TokenType.INTEGER, Parser.class.getDeclaredMethod("parseIntegerLiteral"));
            registerPrefixParse(TokenType.TRUE, Parser.class.getDeclaredMethod("parseBooleanLiteral"));
            registerPrefixParse(TokenType.FALSE, Parser.class.getDeclaredMethod("parseBooleanLiteral"));
            registerPrefixParse(TokenType.DOUBLE, Parser.class.getDeclaredMethod("parseDoubleLiteral"));
            registerPrefixParse(TokenType.STRING, Parser.class.getDeclaredMethod("parseStringLiteral"));
            registerPrefixParse(TokenType.FUNCTION, Parser.class.getDeclaredMethod("parseFunctionLiteral"));
            registerPrefixParse(TokenType.MACRO, Parser.class.getDeclaredMethod("parseMacroLiteral"));
            registerPrefixParse(TokenType.LBRACKET, Parser.class.getDeclaredMethod("parseArrayLiteral"));
            registerPrefixParse(TokenType.LBRACE, Parser.class.getDeclaredMethod("parseHashLiteral"));
            registerPrefixParse(TokenType.BANG, Parser.class.getDeclaredMethod("parsePrefixExpression"));
            registerPrefixParse(TokenType.PLUS, Parser.class.getDeclaredMethod("parsePrefixExpression"));
            registerPrefixParse(TokenType.MINUS, Parser.class.getDeclaredMethod("parsePrefixExpression"));
            registerPrefixParse(TokenType.LPAREN, Parser.class.getDeclaredMethod("parseGroupExpression"));
            registerPrefixParse(TokenType.IF, Parser.class.getDeclaredMethod("parseIfExpression"));
            registerPrefixParse(TokenType.WHILE, Parser.class.getDeclaredMethod("parseWhileExpression"));
            // 中缀解析函数
            registerInfixParse(TokenType.PLUS, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.PLUS_EQ, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.MINUS, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.MINUS_EQ, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.ASTERISK, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.ASTERISK_EQ, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.SLASH, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.SLASH_EQ, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.PERCENT, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.PERCENT_EQ, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.EQ, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.NEQ, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.LT, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.GT, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.LT_EQ, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.GT_EQ, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.ASSIGN, Parser.class.getDeclaredMethod("parseInfixExpression", Expression.class));
            registerInfixParse(TokenType.LBRACKET, Parser.class.getDeclaredMethod("parseIndexExpression", Expression.class));
            registerInfixParse(TokenType.LPAREN, Parser.class.getDeclaredMethod("parseCallExpression", Expression.class));
            registerInfixParse(TokenType.DOT, Parser.class.getDeclaredMethod("parseDotExpression", Expression.class));
            registerInfixParse(TokenType.QUESTION, Parser.class.getDeclaredMethod("parseTernaryExpression", Expression.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.errors = new ArrayList<>();
        // 读取两个词法单元设置currentToken和nextToken
        advanceToken();
        advanceToken();
    }

    // 读取下一个词法单元
    private void advanceToken() {
        currentToken = nextToken;
        nextToken = lexer.nextToken();
    }

    /**
     * 语法解析器进行代码解析
     *
     * @return 整个程序构建出的语法树
     */
    public Program parse() {
        Program program = new Program();
        while (!currentTokenIs(TokenType.EOF)) {
            Statement statement = parseStatement();
            program.statements.add(statement);
            advanceToken();
        }
        return program;
    }

    /**
     * 一条条解析语句,解析完毕后,当前token应该位于当前语句的最后一个token
     *
     * @return 语句
     */
    private Statement parseStatement() {
        TokenType currentTokenType = currentToken.type;
        // Let语句
        if (Objects.equals(currentTokenType, TokenType.LET)) {
            return parseLetStatement();
        } else if (Objects.equals(currentTokenType, TokenType.RETURN)) {
            return parseReturnStatement();
        }
        return parseExpressionStatement();
    }

    /**
     * 解析let语句
     *
     * @return let语句
     */
    private Statement parseLetStatement() {
        // 初始化let语句
        LetStatement statement = new LetStatement();
        statement.token = currentToken;
        // 判断let后是否跟着标识符
        if (!expectNextToken(TokenType.IDENTIFIER)) {
            return null;
        }
        // 如果是标识符,则创建标识符
        Identifier identifier = new Identifier();
        identifier.token = currentToken;
        identifier.value = currentToken.literal;
        // 设置语句的标识符
        statement.identifier = identifier;
        // 标识符右侧应该跟着等号
        if (!expectNextToken(TokenType.ASSIGN)) {
            return null;
        }
        // 解析右侧表达式
        advanceToken();
        statement.expression = parseExpression(OperatorPriority.LOWEST);
        if (nextTokenIs(TokenType.SEMICOLON)) {
            advanceToken();
        }
        return statement;
    }

    /**
     * 解析返回值语句
     *
     * @return return语句
     */
    private Statement parseReturnStatement() {
        // 初始化return语句
        ReturnStatement statement = new ReturnStatement();
        statement.token = currentToken;
        // 前移token
        advanceToken();
        // 解析return的表达式
        if (currentTokenIs(TokenType.SEMICOLON)) {
            statement.returnValue = NullLiteral.instance;
        } else {
            statement.returnValue = parseExpression(OperatorPriority.LOWEST);
            if (nextTokenIs(TokenType.SEMICOLON)) {
                advanceToken();
            }
        }
        return statement;
    }

    /**
     * 解析表达式语句
     *
     * @return 表达式语句
     */
    private Statement parseExpressionStatement() {
        // 初始化expression语句
        ExpressionStatement statement = new ExpressionStatement();
        statement.token = currentToken;
        // 解析表达式
        statement.expression = parseExpression(OperatorPriority.LOWEST);
        // 分号可选,为分号则前移token
        if (nextTokenIs(TokenType.SEMICOLON)) {
            advanceToken();
        }
        return statement;
    }

    /**
     * 解析表达式,所有解析函数均遵循一个原则:函数在开始解析表达式时,currentToken为所关联的词法单元类型
     * 返回分析的表达式结果时,currentToken为当前表达式中的最后一个词法单元
     * 解析后的表达式AST会产生一个现象:具有较高优先级的运算符表达式位于树中更高的位置,而较低优先级的运算符表达式位于树中较低的位置
     *
     * @param priority 优先级
     * @return 表达式
     */
    private Expression parseExpression(OperatorPriority priority) {
        // 获取前缀函数
        Method prefixFunction = prefixParseFunctionMap.get(currentToken.type);
        if (prefixFunction == null) {
            String error = String.format("no prefix parse function for %s found", currentToken.type);
            errors.add(error);
            return null;
        }
        // 获取前缀函数后调用
        Expression leftExpression = (Expression) ReflectUtils.invokeMethod(this, prefixFunction);
        // 获取中缀函数后调用
        while (!nextTokenIs(TokenType.SEMICOLON) && priority.compareTo(nextTokenPrecedence()) < 0) {
            Method infixMethod = infixParseFunctionMap.get(nextToken.type);
            if (infixMethod != null) {
                advanceToken();
                leftExpression = (Expression) ReflectUtils.invokeMethod(this, infixMethod, leftExpression);
            }
        }
        return leftExpression;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息列表
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * 判断当前token的类型是否为传入类型
     *
     * @param tokenType 所期望的token类型
     * @return 是否为传入类型
     */
    private boolean currentTokenIs(TokenType tokenType) {
        return Objects.equals(currentToken.type, tokenType);
    }

    /**
     * 判断下一个token的类型是否为传入类型
     *
     * @param tokenType 所期望的token类型
     * @return 是否为传入类型
     */
    private boolean nextTokenIs(TokenType tokenType) {
        return Objects.equals(nextToken.type, tokenType);
    }

    /**
     * 获取当前token的优先级
     *
     * @return 优先级
     */
    private OperatorPriority currentTokenPrecedence() {
        return priorityMap.getOrDefault(currentToken.type, OperatorPriority.LOWEST);
    }

    /**
     * 获取下一个token的优先级
     *
     * @return 优先级
     */
    private OperatorPriority nextTokenPrecedence() {
        return priorityMap.getOrDefault(nextToken.type, OperatorPriority.LOWEST);
    }

    /**
     * 判断下一个token的类型是否为传入类型,如果是则进行token前移
     *
     * @param tokenType 所期望的token类型
     * @return 是否为传入类型
     */
    private boolean expectNextToken(TokenType tokenType) {
        if (nextTokenIs(tokenType)) {
            advanceToken();
            return true;
        } else {
            String error = String.format("expected next token is [%s],but get [%s] instead",
                    tokenType, currentToken.type);
            errors.add(error);
            return false;
        }
    }

    /**
     * 注册前缀解析函数
     *
     * @param tokenType token类型
     * @param method    方法
     */
    private static void registerPrefixParse(TokenType tokenType, Method method) {
        prefixParseFunctionMap.put(tokenType, method);
    }

    /**
     * 注册中缀解析函数
     *
     * @param tokenType token类型
     * @param method    方法
     */
    private static void registerInfixParse(TokenType tokenType, Method method) {
        infixParseFunctionMap.put(tokenType, method);
    }

    /**
     * 解析标识符
     *
     * @return 标识符
     */
    private Expression parseIdentifier() {
        Identifier identifier = new Identifier();
        identifier.token = currentToken;
        identifier.value = currentToken.literal;
        return identifier;
    }

    /**
     * 解析空值字面量
     *
     * @return 空值字面量
     */
    private Expression parseNullLiteral() {
        return NullLiteral.instance;
    }

    /**
     * 解析整数字面量
     *
     * @return 整数字面量
     */
    private Expression parseIntegerLiteral() {
        IntegerLiteral integerLiteral = new IntegerLiteral();
        integerLiteral.token = currentToken;
        try {
            integerLiteral.value = (Long.parseLong(currentToken.literal));
        } catch (NumberFormatException e) {
            String error = String.format("could not parse %s as integer", currentToken.literal);
            errors.add(error);
            return null;
        }
        return integerLiteral;
    }

    /**
     * 解析布尔字面量
     *
     * @return 布尔字面量
     */
    private Expression parseBooleanLiteral() {
        BooleanLiteral booleanLiteral = new BooleanLiteral();
        booleanLiteral.token = currentToken;
        booleanLiteral.value = Boolean.parseBoolean(currentToken.literal);
        return booleanLiteral;
    }

    /**
     * 解析浮点数字面量
     *
     * @return 浮点数字面量
     */
    private Expression parseDoubleLiteral() {
        DoubleLiteral doubleLiteral = new DoubleLiteral();
        doubleLiteral.token = currentToken;
        doubleLiteral.value = Double.parseDouble(currentToken.literal);
        return doubleLiteral;
    }

    /**
     * 解析字符串字面量
     *
     * @return 字符串字面量
     */
    private Expression parseStringLiteral() {
        StringLiteral stringLiteral = new StringLiteral();
        stringLiteral.token = currentToken;
        stringLiteral.value = currentToken.literal;
        return stringLiteral;
    }

    /**
     * 解析数组字面量
     *
     * @return 数组字面量
     */
    private Expression parseArrayLiteral() {
        ArrayLiteral arrayLiteral = new ArrayLiteral();
        arrayLiteral.token = currentToken;
        // 当前token应该为[,因此前移token
        advanceToken();
        // 此时开始解析右侧表达式
        while (!currentTokenIs(TokenType.RBRACKET) && !currentTokenIs(TokenType.EOF)) {
            Expression expression = parseExpression(OperatorPriority.LOWEST);
            if (expression != null) {
                arrayLiteral.elements.add(expression);
            }
            advanceToken();
            if (currentTokenIs(TokenType.COMMA)) {
                advanceToken();
            }
        }
        return arrayLiteral;
    }

    /**
     * 解析哈希字面量
     *
     * @return 哈希字面量
     */
    private Expression parseHashLiteral() {
        HashLiteral hashLiteral = new HashLiteral();
        hashLiteral.token = currentToken;
        // 当前token应该为{
        advanceToken();
        // 解析hash内的数据
        while (!currentTokenIs(TokenType.RBRACE) && !currentTokenIs(TokenType.EOF)) {
            // 先解析左侧表达式key
            Expression key = parseExpression(OperatorPriority.LOWEST);
            if (key == null) {
                return null;
            }
            // 下一个token应该为:
            if (!expectNextToken(TokenType.COLON)) {
                return null;
            }
            advanceToken();
            // 再解析右侧表达式value
            Expression value = parseExpression(OperatorPriority.LOWEST);
            if (value == null) {
                return null;
            }
            advanceToken();
            if (currentTokenIs(TokenType.COMMA)) {
                advanceToken();
            }
            // 存储hash字面量
            hashLiteral.hashMap.put(key, value);
        }
        return hashLiteral;
    }

    /**
     * 解析分组表达式
     *
     * @return 分组表达式
     */
    private Expression parseGroupExpression() {
        // 当前token为(
        advanceToken();
        Expression expression = parseExpression(OperatorPriority.LOWEST);
        // 解析完毕后应该为)
        if (!expectNextToken(TokenType.RPAREN)) {
            String error = String.format("expect token is ),but actually token is %s", nextToken.type);
            errors.add(error);
            return null;
        }
        return expression;
    }

    /**
     * 解析语句块
     *
     * @return 语句块
     */
    private BlockStatement parseBlockStatement() {
        // 当前token应该为 {
        advanceToken();
        BlockStatement blockStatement = new BlockStatement();
        blockStatement.token = currentToken;
        List<Statement> statements = blockStatement.statements;
        // 一句一句解析语句
        while (!currentTokenIs(TokenType.RBRACE) && !currentTokenIs(TokenType.EOF)) {
            Statement statement = parseStatement();
            if (statement != null) {
                statements.add(statement);
            }
            advanceToken();
        }
        return blockStatement;
    }

    /**
     * 解析if语句
     *
     * @return if语句
     */
    private Expression parseIfExpression() {
        IfExpression ifExpression = new IfExpression();
        ifExpression.token = currentToken;
        // if后应该跟着 左括号
        if (!expectNextToken(TokenType.LPAREN)) {
            return null;
        }
        advanceToken();
        // 解析条件
        ifExpression.condition = parseExpression(OperatorPriority.LOWEST);
        // 条件后应该跟着 右括号
        if (!expectNextToken(TokenType.RPAREN)) {
            return null;
        }
        if (!expectNextToken(TokenType.LBRACE)) {
            return null;
        }
        // 解析block语句
        ifExpression.consequence = parseBlockStatement();
        // 查询是否跟着else
        if (nextTokenIs(TokenType.ELSE)) {
            // 解析else语句块
            advanceToken();
            if (!expectNextToken(TokenType.LBRACE)) {
                return null;
            }
            ifExpression.alternative = parseBlockStatement();
        }
        return ifExpression;
    }

    /**
     * 解析三元表达式
     *
     * @param condition 条件
     * @return if语句
     */
    private Expression parseTernaryExpression(Expression condition) {
        TernaryExpression ternaryExpression = new TernaryExpression();
        ternaryExpression.token = currentToken;
        ternaryExpression.condition = condition;
        // 此时token应该为?
        advanceToken();
        // 解析true语句
        ternaryExpression.consequence = parseExpression(OperatorPriority.LOWEST);
        if (!expectNextToken(TokenType.COLON)) {
            return null;
        }
        // 解析false语句
        advanceToken();
        ternaryExpression.alternative = parseExpression(OperatorPriority.LOWEST);
        return ternaryExpression;
    }

    /**
     * 解析while语句
     *
     * @return while语句
     */
    private Expression parseWhileExpression() {
        // 初始化while语句
        WhileExpression whileExpression = new WhileExpression();
        whileExpression.token = currentToken;
        // while关键字后应该跟着左括号
        if (!expectNextToken(TokenType.LPAREN)) {
            return null;
        }
        advanceToken();
        // 解析条件
        whileExpression.condition = parseExpression(OperatorPriority.LOWEST);
        // 条件后应该跟着右括号
        if (!expectNextToken(TokenType.RPAREN)) {
            return null;
        }
        if (!expectNextToken(TokenType.LBRACE)) {
            return null;
        }
        // 此时当前token应该为左大括号
        whileExpression.blockStatement = parseBlockStatement();
        return whileExpression;
    }

    /**
     * 解析函数字面量
     *
     * @return 函数表达式
     */
    private Expression parseFunctionLiteral() {
        FunctionLiteral functionLiteral = new FunctionLiteral();
        functionLiteral.token = currentToken;
        // 解析函数表达式,函数后应该紧跟(参数列表)
        if (!expectNextToken(TokenType.LPAREN)) {
            return null;
        }
        // 现在当前token为(,应该解析参数,所谓参数也应该只是标识符列
        advanceToken();
        while (!currentTokenIs(TokenType.RPAREN) && !currentTokenIs(TokenType.EOF)) {
            Expression identifier = parseIdentifier();
            functionLiteral.parameters.add((Identifier) identifier);
            advanceToken();
            if (currentTokenIs(TokenType.COMMA)) {
                advanceToken();
            }
        }
        // 此时当前token应该为),然后解析语句
        if (!expectNextToken(TokenType.LBRACE)) {
            return null;
        }
        functionLiteral.statement = parseBlockStatement();
        return functionLiteral;
    }

    /**
     * 解析宏函数字面量
     *
     * @return 宏函数字面量
     */
    private Expression parseMacroLiteral() {
        MacroLiteral macroLiteral = new MacroLiteral();
        macroLiteral.token = currentToken;
        // 解析函数表达式,函数后应该紧跟(参数列表)
        if (!expectNextToken(TokenType.LPAREN)) {
            return null;
        }
        // 现在当前token为(,应该解析参数,所谓参数也应该只是标识符列
        advanceToken();
        while (!currentTokenIs(TokenType.RPAREN) && !currentTokenIs(TokenType.EOF)) {
            Expression identifier = parseIdentifier();
            macroLiteral.parameters.add((Identifier) identifier);
            advanceToken();
            if (currentTokenIs(TokenType.COMMA)) {
                advanceToken();
            }
        }
        // 此时当前token应该为),然后解析语句
        if (!expectNextToken(TokenType.LBRACE)) {
            return null;
        }
        macroLiteral.statement = parseBlockStatement();
        return macroLiteral;
    }

    /**
     * 解析下标访问表达式
     *
     * @param target 要访问的目标
     * @return 下标访问表达式
     */
    private Expression parseIndexExpression(Expression target) {
        IndexExpression expression = new IndexExpression();
        expression.expression = target;
        expression.token = currentToken;
        // 当前token应该为[
        advanceToken();
        // 解析右侧下标的表达式
        expression.index = parseExpression(OperatorPriority.LOWEST);
        if (!expectNextToken(TokenType.RBRACKET)) {
            return null;
        }
        return expression;
    }

    /**
     * 解析函数调用
     *
     * @return 函数调用表达式
     */
    private Expression parseCallExpression(Expression function) {
        CallExpression callExpression = new CallExpression();
        callExpression.token = currentToken;
        // 解析函数字面量或者标识符,当前token应该为(
        callExpression.function = function;
        advanceToken();
        while (!currentTokenIs(TokenType.RPAREN) && !currentTokenIs(TokenType.SEMICOLON) && !currentTokenIs(TokenType.EOF)) {
            Expression argument = parseExpression(OperatorPriority.LOWEST);
            if (argument != null) {
                callExpression.arguments.add(argument);
            }
            advanceToken();
            if (currentTokenIs(TokenType.COMMA)) {
                advanceToken();
            }
        }
        return callExpression;
    }

    /**
     * 解析.函数调用表达式
     *
     * @param expression 左侧表达式(要访问的目标)
     * @return .函数调用表达式
     */
    private Expression parseDotExpression(Expression expression) {
        // 构造函数表达式
        DotExpression dotExpression = new DotExpression();
        dotExpression.token = currentToken;
        dotExpression.element = expression;
        // 当前token应该为.,下一个token应该为被调用的函数
        if (!expectNextToken(TokenType.IDENTIFIER)) {
            return null;
        }
        // 解析标识符
        dotExpression.function = (Identifier) parseIdentifier();
        if (!expectNextToken(TokenType.LPAREN)) {
            return null;
        }
        advanceToken();
        // 解析参数
        while (!currentTokenIs(TokenType.RPAREN) && !currentTokenIs(TokenType.SEMICOLON) && !currentTokenIs(TokenType.EOF)) {
            Expression argument = parseExpression(OperatorPriority.LOWEST);
            advanceToken();
            if (argument != null) {
                dotExpression.arguments.add(argument);
            }
            if (currentTokenIs(TokenType.COMMA)) {
                advanceToken();
            }
        }
        return dotExpression;
    }

    /**
     * 解析前缀表达式
     *
     * @return 前缀表达式
     */
    private Expression parsePrefixExpression() {
        // 构造前缀表达式
        PrefixExpression prefixExpression = new PrefixExpression();
        prefixExpression.token = currentToken;
        prefixExpression.operator = currentToken.literal;
        // 前移token,查看右侧表达式
        advanceToken();
        prefixExpression.rightExpression = parseExpression(OperatorPriority.PREFIX);
        return prefixExpression;
    }

    /**
     * 解析中缀表达式
     *
     * @return 中缀表达式
     */
    private Expression parseInfixExpression(Expression leftExpression) {
        // 构造中缀表达式
        InfixExpression infixExpression = new InfixExpression();
        infixExpression.token = currentToken;
        infixExpression.operator = currentToken.literal;
        infixExpression.leftExpression = leftExpression;
        // 获取当前token优先级
        OperatorPriority operatorPriority = currentTokenPrecedence();
        advanceToken();
        // 解析右侧表达式
        infixExpression.rightExpression = parseExpression(operatorPriority);
        return infixExpression;
    }
}
