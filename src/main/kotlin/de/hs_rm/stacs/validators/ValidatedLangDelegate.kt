import kotlin.reflect.KProperty

class ValidatedLangDelegate(private var value: String) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return value
    }

    /**
     * Validates lang by checking if the value is in the list of valid values
     */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: String) {
        val langs = listOf("","nl","en","fi","fr","de","id","it","pt","es","sw")
        require(langs.contains(newValue)) { "Invalid language code" }
        value = newValue
    }

}