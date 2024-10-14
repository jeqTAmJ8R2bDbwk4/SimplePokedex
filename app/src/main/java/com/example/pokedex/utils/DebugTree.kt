package com.example.pokedex.utils
import timber.log.Timber.DebugTree as TimberDebugTree


class DebugTree : TimberDebugTree() {
    protected override fun createStackElementTag(element: StackTraceElement): String? {
        return String.format(
            "[L:%s] [M:%s] [C:%s]",
            element.lineNumber,
            element.methodName,
            super.createStackElementTag(element)
        )
    }
}