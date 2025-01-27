import kotlin.reflect.KProperty

class ValidatedDateDelegate(private var value: String) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return value
    }

    /**
     * Validates String-Dates to ensure they are either empty or in the needed format
     */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: String) {
        println("value $value newValue $newValue")
        val regex = Regex("2\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T\\d{2}:\\d{2}:\\d{2}Z")
        require(newValue == "" || newValue.matches(regex)) { "Invalid date" }
        value = newValue
    }

}