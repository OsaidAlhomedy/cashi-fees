package com.cashi.fees.api.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EnumValueValidator::class])
annotation class EnumValue(
    val enumClass: KClass<out Enum<*>>,
    val message: String = "must be one of {values}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class EnumValueValidator : ConstraintValidator<EnumValue, String?> {

    private var accepted: Set<String>? = null

    override fun initialize(annotation: EnumValue) {
        accepted = annotation.enumClass.java.enumConstants
            .map { it.name }
            .toSet()
    }

    override fun isValid(value: String?, ctx: ConstraintValidatorContext): Boolean {
        if (value == null) return true          // let @NotBlank own nullability
        val candidate = value.uppercase()
        if (accepted?.contains(candidate) == true ) return true

        ctx.disableDefaultConstraintViolation()
        ctx.buildConstraintViolationWithTemplate(
            "must be one of: ${accepted?.joinToString(", ")}"
        ).addConstraintViolation()
        return false
    }
}