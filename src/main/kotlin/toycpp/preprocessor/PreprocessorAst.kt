package toycpp.preprocessor

import toycpp.lex.PpToken
import toycpp.lex.Pptok.*
import toycpp.location.SourceLocation
import toycpp.parser.combinators.*
import toycpp.parser.combinators.get

data class PpAstNode(
    val startLocation: SourceLocation
)
data class PreprocessorAst(
    val globalModuleFragmentGroups: List<Group>,
    val moduleName: List<PpToken>,
    val moduleGroups: List<Group>,
    val privateModuleFragmentGroups: List<Group>
)

sealed class Group

sealed class Directive : Group()
data class IncludeDirective(val headerPath: String) : Directive()
data class ImportDirective(val headerPath: String) : Directive()
data class DefineDirective(val name: String, val params: List<String>, val isVariadic: Boolean, val replacement: List<PpToken>)
data class UndefDirective(val name: String)
data class LineDirective(val lineNumber: Int, val filename: String?)
data class ErrorDirective(val givenText: String)
data class PragmaDirective(val givenText: String)

data class IfGroup(val sections: List<IfSection>)
data class IfSection(val condition: IfCondition, val body: List<PpToken>)

sealed class IfCondition
data class IfConstantCondition(val condition: Boolean) : IfCondition()
data class IfDefinedCondition(val name: String) : IfCondition()
data class IfNotDefinedCondition(val name: String) : IfCondition()
object Else : IfCondition()

data class TextLine(val tokens: List<PpToken>) : Group()

val endOfLine = -Newline or endOfInput named "end of line"
val lParen = -one(OpenParen)[{!it.hasLeadingWhitespace}] named "no-space ("

val textLine: PpParser<List<PpToken>> = zeroOrMore(anyTokenExcept(Newline)) then endOfLine named "text line"

val nullDirective = -Pound then endOfLine named "null directive"

