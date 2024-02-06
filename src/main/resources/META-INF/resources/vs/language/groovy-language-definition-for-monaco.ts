
import {Monaco} from "@monaco-editor/react";
import {languages} from 'monaco-editor/esm/vs/editor/editor.api'
function getTokens(tokens: string, divider = "|"): string[] {
    return tokens.split(divider);
}

const wordPattern = /(-?\d*\.\d\w*)|([^`~!@#%^&*()\-=+[{\]}\\|;:'",./?\s]+)/g;

const brackets: languages.CharacterPair[] = [
    ["{", "}"],
    ["[", "]"],
    ["(", ")"],
];

const bracketTokens = [
    {
        open: "[",
        close: "]",
        token: "delimiter.square",
    },
    {
        open: "(",
        close: ")",
        token: "delimiter.parenthesis",
    },
    {
        open: "{",
        close: "}",
        token: "delimiter.curly",
    },
];

const autoClosingPairs = [
    { open: "{", close: "}" },
    { open: "[", close: "]" },
    { open: "(", close: ")" },
    { open: '"', close: '"' },
    { open: "'", close: "'" },
    { open: "`", close: "`" },
];

const surroundingPairs = autoClosingPairs;

const id = "groovy";
const label = "Groovy";

export const registerGroovyLanguageForMonaco = (monaco: Monaco) => {
    monaco.languages.register({ id, aliases: [label] });

    monaco.languages.setMonarchTokensProvider(id, {
        brackets: bracketTokens,
        tokenPostfix: ".groovy",
        keywords: getTokens(
            "assert|with|abstract|continue|for|new|switch|assert|default|goto|package|synchronized|boolean|do|if|private|this|break|double|implements|protected|throw|byte|else|import|public|throws|case|enum|instanceof|return|transient|catch|extends|int|short|try|char|final|interface|static|void|class|finally|long|strictfp|volatile|def|float|native|super|while|in|as"
        ),
        typeKeywords: getTokens(
            "Long|Integer|Short|Byte|Double|Number|Float|Character|Boolean|StackTraceElement|Appendable|StringBuffer|Iterable|ThreadGroup|Runnable|Thread|IllegalMonitorStateException|StackOverflowError|OutOfMemoryError|VirtualMachineError|ArrayStoreException|ClassCastException|LinkageError|NoClassDefFoundError|ClassNotFoundException|RuntimeException|Exception|ThreadDeath|Error|Throwable|System|ClassLoader|Cloneable|Class|CharSequence|Comparable|String|Object"
        ),
        constants: getTokens("null|Infinity|NaN|undefined|true|false"),
        builtinFunctions: getTokens(
            "AbstractMethodError|AssertionError|ClassCircularityError|ClassFormatError|Deprecated|EnumConstantNotPresentException|ExceptionInInitializerError|IllegalAccessError|IllegalThreadStateException|InstantiationError|InternalError|NegativeArraySizeException|NoSuchFieldError|Override|Process|ProcessBuilder|SecurityManager|StringIndexOutOfBoundsException|SuppressWarnings|TypeNotPresentException|UnknownError|UnsatisfiedLinkError|UnsupportedClassVersionError|VerifyError|InstantiationException|IndexOutOfBoundsException|ArrayIndexOutOfBoundsException|CloneNotSupportedException|NoSuchFieldException|IllegalArgumentException|NumberFormatException|SecurityException|Void|InheritableThreadLocal|IllegalStateException|InterruptedException|NoSuchMethodException|IllegalAccessException|UnsupportedOperationException|Enum|StrictMath|Package|Compiler|Readable|Runtime|StringBuilder|Math|IncompatibleClassChangeError|NoSuchMethodError|ThreadLocal|RuntimePermission|ArithmeticException|NullPointerException"
        ),
        operators: [
            ".",
            ".&",
            ".@",
            "?.",
            "*",
            "*.",
            "*:",
            "~",
            "!",
            "++",
            "--",
            "**",
            "+",
            "-",
            "*",
            "/",
            "%",
            "<<",
            ">>",
            ">>>",
            "..",
            "..<",
            "<",
            "<=",
            ">",
            ">",
            "==",
            "!=",
            "<=>",
            "===",
            "!==",
            "=~",
            "==~",
            "^",
            "|",
            "&&",
            "||",
            "?",
            ":",
            "?:",
            "=",
            "**=",
            "*=",
            "/=",
            "%=",
            "+=",
            "-=",
            "<<=",
            ">>=",
            ">>>=",
            "&=",
            "^=",
            "|=",
            "?=",
        ],
        symbols: /[=><!~?:&|+\-*/^%]+/,
        escapes: /\\(?:[abfnrtv\\"'`]|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,

        regexpctl: /[(){}[\]$^|\-*+?.]/,
        regexpesc: /\\(?:[bBdDfnrstvwWn0\\/]|@regexpctl|c[A-Z]|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4})/,

        tokenizer: {
            root: [
                { include: "@whitespace" },
                [
                    /\/(?=([^\\/]|\\.)+\/([dgimsuy]*)(\s*)(\.|;|,|\)|\]|\}|$))/,
                    { token: "regexp", bracket: "@open", next: "@regexp" },
                ],
                { include: "@comments" },
                { include: "@numbers" },
                { include: "common" },
                [/[;,.]/, "delimiter"],
                [/[(){}[\]]/, "@brackets"],
                [
                    /[a-zA-Z_$]\w*/,
                    {
                        cases: {
                            "@keywords": "keyword",
                            "@typeKeywords": "type",
                            "@constants": "constant.groovy",
                            "@builtinFunctions": "constant.other.color",
                            "@default": "identifier",
                        },
                    },
                ],
                [
                    /@symbols/,
                    {
                        cases: {
                            "@operators": "operator",
                            "@default": "",
                        },
                    },
                ],
            ],
            common: [
                // delimiters and operators
                [/[()[\]]/, "@brackets"],
                [/[<>](?!@symbols)/, "@brackets"],
                [
                    /@symbols/,
                    {
                        cases: {
                            "@operators": "delimiter",
                            "@default": "",
                        },
                    },
                ],

                [
                    /\/(?=([^\\/]|\\.)+\/([gimsuy]*)(\s*)(\.|;|\/|,|\)|\]|\}|$))/,
                    { token: "regexp", bracket: "@open", next: "@regexp" },
                ],

                // delimiter: after number because of .\d floats
                [/[;,.]/, "delimiter"],

                // strings
                [/"([^"\\]|\\.)*$/, "string.invalid"],
                [/'([^'\\]|\\.)*$/, "string.invalid"],
                [/"/, "string", "@string_double"],
                [/'/, "string", "@string_single"],
            ],
            whitespace: [[/\s+/, "white"]],
            comments: [
                [/\/\/.*/, "comment"],
                [
                    /\/\*/,
                    {
                        token: "comment.quote",
                        next: "@comment",
                    },
                ],
            ],
            comment: [
                [/[^*/]+/, "comment"],
                [
                    /\*\//,
                    {
                        token: "comment.quote",
                        next: "@pop",
                    },
                ],
                [/./, "comment"],
            ],
            commentAnsi: [
                [
                    /\/\*/,
                    {
                        token: "comment.quote",
                        next: "@comment",
                    },
                ],
                [/[^*/]+/, "comment"],
                [
                    /\*\//,
                    {
                        token: "comment.quote",
                        next: "@pop",
                    },
                ],
                [/./, "comment"],
            ],
            numbers: [
                [/[+-]?\d+(?:(?:\.\d*)?(?:[eE][+-]?\d+)?)?f?\b/, "number.float"],
                [/[+-]?(?:0[obx])?\d+(?:u?[lst]?)?\b/, "number"],
            ],
            regexp: [
                [/(\{)(\d+(?:,\d*)?)(\})/, ["regexp.escape.control", "regexp.escape.control", "regexp.escape.control"]],
                [
                    /(\[)(\^?)(?=(?:[^\]\\/]|\\.)+)/,
                    // @ts-ignore
                    ["regexp.escape.control", { token: "regexp.escape.control", next: "@regexrange" }],
                ],
                [/(\()(\?:|\?=|\?!)/, ["regexp.escape.control", "regexp.escape.control"]],
                [/[()]/, "regexp.escape.control"],
                [/@regexpctl/, "regexp.escape.control"],
                [/[^\\/]/, "regexp"],
                [/@regexpesc/, "regexp.escape"],
                [/\\\./, "regexp.invalid"],
                // @ts-ignore
                [/(\/)([gimsuy]*)/, [{ token: "regexp", bracket: "@close", next: "@pop" }, "keyword.other"]],
            ],

            regexrange: [
                [/-/, "regexp.escape.control"],
                [/\^/, "regexp.invalid"],
                [/@regexpesc/, "regexp.escape"],
                [/[^\]]/, "regexp"],
                [/\]/, { token: "regexp.escape.control", next: "@pop", bracket: "@close" }],
            ],
            embedded: [
                [
                    /([^@]|^)([@]{4})*[@]{2}([@]([^@]|$)|[^@]|$)/,
                    {
                        token: "@rematch",
                        next: "@pop",
                        nextEmbedded: "@pop",
                    },
                ],
            ],
            string_double: [
                [/\$\{/, { token: "delimiter.bracket", next: "@bracketCounting" }],
                [/[^\\"$]+/, "string"],
                [/[^\\"]+/, "string"],
                [/@escapes/, "string.escape"],
                [/\\./, "string.escape.invalid"],
                [/"/, "string", "@pop"],
            ],
            string_single: [
                [/[^\\']+/, "string"],
                [/@escapes/, "string.escape"],
                [/\\./, "string.escape.invalid"],
                [/'/, "string", "@pop"],
            ],
            string_backtick: [
                [/\$\{/, { token: "delimiter.bracket", next: "@bracketCounting" }],
                [/[^\\"$]+/, "string"],
                [/@escapes/, "string.escape"],
                [/\\./, "string.escape.invalid"],
                [/"/, "string", "@pop"],
            ],
            bracketCounting: [
                [/\{/, "delimiter.bracket", "@bracketCounting"],
                [/\}/, "delimiter.bracket", "@pop"],
                { include: "common" },
            ],
        },
    });
    monaco.languages.setLanguageConfiguration(id, {
        comments: {
            lineComment: "//",
            blockComment: ["/*", "*/"],
        },
        brackets,
        autoClosingPairs,
        surroundingPairs,
        wordPattern,
    });

    const completionItemProvider: languages.CompletionItemProvider = {
        provideCompletionItems: function (model, position) {
            // 根据当前模型和位置提供自动完成建议
            const word = model.getWordUntilPosition(position);
            const range = {
                startLineNumber: position.lineNumber,
                endLineNumber: position.lineNumber,
                startColumn: word.startColumn,
                endColumn: word.endColumn
            };

            const suggestions: languages.CompletionItem[] = [
                {
                    label: 'println',
                    kind: monaco.languages.CompletionItemKind.Function,
                    insertText: 'println("${1}")',
                    insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                    documentation: '打印一行到控制台',
                    range: range,
                }, {
                    label: 'for',
                    kind: monaco.languages.CompletionItemKind.Keyword,
                    insertText: 'for (int ${1:i} = 0; ${1:i} < ${2:condition}; ${1:i}++) {\n\t${3}\n}',
                    insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                    documentation: 'Groovy for 循环',
                    range: range, // 确保 range 不是 undefined
                },

            ];

            return {suggestions};
        },
        // 可选的方法，用于解析更多关于自动完成项的信息
        resolveCompletionItem: function (item: any) {
            return item;
        }
    };

    monaco.languages.registerCompletionItemProvider('groovy', completionItemProvider);
};