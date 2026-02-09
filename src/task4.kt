fun main(args: Array<String>) {
    args.groupingBy { it }
        .eachCount()
        .toSortedMap()
        .forEach { (word, count) -> println("$word $count") }
}