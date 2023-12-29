package dev.romio.remock.matcher


interface PathMatcher {
    fun isPattern(path: String?): Boolean
    fun match(pattern: String?, path: String?): Boolean
    fun matchStart(pattern: String, path: String): Boolean
    fun extractPathWithinPattern(pattern: String, path: String): String
    fun extractUriTemplateVariables(pattern: String, path: String): Map<String, String?>
}