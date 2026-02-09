fun main(args: Array<String>) {
    args.groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key }
        )
        .forEach { println("${it.key} ${it.value}") }
}