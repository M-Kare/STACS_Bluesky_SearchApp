package de.hs_rm.stacs

import ValidatedDateDelegate
import org.junit.jupiter.api.Test
import ValidatedLangDelegate
import ValidatedSearchwordDelegate
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ValidatorTests {

    @ParameterizedTest
    @CsvSource(
        "abc",
        "hello",
        "2017-01-17T17:17:17",
        "2017-01-1717:17:17Z",
        "1917-01-17T17:17:17Z"
        )
    fun invalidDateTest(input: String) {
        var invalidDate: String by ValidatedDateDelegate("")
        assertThrows<IllegalArgumentException> {
            invalidDate = input
        }
    }

    @ParameterizedTest
    @CsvSource(
        "2017-01-17T17:17:17Z",
        "2000-01-01T00:00:00Z",
    )
    fun validDateTest(input: String) {
        //"2017-01-17T17:17:17Z"
        var validDate: String by ValidatedDateDelegate("")
        assertDoesNotThrow {
            validDate = input
        }
    }

    @ParameterizedTest
    @CsvSource(
        "english",
        "ger",
    )
    fun invalidLangTest(input: String) {
        var invalidLang: String by ValidatedLangDelegate("")
        assertThrows<IllegalArgumentException> {
            invalidLang = input
        }
    }

    @ParameterizedTest
    @CsvSource(
        "nl","en","fi","fr","de","id","it","pt","es","sw"
    )
    fun validLangTest(input: String) {
        var validLang: String by ValidatedLangDelegate("")
        assertDoesNotThrow {
            validLang = input
        }
    }

    @ParameterizedTest
    @CsvSource(
        "hello?",
        "%20",
        "/password/now?gimme"
    )
    fun invalidSearchwordTest(input: String) {
        var invalidSearchword: String by ValidatedSearchwordDelegate("")
        assertThrows<IllegalArgumentException> {
            invalidSearchword = input
        }
    }

    @ParameterizedTest
    @CsvSource(
        "hello",
        "20",
        "i want your password",
        "dragons dogma"
    )
    fun validSearchwordTest(input: String) {
        var validSearchword: String by ValidatedSearchwordDelegate("")
        assertDoesNotThrow {
            validSearchword = input
        }
    }

}