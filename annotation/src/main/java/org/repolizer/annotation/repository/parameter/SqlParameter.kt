package org.repolizer.annotation.repository.parameter

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SqlParameter(val key: String)