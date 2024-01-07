package dev.romio.remock.matcher

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.Test
import java.util.Collections

class ReMockPathMatcherTest {

    private val pathMatcher = ReMockPathMatcher()

    @Test
    fun match() {
        // test exact matching
        assertThat(pathMatcher.match("test", "test")).isTrue()
        assertThat(pathMatcher.match("/test", "/test")).isTrue()
        // SPR-14141
        assertThat(pathMatcher.match("https://example.org", "https://example.org")).isTrue()
        assertThat(pathMatcher.match("/test.jpg", "test.jpg")).isFalse()
        assertThat(pathMatcher.match("test", "/test")).isFalse()
        assertThat(pathMatcher.match("/test", "test")).isFalse()

        // test matching with ?'s
        assertThat(pathMatcher.match("t?st", "test")).isTrue()
        assertThat(pathMatcher.match("??st", "test")).isTrue()
        assertThat(pathMatcher.match("tes?", "test")).isTrue()
        assertThat(pathMatcher.match("te??", "test")).isTrue()
        assertThat(pathMatcher.match("?es?", "test")).isTrue()
        assertThat(pathMatcher.match("tes?", "tes")).isFalse()
        assertThat(pathMatcher.match("tes?", "testt")).isFalse()
        assertThat(pathMatcher.match("tes?", "tsst")).isFalse()

        // test matching with *'s
        assertThat(pathMatcher.match("*", "test")).isTrue()
        assertThat(pathMatcher.match("test*", "test")).isTrue()
        assertThat(pathMatcher.match("test*", "testTest")).isTrue()
        assertThat(pathMatcher.match("test/*", "test/Test")).isTrue()
        assertThat(pathMatcher.match("test/*", "test/t")).isTrue()
        assertThat(pathMatcher.match("test/*", "test/")).isTrue()
        assertThat(pathMatcher.match("*test*", "AnothertestTest")).isTrue()
        assertThat(pathMatcher.match("*test", "Anothertest")).isTrue()
        assertThat(pathMatcher.match("*.*", "test.")).isTrue()
        assertThat(pathMatcher.match("*.*", "test.test")).isTrue()
        assertThat(pathMatcher.match("*.*", "test.test.test")).isTrue()
        assertThat(pathMatcher.match("test*aaa", "testblaaaa")).isTrue()
        assertThat(pathMatcher.match("test*", "tst")).isFalse()
        assertThat(pathMatcher.match("test*", "tsttest")).isFalse()
        assertThat(pathMatcher.match("test*", "test/")).isFalse()
        assertThat(pathMatcher.match("test*", "test/t")).isFalse()
        assertThat(pathMatcher.match("test/*", "test")).isFalse()
        assertThat(pathMatcher.match("*test*", "tsttst")).isFalse()
        assertThat(pathMatcher.match("*test", "tsttst")).isFalse()
        assertThat(pathMatcher.match("*.*", "tsttst")).isFalse()
        assertThat(pathMatcher.match("test*aaa", "test")).isFalse()
        assertThat(pathMatcher.match("test*aaa", "testblaaab")).isFalse()

        // test matching with ?'s and /'s
        assertThat(pathMatcher.match("/?", "/a")).isTrue()
        assertThat(pathMatcher.match("/?/a", "/a/a")).isTrue()
        assertThat(pathMatcher.match("/a/?", "/a/b")).isTrue()
        assertThat(pathMatcher.match("/??/a", "/aa/a")).isTrue()
        assertThat(pathMatcher.match("/a/??", "/a/bb")).isTrue()
        assertThat(pathMatcher.match("/?", "/a")).isTrue()

        // test matching with **'s
        assertThat(pathMatcher.match("/**", "/testing/testing")).isTrue()
        assertThat(pathMatcher.match("/*/**", "/testing/testing")).isTrue()
        assertThat(pathMatcher.match("/**/*", "/testing/testing")).isTrue()
        assertThat(pathMatcher.match("/bla/**/bla", "/bla/testing/testing/bla")).isTrue()
        assertThat(pathMatcher.match("/bla/**/bla", "/bla/testing/testing/bla/bla")).isTrue()
        assertThat(pathMatcher.match("/**/test", "/bla/bla/test")).isTrue()
        assertThat(pathMatcher.match("/bla/**/**/bla", "/bla/bla/bla/bla/bla/bla")).isTrue()
        assertThat(pathMatcher.match("/bla*bla/test", "/blaXXXbla/test")).isTrue()
        assertThat(pathMatcher.match("/*bla/test", "/XXXbla/test")).isTrue()
        assertThat(pathMatcher.match("/bla*bla/test", "/blaXXXbl/test")).isFalse()
        assertThat(pathMatcher.match("/*bla/test", "XXXblab/test")).isFalse()
        assertThat(pathMatcher.match("/*bla/test", "XXXbl/test")).isFalse()
        assertThat(pathMatcher.match("/????", "/bala/bla")).isFalse()
        assertThat(pathMatcher.match("/**/*bla", "/bla/bla/bla/bbb")).isFalse()
        assertThat(
            pathMatcher.match(
                "/*bla*/**/bla/**",
                "/XXXblaXXXX/testing/testing/bla/testing/testing/"
            )
        ).isTrue()
        assertThat(
            pathMatcher.match(
                "/*bla*/**/bla/*",
                "/XXXblaXXXX/testing/testing/bla/testing"
            )
        ).isTrue()
        assertThat(
            pathMatcher.match(
                "/*bla*/**/bla/**",
                "/XXXblaXXXX/testing/testing/bla/testing/testing"
            )
        ).isTrue()
        assertThat(
            pathMatcher.match(
                "/*bla*/**/bla/**",
                "/XXXblaXXXX/testing/testing/bla/testing/testing.jpg"
            )
        ).isTrue()
        assertThat(
            pathMatcher.match(
                "*bla*/**/bla/**",
                "XXXblaXXXX/testing/testing/bla/testing/testing/"
            )
        ).isTrue()
        assertThat(
            pathMatcher.match(
                "*bla*/**/bla/*",
                "XXXblaXXXX/testing/testing/bla/testing"
            )
        ).isTrue()
        assertThat(
            pathMatcher.match(
                "*bla*/**/bla/**",
                "XXXblaXXXX/testing/testing/bla/testing/testing"
            )
        ).isTrue()
        assertThat(
            pathMatcher.match(
                "*bla*/**/bla/*",
                "XXXblaXXXX/testing/testing/bla/testing/testing"
            )
        ).isFalse()
        assertThat(pathMatcher.match("/x/x/**/bla", "/x/x/x/")).isFalse()
        assertThat(pathMatcher.match("/foo/bar/**", "/foo/bar")).isTrue()
        assertThat(pathMatcher.match("", "")).isTrue()
        assertThat(pathMatcher.match("/{bla}.*", "/testing.html")).isTrue()
        assertThat(pathMatcher.match("/{bla}", "//x\ny")).isTrue()
        assertThat(pathMatcher.match("/{var:.*}", "/x\ny")).isTrue()
    }

    @Test
    fun matchWithNullPath() {
        assertThat(pathMatcher.match("/test", null)).isFalse()
        assertThat(pathMatcher.match("/", null)).isFalse()
        assertThat(pathMatcher.match(null, null)).isFalse()
    }

    // SPR-14247
    @Test
    @Throws(Exception::class)
    fun matchWithTrimTokensEnabled() {
        pathMatcher.setTrimTokens(true)
        assertThat(pathMatcher.match("/foo/bar", "/foo /bar")).isTrue()
    }

    @Test
    fun matchStart() {
        // test exact matching
        assertThat(pathMatcher.matchStart("test", "test")).isTrue()
        assertThat(pathMatcher.matchStart("/test", "/test")).isTrue()
        assertThat(pathMatcher.matchStart("/test.jpg", "test.jpg")).isFalse()
        assertThat(pathMatcher.matchStart("test", "/test")).isFalse()
        assertThat(pathMatcher.matchStart("/test", "test")).isFalse()

        // test matching with ?'s
        assertThat(pathMatcher.matchStart("t?st", "test")).isTrue()
        assertThat(pathMatcher.matchStart("??st", "test")).isTrue()
        assertThat(pathMatcher.matchStart("tes?", "test")).isTrue()
        assertThat(pathMatcher.matchStart("te??", "test")).isTrue()
        assertThat(pathMatcher.matchStart("?es?", "test")).isTrue()
        assertThat(pathMatcher.matchStart("tes?", "tes")).isFalse()
        assertThat(pathMatcher.matchStart("tes?", "testt")).isFalse()
        assertThat(pathMatcher.matchStart("tes?", "tsst")).isFalse()

        // test matching with *'s
        assertThat(pathMatcher.matchStart("*", "test")).isTrue()
        assertThat(pathMatcher.matchStart("test*", "test")).isTrue()
        assertThat(pathMatcher.matchStart("test*", "testTest")).isTrue()
        assertThat(pathMatcher.matchStart("test/*", "test/Test")).isTrue()
        assertThat(pathMatcher.matchStart("test/*", "test/t")).isTrue()
        assertThat(pathMatcher.matchStart("test/*", "test/")).isTrue()
        assertThat(pathMatcher.matchStart("*test*", "AnothertestTest")).isTrue()
        assertThat(pathMatcher.matchStart("*test", "Anothertest")).isTrue()
        assertThat(pathMatcher.matchStart("*.*", "test.")).isTrue()
        assertThat(pathMatcher.matchStart("*.*", "test.test")).isTrue()
        assertThat(pathMatcher.matchStart("*.*", "test.test.test")).isTrue()
        assertThat(pathMatcher.matchStart("test*aaa", "testblaaaa")).isTrue()
        assertThat(pathMatcher.matchStart("test*", "tst")).isFalse()
        assertThat(pathMatcher.matchStart("test*", "test/")).isFalse()
        assertThat(pathMatcher.matchStart("test*", "tsttest")).isFalse()
        assertThat(pathMatcher.matchStart("test*", "test/")).isFalse()
        assertThat(pathMatcher.matchStart("test*", "test/t")).isFalse()
        assertThat(pathMatcher.matchStart("test/*", "test")).isTrue()
        assertThat(pathMatcher.matchStart("test/t*.txt", "test")).isTrue()
        assertThat(pathMatcher.matchStart("*test*", "tsttst")).isFalse()
        assertThat(pathMatcher.matchStart("*test", "tsttst")).isFalse()
        assertThat(pathMatcher.matchStart("*.*", "tsttst")).isFalse()
        assertThat(pathMatcher.matchStart("test*aaa", "test")).isFalse()
        assertThat(pathMatcher.matchStart("test*aaa", "testblaaab")).isFalse()

        // test matching with ?'s and /'s
        assertThat(pathMatcher.matchStart("/?", "/a")).isTrue()
        assertThat(pathMatcher.matchStart("/?/a", "/a/a")).isTrue()
        assertThat(pathMatcher.matchStart("/a/?", "/a/b")).isTrue()
        assertThat(pathMatcher.matchStart("/??/a", "/aa/a")).isTrue()
        assertThat(pathMatcher.matchStart("/a/??", "/a/bb")).isTrue()
        assertThat(pathMatcher.matchStart("/?", "/a")).isTrue()

        // test matching with **'s
        assertThat(pathMatcher.matchStart("/**", "/testing/testing")).isTrue()
        assertThat(pathMatcher.matchStart("/*/**", "/testing/testing")).isTrue()
        assertThat(pathMatcher.matchStart("/**/*", "/testing/testing")).isTrue()
        assertThat(pathMatcher.matchStart("test*/**", "test/")).isTrue()
        assertThat(pathMatcher.matchStart("test*/**", "test/t")).isTrue()
        assertThat(pathMatcher.matchStart("/bla/**/bla", "/bla/testing/testing/bla")).isTrue()
        assertThat(pathMatcher.matchStart("/bla/**/bla", "/bla/testing/testing/bla/bla")).isTrue()
        assertThat(pathMatcher.matchStart("/**/test", "/bla/bla/test")).isTrue()
        assertThat(pathMatcher.matchStart("/bla/**/**/bla", "/bla/bla/bla/bla/bla/bla")).isTrue()
        assertThat(pathMatcher.matchStart("/bla*bla/test", "/blaXXXbla/test")).isTrue()
        assertThat(pathMatcher.matchStart("/*bla/test", "/XXXbla/test")).isTrue()
        assertThat(pathMatcher.matchStart("/bla*bla/test", "/blaXXXbl/test")).isFalse()
        assertThat(pathMatcher.matchStart("/*bla/test", "XXXblab/test")).isFalse()
        assertThat(pathMatcher.matchStart("/*bla/test", "XXXbl/test")).isFalse()
        assertThat(pathMatcher.matchStart("/????", "/bala/bla")).isFalse()
        assertThat(pathMatcher.matchStart("/**/*bla", "/bla/bla/bla/bbb")).isTrue()
        assertThat(
            pathMatcher.matchStart(
                "/*bla*/**/bla/**",
                "/XXXblaXXXX/testing/testing/bla/testing/testing/"
            )
        ).isTrue()
        assertThat(
            pathMatcher.matchStart(
                "/*bla*/**/bla/*",
                "/XXXblaXXXX/testing/testing/bla/testing"
            )
        ).isTrue()
        assertThat(
            pathMatcher.matchStart(
                "/*bla*/**/bla/**",
                "/XXXblaXXXX/testing/testing/bla/testing/testing"
            )
        ).isTrue()
        assertThat(
            pathMatcher.matchStart(
                "/*bla*/**/bla/**",
                "/XXXblaXXXX/testing/testing/bla/testing/testing.jpg"
            )
        ).isTrue()
        assertThat(
            pathMatcher.matchStart(
                "*bla*/**/bla/**",
                "XXXblaXXXX/testing/testing/bla/testing/testing/"
            )
        ).isTrue()
        assertThat(
            pathMatcher.matchStart(
                "*bla*/**/bla/*",
                "XXXblaXXXX/testing/testing/bla/testing"
            )
        ).isTrue()
        assertThat(
            pathMatcher.matchStart(
                "*bla*/**/bla/**",
                "XXXblaXXXX/testing/testing/bla/testing/testing"
            )
        ).isTrue()
        assertThat(
            pathMatcher.matchStart(
                "*bla*/**/bla/*",
                "XXXblaXXXX/testing/testing/bla/testing/testing"
            )
        ).isTrue()
        assertThat(pathMatcher.matchStart("/x/x/**/bla", "/x/x/x/")).isTrue()
        assertThat(pathMatcher.matchStart("", "")).isTrue()
    }

    @Test
    fun uniqueDeliminator() {
        pathMatcher.setPathSeparator(".")

        // test exact matching
        assertThat(pathMatcher.match("test", "test")).isTrue()
        assertThat(pathMatcher.match(".test", ".test")).isTrue()
        assertThat(pathMatcher.match(".test/jpg", "test/jpg")).isFalse()
        assertThat(pathMatcher.match("test", ".test")).isFalse()
        assertThat(pathMatcher.match(".test", "test")).isFalse()

        // test matching with ?'s
        assertThat(pathMatcher.match("t?st", "test")).isTrue()
        assertThat(pathMatcher.match("??st", "test")).isTrue()
        assertThat(pathMatcher.match("tes?", "test")).isTrue()
        assertThat(pathMatcher.match("te??", "test")).isTrue()
        assertThat(pathMatcher.match("?es?", "test")).isTrue()
        assertThat(pathMatcher.match("tes?", "tes")).isFalse()
        assertThat(pathMatcher.match("tes?", "testt")).isFalse()
        assertThat(pathMatcher.match("tes?", "tsst")).isFalse()

        // test matching with *'s
        assertThat(pathMatcher.match("*", "test")).isTrue()
        assertThat(pathMatcher.match("test*", "test")).isTrue()
        assertThat(pathMatcher.match("test*", "testTest")).isTrue()
        assertThat(pathMatcher.match("*test*", "AnothertestTest")).isTrue()
        assertThat(pathMatcher.match("*test", "Anothertest")).isTrue()
        assertThat(pathMatcher.match("*/*", "test/")).isTrue()
        assertThat(pathMatcher.match("*/*", "test/test")).isTrue()
        assertThat(pathMatcher.match("*/*", "test/test/test")).isTrue()
        assertThat(pathMatcher.match("test*aaa", "testblaaaa")).isTrue()
        assertThat(pathMatcher.match("test*", "tst")).isFalse()
        assertThat(pathMatcher.match("test*", "tsttest")).isFalse()
        assertThat(pathMatcher.match("*test*", "tsttst")).isFalse()
        assertThat(pathMatcher.match("*test", "tsttst")).isFalse()
        assertThat(pathMatcher.match("*/*", "tsttst")).isFalse()
        assertThat(pathMatcher.match("test*aaa", "test")).isFalse()
        assertThat(pathMatcher.match("test*aaa", "testblaaab")).isFalse()

        // test matching with ?'s and .'s
        assertThat(pathMatcher.match(".?", ".a")).isTrue()
        assertThat(pathMatcher.match(".?.a", ".a.a")).isTrue()
        assertThat(pathMatcher.match(".a.?", ".a.b")).isTrue()
        assertThat(pathMatcher.match(".??.a", ".aa.a")).isTrue()
        assertThat(pathMatcher.match(".a.??", ".a.bb")).isTrue()
        assertThat(pathMatcher.match(".?", ".a")).isTrue()

        // test matching with **'s
        assertThat(pathMatcher.match(".**", ".testing.testing")).isTrue()
        assertThat(pathMatcher.match(".*.**", ".testing.testing")).isTrue()
        assertThat(pathMatcher.match(".**.*", ".testing.testing")).isTrue()
        assertThat(pathMatcher.match(".bla.**.bla", ".bla.testing.testing.bla")).isTrue()
        assertThat(pathMatcher.match(".bla.**.bla", ".bla.testing.testing.bla.bla")).isTrue()
        assertThat(pathMatcher.match(".**.test", ".bla.bla.test")).isTrue()
        assertThat(pathMatcher.match(".bla.**.**.bla", ".bla.bla.bla.bla.bla.bla")).isTrue()
        assertThat(pathMatcher.match(".bla*bla.test", ".blaXXXbla.test")).isTrue()
        assertThat(pathMatcher.match(".*bla.test", ".XXXbla.test")).isTrue()
        assertThat(pathMatcher.match(".bla*bla.test", ".blaXXXbl.test")).isFalse()
        assertThat(pathMatcher.match(".*bla.test", "XXXblab.test")).isFalse()
        assertThat(pathMatcher.match(".*bla.test", "XXXbl.test")).isFalse()
    }

    @Test
    @Throws(Exception::class)
    fun extractPathWithinPattern() {
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/docs/commit.html",
                "/docs/commit.html"
            )
        ).isEmpty()
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/docs/*",
                "/docs/cvs/commit"
            )
        ).isEqualTo("cvs/commit")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/docs/cvs/*.html",
                "/docs/cvs/commit.html"
            )
        ).isEqualTo("commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/docs/**",
                "/docs/cvs/commit"
            )
        ).isEqualTo("cvs/commit")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/docs/**/*.html",
                "/docs/cvs/commit.html"
            )
        ).isEqualTo("cvs/commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/docs/**/*.html",
                "/docs/commit.html"
            )
        ).isEqualTo("commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/*.html",
                "/commit.html"
            )
        ).isEqualTo("commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/*.html",
                "/docs/commit.html"
            )
        ).isEqualTo("docs/commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "*.html",
                "/commit.html"
            )
        ).isEqualTo("/commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "*.html",
                "/docs/commit.html"
            )
        ).isEqualTo("/docs/commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "**/*.*",
                "/docs/commit.html"
            )
        ).isEqualTo("/docs/commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "*",
                "/docs/commit.html"
            )
        ).isEqualTo("/docs/commit.html")
        // SPR-10515
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "**/commit.html",
                "/docs/cvs/other/commit.html"
            )
        ).isEqualTo("/docs/cvs/other/commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/docs/**/commit.html",
                "/docs/cvs/other/commit.html"
            )
        ).isEqualTo("cvs/other/commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/docs/**/**/**/**",
                "/docs/cvs/other/commit.html"
            )
        ).isEqualTo("cvs/other/commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/d?cs/*",
                "/docs/cvs/commit"
            )
        ).isEqualTo("docs/cvs/commit")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/docs/c?s/*.html",
                "/docs/cvs/commit.html"
            )
        ).isEqualTo("cvs/commit.html")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/d?cs/**",
                "/docs/cvs/commit"
            )
        ).isEqualTo("docs/cvs/commit")
        assertThat(
            pathMatcher.extractPathWithinPattern(
                "/d?cs/**/*.html",
                "/docs/cvs/commit.html"
            )
        ).isEqualTo("docs/cvs/commit.html")
    }

    @Test
    @Throws(Exception::class)
    fun extractUriTemplateVariables() {
        var result: Map<String, String?> =
            pathMatcher.extractUriTemplateVariables("/hotels/{hotel}", "/hotels/1")
        assertThat(result).isEqualTo(Collections.singletonMap("hotel", "1"))
        result = pathMatcher.extractUriTemplateVariables("/h?tels/{hotel}", "/hotels/1")
        assertThat(result).isEqualTo(Collections.singletonMap("hotel", "1"))
        result = pathMatcher.extractUriTemplateVariables(
            "/hotels/{hotel}/bookings/{booking}",
            "/hotels/1/bookings/2"
        )
        var expected: MutableMap<String?, String?> = LinkedHashMap()
        expected["hotel"] = "1"
        expected["booking"] = "2"
        assertThat(result).isEqualTo(expected)
        result =
            pathMatcher.extractUriTemplateVariables("/**/hotels/**/{hotel}", "/foo/hotels/bar/1")
        assertThat(result).isEqualTo(Collections.singletonMap("hotel", "1"))
        result = pathMatcher.extractUriTemplateVariables("/{page}.html", "/42.html")
        assertThat(result).isEqualTo(Collections.singletonMap("page", "42"))
        result = pathMatcher.extractUriTemplateVariables("/{page}.*", "/42.html")
        assertThat(result).isEqualTo(Collections.singletonMap("page", "42"))
        result = pathMatcher.extractUriTemplateVariables("/A-{B}-C", "/A-b-C")
        assertThat(result).isEqualTo(Collections.singletonMap("B", "b"))
        result = pathMatcher.extractUriTemplateVariables("/{name}.{extension}", "/test.html")
        expected = LinkedHashMap()
        expected["name"] = "test"
        expected["extension"] = "html"
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun extractUriTemplateVariablesRegex() {
        var result: Map<String, String?> = pathMatcher
            .extractUriTemplateVariables(
                "{symbolicName:[\\w\\.]+}-{version:[\\w\\.]+}.jar",
                "com.example-1.0.0.jar"
            )
        assertThat(result["symbolicName"]).isEqualTo("com.example")
        assertThat(result["version"]).isEqualTo("1.0.0")
        result = pathMatcher.extractUriTemplateVariables(
            "{symbolicName:[\\w\\.]+}-sources-{version:[\\w\\.]+}.jar",
            "com.example-sources-1.0.0.jar"
        )
        assertThat(result["symbolicName"]).isEqualTo("com.example")
        assertThat(result["version"]).isEqualTo("1.0.0")
    }

    @Test
    fun extractUriTemplateVarsRegexQualifiers() {
        var result: Map<String, String?> = pathMatcher.extractUriTemplateVariables(
            "{symbolicName:[\\p{L}\\.]+}-sources-{version:[\\p{N}\\.]+}.jar",
            "com.example-sources-1.0.0.jar"
        )
        assertThat(result["symbolicName"]).isEqualTo("com.example")
        assertThat(result["version"]).isEqualTo("1.0.0")
        result = pathMatcher.extractUriTemplateVariables(
            "{symbolicName:[\\w\\.]+}-sources-{version:[\\d\\.]+}-{year:\\d{4}}{month:\\d{2}}{day:\\d{2}}.jar",
            "com.example-sources-1.0.0-20100220.jar"
        )
        assertThat(result["symbolicName"]).isEqualTo("com.example")
        assertThat(result["version"]).isEqualTo("1.0.0")
        assertThat(result["year"]).isEqualTo("2010")
        assertThat(result["month"]).isEqualTo("02")
        assertThat(result["day"]).isEqualTo("20")
        result = pathMatcher.extractUriTemplateVariables(
            "{symbolicName:[\\p{L}\\.]+}-sources-{version:[\\p{N}\\.\\{\\}]+}.jar",
            "com.example-sources-1.0.0.{12}.jar"
        )
        assertThat(result["symbolicName"]).isEqualTo("com.example")
        assertThat(result["version"]).isEqualTo("1.0.0.{12}")
    }

    @Test
    fun extractUriTemplateVarsRegexCapturingGroups() {
        assertThatIllegalArgumentException().isThrownBy {
            pathMatcher.extractUriTemplateVariables(
                "/web/{id:foo(bar)?}",
                "/web/foobar"
            )
        }
            .withMessageContaining("The number of capturing groups in the pattern")
    }

    @Test
    fun trimTokensOff() {
        pathMatcher.setTrimTokens(false)
        assertThat(pathMatcher.match("/group/{groupName}/members", "/group/sales/members")).isTrue()
        assertThat(
            pathMatcher.match(
                "/group/{groupName}/members",
                "/group/  sales/members"
            )
        ).isTrue()
        assertThat(
            pathMatcher.match(
                "/group/{groupName}/members",
                "/Group/  Sales/Members"
            )
        ).isFalse()
    }

    @Test
    fun caseInsensitive() {
        pathMatcher.setCaseSensitive(false)
        assertThat(pathMatcher.match("/group/{groupName}/members", "/group/sales/members")).isTrue()
        assertThat(pathMatcher.match("/group/{groupName}/members", "/Group/Sales/Members")).isTrue()
        assertThat(pathMatcher.match("/Group/{groupName}/Members", "/group/Sales/members")).isTrue()
    }

    @Test
    fun defaultCacheSetting() {
        match()
        assertThat(pathMatcher.stringMatcherCache).size().isGreaterThan(20)
        for (i in 0..65535) {
            pathMatcher.match("test$i", "test")
        }
        // Cache turned off because it went beyond the threshold
        assertThat(pathMatcher.stringMatcherCache).isEmpty()
    }

    @Test
    fun cachePatternsSetToTrue() {
        pathMatcher.setCachePatterns(true)
        match()
        assertThat(pathMatcher.stringMatcherCache).size().isGreaterThan(20)
        for (i in 0..65535) {
            pathMatcher.match("test$i", "test$i")
        }
        // Cache keeps being alive due to the explicit cache setting
        assertThat(pathMatcher.stringMatcherCache).size().isGreaterThan(65536)
    }

    @Test
    fun preventCreatingStringMatchersIfPathDoesNotStartsWithPatternPrefix() {
        pathMatcher.setCachePatterns(true)
        assertThat(pathMatcher.stringMatcherCache).isEmpty()
        pathMatcher.match("test?", "test")
        assertThat(pathMatcher.stringMatcherCache).hasSize(1)
        pathMatcher.match("test?", "best")
        pathMatcher.match("test/*", "view/test.jpg")
        pathMatcher.match("test/**/test.jpg", "view/test.jpg")
        pathMatcher.match("test/{name}.jpg", "view/test.jpg")
        assertThat(pathMatcher.stringMatcherCache).hasSize(1)
    }

    @Test
    fun creatingStringMatchersIfPatternPrefixCannotDetermineIfPathMatch() {
        pathMatcher.setCachePatterns(true)
        assertThat(pathMatcher.stringMatcherCache).isEmpty()
        pathMatcher.match("test", "testian")
        pathMatcher.match("test?", "testFf")
        pathMatcher.match("test/*", "test/dir/name.jpg")
        pathMatcher.match("test/{name}.jpg", "test/lorem.jpg")
        pathMatcher.match("bla/**/test.jpg", "bla/test.jpg")
        pathMatcher.match("**/{name}.jpg", "test/lorem.jpg")
        pathMatcher.match("/**/{name}.jpg", "/test/lorem.jpg")
        pathMatcher.match("/*/dir/{name}.jpg", "/*/dir/lorem.jpg")
        assertThat(pathMatcher.stringMatcherCache).hasSize(7)
    }

    @Test
    fun cachePatternsSetToFalse() {
        pathMatcher.setCachePatterns(false)
        match()
        assertThat(pathMatcher.stringMatcherCache).isEmpty()
    }

    @Test
    fun isPattern() {
        assertThat(pathMatcher.isPattern("/test/*")).isTrue()
        assertThat(pathMatcher.isPattern("/test/**/name")).isTrue()
        assertThat(pathMatcher.isPattern("/test?")).isTrue()
        assertThat(pathMatcher.isPattern("/test/{name}")).isTrue()
        assertThat(pathMatcher.isPattern("/test/name")).isFalse()
        assertThat(pathMatcher.isPattern("/test/foo{bar")).isFalse()
    }

    @Test
    fun isPatternWithNullPath() {
        assertThat(pathMatcher.isPattern(null)).isFalse()
    }

    @Test
    fun consistentMatchWithWildcardsAndTrailingSlash() {
        assertThat(pathMatcher.match("/*/foo", "/en/foo")).isTrue()
        assertThat(pathMatcher.match("/*/foo", "/en/foo/")).isFalse()
        assertThat(pathMatcher.match("/**/foo", "/en/foo")).isTrue()
        assertThat(pathMatcher.match("/**/foo", "/en/foo/")).isFalse()
    }
}