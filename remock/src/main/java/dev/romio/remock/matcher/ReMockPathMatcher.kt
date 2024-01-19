package dev.romio.remock.matcher

import dev.romio.remock.util.ReMockUtils.tokenizeToStringArray
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.Volatile


internal class ReMockPathMatcher: PathMatcher {

    companion object {
        const val DEFAULT_PATH_SEPARATOR = "/"
        private const val CACHE_TURNOFF_THRESHOLD = 65536

        private val WILDCARD_CHARS = charArrayOf('*', '?', '{')
    }

    private var pathSeparator: String
    private var pathSeparatorPatternCache: PathSeparatorPatternCache
    private var caseSensitive = true
    private var trimTokens = false

    @Volatile
    private var cachePatterns: Boolean? = null
    private val tokenizedPatternCache = ConcurrentHashMap<String, Array<String>>(256)
    val stringMatcherCache = ConcurrentHashMap<String, ReMockPathStringMatcher>(256)

    constructor() {
        this.pathSeparator = DEFAULT_PATH_SEPARATOR
        this.pathSeparatorPatternCache = PathSeparatorPatternCache(DEFAULT_PATH_SEPARATOR)
    }

    constructor(pathSeparator: String) {
        this.pathSeparator = pathSeparator
        this.pathSeparatorPatternCache = PathSeparatorPatternCache(pathSeparator)
    }

    fun setPathSeparator(pathSeparator: String?) {
        this.pathSeparator = pathSeparator ?: DEFAULT_PATH_SEPARATOR
        pathSeparatorPatternCache = PathSeparatorPatternCache(this.pathSeparator)
    }

    fun setCaseSensitive(caseSensitive: Boolean) {
        this.caseSensitive = caseSensitive
    }

    fun setTrimTokens(trimTokens: Boolean) {
        this.trimTokens = trimTokens
    }

    fun setCachePatterns(cachePatterns: Boolean) {
        this.cachePatterns = cachePatterns
    }

    private fun deactivatePatternCache() {
        cachePatterns = false
        tokenizedPatternCache.clear()
        stringMatcherCache.clear()
    }

    override fun isPattern(path: String?): Boolean {
        if(path == null) {
            return false
        }
        var uriVar = false
        for (c in path) {
            if (c == '*' || c == '?') {
                return true
            }
            if (c == '{') {
                uriVar = true
                continue
            }
            if (c == '}' && uriVar) {
                return true
            }
        }
        return false
    }

    override fun match(pattern: String?, path: String?): Boolean {
        return doMatch(pattern, path, true, null)
    }

    override fun matchStart(pattern: String, path: String): Boolean {
        return doMatch(pattern, path, false, null)
    }

    override fun extractPathWithinPattern(pattern: String, path: String): String {
        val patternParts = tokenizeToStringArray(
            pattern,
            pathSeparator, trimTokens, true
        )
        val pathParts = tokenizeToStringArray(
            path,
            pathSeparator, trimTokens, true
        )
        val builder = StringBuilder()
        var pathStarted = false

        var segment = 0
        while (segment < patternParts.size) {
            val patternPart = patternParts[segment]
            if (patternPart.indexOf('*') > -1 || patternPart.indexOf('?') > -1) {
                while (segment < pathParts.size) {
                    if (pathStarted || segment == 0 && !pattern.startsWith(pathSeparator)) {
                        builder.append(pathSeparator)
                    }
                    builder.append(pathParts[segment])
                    pathStarted = true
                    segment++
                }
            }
            segment++
        }

        return builder.toString()
    }

    override fun extractUriTemplateVariables(pattern: String, path: String): Map<String, String?> {
        val variables = LinkedHashMap<String, String?>()
        val result = doMatch(pattern, path, true, variables)
        check(result) { "Pattern \"$pattern\" is not a match for \"$path\"" }
        return variables
    }

    private fun tokenizePattern(pattern: String): Array<String> {
        var tokenized: Array<String>? = null
        val cachePatterns = this.cachePatterns
        if (cachePatterns == null || cachePatterns) {
            tokenized = tokenizedPatternCache[pattern]
        }
        if (tokenized == null) {
            tokenized = tokenizePath(pattern)
            if (cachePatterns == null && tokenizedPatternCache.size >= CACHE_TURNOFF_THRESHOLD) {
                // Try to adapt to the runtime situation that we're encountering:
                // There are obviously too many different patterns coming in here...
                // So let's turn off the cache since the patterns are unlikely to be reoccurring.
                deactivatePatternCache()
                return tokenized
            }
            if (cachePatterns == null || cachePatterns) {
                tokenizedPatternCache.put(pattern, tokenized)
            }
        }
        return tokenized
    }

    private fun getStringMatcher(pattern: String): ReMockPathStringMatcher {
        var matcher: ReMockPathStringMatcher? = null
        val cachePatterns = this.cachePatterns
        if (cachePatterns != false) {
            matcher = stringMatcherCache[pattern]
        }
        if (matcher == null) {
            matcher = ReMockPathStringMatcher(pattern, caseSensitive)
            if (cachePatterns == null && stringMatcherCache.size >= CACHE_TURNOFF_THRESHOLD) {
                // Try to adapt to the runtime situation that we're encountering:
                // There are obviously too many different patterns coming in here...
                // So let's turn off the cache since the patterns are unlikely to be reoccurring.
                deactivatePatternCache()
                return matcher
            }
            if (cachePatterns != false) {
                stringMatcherCache[pattern] = matcher
            }
        }
        return matcher
    }

    private fun doMatch(
        pattern: String?,
        path: String?,
        fullMatch: Boolean,
        uriTemplateVariables: MutableMap<String, String?>?
    ): Boolean {
        if (path == null || path.startsWith(pathSeparator) != pattern?.startsWith(pathSeparator)) {
            return false
        }
        val pattDirs: Array<String> = tokenizePattern(pattern)
        if (fullMatch && caseSensitive && !isPotentialMatch(path, pattDirs)) {
            return false
        }
        val pathDirs: Array<String> = tokenizePath(path)
        var pattIdxStart = 0
        var pattIdxEnd = pattDirs.size - 1
        var pathIdxStart = 0
        var pathIdxEnd = pathDirs.size - 1

        // Match all elements up to the first **
        while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            val pattDir = pattDirs[pattIdxStart]
            if ("**" == pattDir) {
                break
            }
            if (!matchStrings(pattDir, pathDirs[pathIdxStart], uriTemplateVariables)) {
                return false
            }
            pattIdxStart++
            pathIdxStart++
        }
        if (pathIdxStart > pathIdxEnd) {
            // Path is exhausted, only match if rest of pattern is * or **'s
            if (pattIdxStart > pattIdxEnd) {
                return pattern.endsWith(pathSeparator) == path.endsWith(pathSeparator)
            }
            if (!fullMatch) {
                return true
            }
            if (pattIdxStart == pattIdxEnd && pattDirs[pattIdxStart] == "*" && path.endsWith(
                    pathSeparator
                )
            ) {
                return true
            }
            for (i in pattIdxStart..pattIdxEnd) {
                if (pattDirs[i] != "**") {
                    return false
                }
            }
            return true
        } else if (pattIdxStart > pattIdxEnd) {
            // String not exhausted, but pattern is. Failure.
            return false
        } else if (!fullMatch && "**" == pattDirs[pattIdxStart]) {
            // Path start definitely matches due to "**" part in pattern.
            return true
        }

        // up to last '**'
        while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            val pattDir = pattDirs[pattIdxEnd]
            if (pattDir == "**") {
                break
            }
            if (!matchStrings(pattDir, pathDirs[pathIdxEnd], uriTemplateVariables)) {
                return false
            }
            if (pattIdxEnd == pattDirs.size - 1
                && pattern.endsWith(pathSeparator) != path.endsWith(pathSeparator)
            ) {
                return false
            }
            pattIdxEnd--
            pathIdxEnd--
        }
        if (pathIdxStart > pathIdxEnd) {
            // String is exhausted
            for (i in pattIdxStart..pattIdxEnd) {
                if (pattDirs[i] != "**") {
                    return false
                }
            }
            return true
        }
        while (pattIdxStart != pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            var patIdxTmp = -1
            for (i in pattIdxStart + 1..pattIdxEnd) {
                if (pattDirs[i] == "**") {
                    patIdxTmp = i
                    break
                }
            }
            if (patIdxTmp == pattIdxStart + 1) {
                // '**/**' situation, so skip one
                pattIdxStart++
                continue
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            val patLength = patIdxTmp - pattIdxStart - 1
            val strLength = pathIdxEnd - pathIdxStart + 1
            var foundIdx = -1
            strLoop@ for (i in 0..strLength - patLength) {
                for (j in 0 until patLength) {
                    val subPat = pattDirs[pattIdxStart + j + 1]
                    val subStr = pathDirs[pathIdxStart + i + j]
                    if (!matchStrings(subPat, subStr, uriTemplateVariables)) {
                        continue@strLoop
                    }
                }
                foundIdx = pathIdxStart + i
                break
            }
            if (foundIdx == -1) {
                return false
            }
            pattIdxStart = patIdxTmp
            pathIdxStart = foundIdx + patLength
        }
        for (i in pattIdxStart..pattIdxEnd) {
            if (pattDirs[i] != "**") {
                return false
            }
        }
        return true
    }

    private fun isPotentialMatch(path: String, pattDirs: Array<String>): Boolean {
        if (!trimTokens) {
            var pos = 0
            for (pattDir in pattDirs) {
                var skipped = skipSeparator(path, pos, pathSeparator)
                pos += skipped
                skipped = skipSegment(path, pos, pattDir)
                if (skipped < pattDir.length) {
                    return skipped > 0 || pattDir.isNotEmpty() && isWildcardChar(pattDir[0])
                }
                pos += skipped
            }
        }
        return true
    }

    private fun skipSegment(path: String, pos: Int, prefix: String): Int {
        var skipped = 0
        for (c in prefix) {
            if (isWildcardChar(c)) {
                return skipped
            }
            val currPos = pos + skipped
            if (currPos >= path.length) {
                return 0
            }
            if (c == path[currPos]) {
                skipped++
            }
        }
        return skipped
    }

    private fun skipSeparator(path: String, pos: Int, separator: String): Int {
        var skipped = 0
        while (path.startsWith(separator, pos + skipped)) {
            skipped += separator.length
        }
        return skipped
    }

    private fun isWildcardChar(c: Char): Boolean {
        for (candidate in WILDCARD_CHARS) {
            if (c == candidate) {
                return true
            }
        }
        return false
    }

    private fun tokenizePath(path: String?): Array<String> {
        return tokenizeToStringArray(
            path,
            pathSeparator, trimTokens, true
        )
    }

    private fun matchStrings(
        pattern: String, str: String,
        uriTemplateVariables: MutableMap<String, String?>?
    ): Boolean {
        return getStringMatcher(pattern).matchStrings(str, uriTemplateVariables)
    }


    class ReMockPathStringMatcher {

        companion object {
            private val GLOB_PATTERN =
                Pattern.compile("\\?|\\*|\\{((?:\\{[^/]+?\\}|[^/{}]|\\\\[{}])+?)\\}")
            private const val DEFAULT_VARIABLE_PATTERN = "((?s).*)"
        }

        private val rawPattern: String
        private val caseSensitive: Boolean
        private val exactMatch: Boolean
        private val pattern: Pattern?
        private val variableNames = mutableListOf<String>()

        constructor(pattern: String): this(pattern, true)

        constructor(pattern: String, caseSensitive: Boolean) {
            this.rawPattern = pattern
            this.caseSensitive = caseSensitive
            val patternBuilder = StringBuilder()
            val matcher = GLOB_PATTERN.matcher(pattern)
            var end = 0
            while (matcher.find()) {
                patternBuilder.append(quote(pattern, end, matcher.start()))
                val match = matcher.group()
                if ("?" == match) {
                    patternBuilder.append('.')
                } else if ("*" == match) {
                    patternBuilder.append(".*")
                } else if (match.startsWith("{") && match.endsWith("}")) {
                    val colonIdx = match.indexOf(':')
                    if (colonIdx == -1) {
                        patternBuilder.append(DEFAULT_VARIABLE_PATTERN)
                        matcher.group(1)?.let { variableNames.add(it) }
                    } else {
                        val variablePattern = match.substring(colonIdx + 1, match.length - 1)
                        patternBuilder.append('(')
                        patternBuilder.append(variablePattern)
                        patternBuilder.append(')')
                        val variableName = match.substring(1, colonIdx)
                        variableNames.add(variableName)
                    }
                }
                end = matcher.end()
            }
            // No glob pattern was found, this is an exact String match
            if (end == 0) {
                exactMatch = true
                this.pattern = null
            } else {
                exactMatch = false
                patternBuilder.append(quote(pattern, end, pattern.length))
                this.pattern = Pattern.compile(
                    patternBuilder.toString(),
                    Pattern.DOTALL or if (this.caseSensitive) 0 else Pattern.CASE_INSENSITIVE
                )
            }
        }

        private fun quote(s: String, start: Int, end: Int): String {
            return if (start == end) {
                ""
            } else Pattern.quote(s.substring(start, end))
        }

        fun matchStrings(
            str: String,
            uriTemplateVariables: MutableMap<String, String?>?
        ): Boolean {
            if (this.exactMatch) {
                return if (this.caseSensitive)
                    this.rawPattern == str
                else this.rawPattern.equals(str, ignoreCase = true)
            } else if (this.pattern != null) {
                val matcher: Matcher = this.pattern.matcher(str)
                if (matcher.matches()) {
                    if (uriTemplateVariables != null) {
                        if (variableNames.size != matcher.groupCount()) {
                            throw IllegalArgumentException(
                                "The number of capturing groups in the pattern segment " +
                                        this.pattern + " does not match the number of URI template variables it defines, " +
                                        "which can occur if capturing groups are used in a URI template regex. " +
                                        "Use non-capturing groups instead."
                            )
                        }
                        for (i in 1..matcher.groupCount()) {
                            val name = variableNames[i - 1]
                            if (name.startsWith("*")) {
                                throw IllegalArgumentException(
                                    ("Capturing patterns (" + name + ") are not " +
                                            "supported by the ReMockPathMatcher. Use the PathPatternParser instead.")
                                )
                            }
                            val value = matcher.group(i)
                            uriTemplateVariables[name] = value
                        }
                    }
                    return true
                }
            }
            return false
        }
    }

    private class PathSeparatorPatternCache(pathSeparator: String) {
        val endsOnWildCard: String
        val endsOnDoubleWildCard: String

        init {
            endsOnWildCard = "$pathSeparator*"
            endsOnDoubleWildCard = "$pathSeparator**"
        }
    }


}