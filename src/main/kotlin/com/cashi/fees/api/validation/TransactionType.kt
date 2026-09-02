package com.cashi.fees.api.validation

import com.cashi.fees.domain.fees.FeeRuleRegistry
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [KnownTransactionTypeValidator::class])
annotation class KnownTransactionType(
    val message: String = "is not a priced transaction type",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class KnownTransactionTypeValidator(
    private val registry: FeeRuleRegistry,
) : ConstraintValidator<KnownTransactionType, String?> {

    override fun isValid(value: String?, ctx: ConstraintValidatorContext): Boolean {
        if (value == null) return true

        if (runCatching { registry.ruleFor(value) }.isSuccess) return true

        ctx.disableDefaultConstraintViolation()
        ctx.buildConstraintViolationWithTemplate(
            "is not a priced transaction type. Supported: ${registry.supportedTypes().joinToString(", ")}"
        ).addConstraintViolation()
        return false
    }
}