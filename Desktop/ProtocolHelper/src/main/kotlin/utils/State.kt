package utils

data class RunState(
    val ids: List<Int> = listOf(),
    val steps: String = "3200",
    val speed: String = "600",
    val acceleration: String = "600",
    val deceleration: String = "600",
    val tx: String = "",
)

data class SetState(
    val hashMap: Map<Int, Int> = hashMapOf(),
    val tx: String = "",
)

data class QueryState(
    val ids: List<Int> = listOf(),
    val tx: String = "",
)