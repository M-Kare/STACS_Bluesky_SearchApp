import kotlin.reflect.KProperty

class ValidatedSearchwordDelegate(private var value: String) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return value
    }

    /**
     * Validates SearchWords to ensure they don't contain special characters.
     */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: String) {
        require(newValue.length > 0 && !newValue.matches(Regex(".*[?/&=#%+]+.*"))) { "Invalid search" }
        value = newValue
    }

}