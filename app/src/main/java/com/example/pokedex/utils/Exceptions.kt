package com.example.pokedex.utils

class NonEmptyException(val fieldName: String): IllegalArgumentException("$fieldName: Field cannot be empty.")