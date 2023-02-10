class Block(val color: String) {
    object BlockProperties {
        var length = 6
        var width = 4
        fun blocksInBox(length: Int, width: Int): Int {
            val numLength = length / BlockProperties.length
            val numWidth = width / BlockProperties.width
            return numLength * numWidth
        }
    }
}