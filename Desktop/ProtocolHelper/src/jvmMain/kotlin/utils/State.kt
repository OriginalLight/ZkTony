package utils

data class RunState(
    val ids: List<Int> = listOf(),
    val steps: Int = 3200,
    val speed: Float = 600f,
    val acceleration: Float = 600f,
    val deceleration: Float = 600f,
)

data class SetState(
    val ids: List<Int> = listOf(),
    val values: List<Int> = listOf(),
)

data class QueryState(
    val ids: List<Int> = listOf(),
)