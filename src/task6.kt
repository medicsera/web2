fun main(args: Array<String>) {
    val words = if (args.isNotEmpty()) {
        args.toList()
    } else {
        System.`in`.bufferedReader().readText()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
    }

    words.groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key }
        )
        .forEach { println("${it.key} ${it.value}") }
}