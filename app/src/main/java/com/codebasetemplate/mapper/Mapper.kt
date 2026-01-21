package com.codebasetemplate.mapper

interface Mapper<I, O> {
    fun map(model: I): O
}