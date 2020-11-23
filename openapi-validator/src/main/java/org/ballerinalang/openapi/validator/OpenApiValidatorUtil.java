/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.openapi.validator;

import org.ballerinalang.compiler.CompilerPhase;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;

//import org.ballerinalang.test.util.CompileResult;
//import org.wso2.ballerinalang.compiler.tree.BLangPackage;

//import org.ballerinalang.test.util.CompileResult;
//import org.wso2.ballerinalang.compiler.tree.BLangPackage;

//import org.ballerinalang.test.util.BCompileUtil;
//import org.ballerinalang.test.util.CompileResult;
//import org.wso2.ballerinalang.compiler.tree.BLangPackage;

/**
 * Util class for compilation and format execution for formatting CLI tool.
 */
public class OpenApiValidatorUtil {
    private static EmptyPrintStream emptyPrintStream;

    /**
     * Compile a ballerina file.
     *
     * @param sourceRoot  source root of the file
     * @param packageName package name of the file
     * @return {@link BLangPackage} ballerina package
     * @throws UnsupportedEncodingException throws unsupported encoding exception
     */
//    public static BLangPackage compileFile(Path sourceRoot, String packageName) throws UnsupportedEncodingException {
    // previous method
//        emptyPrintStream = new EmptyPrintStream();
//        CompilerContext context = getCompilerContext(sourceRoot);
    // Set the SourceDirectory to process this compilation as a program directory.
//        context.put(SourceDirectory.class, new FileSystemProgramDirectory(sourceRoot));
//        Compiler compiler = Compiler.getInstance(context);
    // Set an EmptyPrintStream to hide unnecessary outputs from compiler.
//        compiler.setOutStream(emptyPrintStream);
//        return compiler.compile(packageName);

//        CompileResult bCompileUtil = compile(sourceRoot.toString(), packageName);
//        return (BLangPackage) bCompileUtil.getAST();

//        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
//        Path testResourceRoot = Paths.get(resourceRoot, "project-based-tests/src/recordValidation/ballerina" +
//                "/validTests/primitive");
//
//        CompileResult bCompileUtil = BCompileUtil.compile(testResourceRoot.resolve("integerB.bal").toString());
//        return (BLangPackage) bCompileUtil.getAST();
//    }

//    public static CompileResult compileFile01(Path sourceRoot, String packageName) throws
//    UnsupportedEncodingException {
////        emptyPrintStream = new EmptyPrintStream();
////        CompilerContext context = getCompilerContext(sourceRoot);
//        // Set the SourceDirectory to process this compilation as a program directory.
////        context.put(SourceDirectory .class, new FileSystemProgramDirectory(sourceRoot));
////        Compiler compiler = Compiler.getInstance(context);
//        // Set an EmptyPrintStream to hide unnecessary outputs from compiler.
////        compiler.setOutStream(emptyPrintStream);
////        return compiler.compile(packageName);
//
////        Path sourcefilePath = sourceRoot.resolve(packageName);
////        CompileResult bCompileUtil = compile(sourceRoot.toString(), packageName);
////        return (BLangPackage) bCompileUtil.getAST();
//
//        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
//        Path testResourceRoot = Paths.get(resourceRoot,
//        "project-based-tests/recordValidation/ballerina/validTests/primitive");
//
//        CompileResult bCompileUtil = BCompileUtil.compile(testResourceRoot.resolve("integerB.bal").toString());
//        return bCompileUtil;
//    }

    /**
     * Get prepared compiler context.
     *
     * @param sourceRootPath ballerina compilable source root path
     * @return {@link CompilerContext} compiler context
     */
    public static CompilerContext getCompilerContext(Path sourceRootPath) {
        CompilerPhase compilerPhase = CompilerPhase.DEFINE;
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(OFFLINE, Boolean.toString(false));
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(SKIP_TESTS, Boolean.toString(false));
        options.put(TEST_ENABLED, "true");
        options.put(LOCK_ENABLED, Boolean.toString(false));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(true));
        options.put(PRESERVE_WHITESPACE, Boolean.toString(true));

        return context;
    }

    public static String getModuleName(String moduleName) {
        String pattern = Pattern.quote(File.separator);
        String[] splitedTokens = moduleName.split(pattern);
        return splitedTokens[splitedTokens.length - 1];
    }

    /**
     * Empty print stream extending the print stream.
     */
    static class EmptyPrintStream extends PrintStream {
        EmptyPrintStream() throws UnsupportedEncodingException {
            super(new OutputStream() {
                @Override
                public void write(int b) {
                }
            }, true, "UTF-8");
        }
    }
}


