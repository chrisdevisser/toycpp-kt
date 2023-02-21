package toycpp.preprocessor

import toycpp.lex.PpToken
import toycpp.location.SourceLocation
import kotlin.contracts.contract

sealed class MacroDefinition {
    abstract val startLocation: SourceLocation
    abstract val name: String
    abstract val replacementTokens: List<PpToken>

    fun isObjectLike(): Boolean {
        contract {
            returns(true) implies (this@MacroDefinition is ObjectMacroDefinition)
        }

        return this is ObjectMacroDefinition
    }

    fun isFunctionLike(): Boolean {
        contract {
            returns(true) implies (this@MacroDefinition is FunctionMacroDefinition)
        }

        return this is FunctionMacroDefinition
    }
}

data class ObjectMacroDefinition(
    override val startLocation: SourceLocation,
    override val name: String,
    override val replacementTokens: List<PpToken>
) : MacroDefinition()

data class FunctionMacroDefinition(
    override val startLocation: SourceLocation,
    override val name: String,
    val paramNames: List<String>,
    val isVariadic: Boolean,
    override val replacementTokens: List<PpToken>
) : MacroDefinition()