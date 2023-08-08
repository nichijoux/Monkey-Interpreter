package com.zh.interpreter;

import com.zh.interpreter.ast.Program;
import com.zh.interpreter.evaluator.Evaluator;
import com.zh.interpreter.lexer.Lexer;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;
import com.zh.interpreter.object.environment.BuiltInEnvironment;
import com.zh.interpreter.object.environment.Environment;
import com.zh.interpreter.parser.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            withoutArguments();
        } else {
            withArguments(args);
        }
    }

    /**
     * 带有参数的解释器用法
     *
     * @param args 参数
     */
    private static void withArguments(String[] args) {
        // 获取参数
        String command = args[0];
        switch (command) {
            case "-h":
                printUsage();
                break;
            case "-c":
                if (args.length < 2) {
                    System.out.println("Missing argument for -c option.");
                    printUsage();
                } else {
                    String filePath = args[1];
                    executeCommandC(filePath, false);
                }
                break;
            case "-cs":
                if (args.length < 2) {
                    System.out.println("Missing argument for -cs option.");
                    printUsage();
                } else {
                    String filePath = args[1];
                    executeCommandC(filePath, true);
                }
                break;
            default:
                System.out.println("Unknown command: " + command);
                printUsage();
                break;
        }
    }

    /**
     * 打印程序的用法
     */
    private static void printUsage() {
        System.out.println("Usage: java -jar Interpreter.jar <command> [<arguments>]");
        System.out.println("Available commands:");
        System.out.println("  -h           Show help");
        System.out.println("  -c           Specifies the source file path");
        System.out.println("  -cs          Specifies the source file path,and display the description of the program");
    }

    /**
     * 执行c命令,即获取文件并解释执行
     *
     * @param path 文件路径
     * @param show 是否显示解析的AST树
     */
    private static void executeCommandC(String path, boolean show) {
        try {
            // 初始化文件及流
            File file = new File(path);
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            // 读取
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            // 初始化词法分析器
            Lexer lexer = new Lexer(builder.toString());
            // 初始化语法分析器
            Parser parser = new Parser(lexer);
            // 解析程序
            Program program = parser.parse();
            if (!parser.getErrors().isEmpty()) {
                parser.getErrors().forEach(System.out::println);
                System.out.println(">>> ");
                return;
            }
            // 解释执行
            Object evaluate = Evaluator.evaluate(program);
            if (evaluate != null && evaluate.getType() != ObjectType.NULL_OBJECT) {
                System.out.println(evaluate);
            }
            if (show) {
                System.out.println("----------final--Program-start--------------");
                System.out.println(program.getNodeDescription());
                System.out.println("----------final--Program--end---------------");
            }
            // 关闭流
            reader.close();
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 边执行边翻译
     */
    private static void withoutArguments() {
        Scanner scanner = new Scanner(System.in);
        Environment environment = new Environment(BuiltInEnvironment.getInstance());
        System.out.print(">>> ");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Lexer lexer = new Lexer(line);
            Parser parser = new Parser(lexer);
            Program program = parser.parse();
            if (!parser.getErrors().isEmpty()) {
                parser.getErrors().forEach(System.out::println);
                System.out.println(">>> ");
                continue;
            }
            Object evaluate = Evaluator.evaluate(program, environment);
            if (evaluate != null && evaluate.getType() != ObjectType.NULL_OBJECT) {
                System.out.println(evaluate);
            }
            System.out.print(">>> ");
        }
    }
}
