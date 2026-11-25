package com.kokouliss.shelfanything.core.data.metadata

/**
 * What product a barcode is, for the shelves whose site has no barcode search (movies on disc,
 * video games, board games). The free trial endpoint allows about a hundred lookups a day per phone.
 */
internal object UpcItemDb {
    fun lookupUrl(barcode: String): String = "https://api.upcitemdb.com/prod/trial/lookup?upc=${barcode.encode()}"

    /** The product's title, or null when the barcode is unknown. */
    fun parseTitle(body: String): String? = body.toJson()["items"].objects().firstNotNullOfOrNull { it["title"].string() }

    /**
     * The words of a product title that name the work: "Alien (Blu-ray) [Region B]" gives "Alien".
     * Anything in brackets, and the words that name a disc or an edition, are dropped.
     */
    fun workTitle(productTitle: String): String {
        val plain = productTitle.replace(Regex("[(\\[].*?[)\\]]"), " ")
        val noise = Regex(
            "\\b(dvd|blu-?ray|4k|uhd|ultra hd|steelbook|edition|collector'?s|special|limited|region [a-c0-9]|ps[1-5]|playstation [1-5]|xbox( one| series x| 360)?|nintendo switch|switch|pc|game)\\b",
            RegexOption.IGNORE_CASE,
        )
        return plain.replace(noise, " ").replace(Regex("[-:,/]+\\s*$"), "").replace(Regex("\\s+"), " ").trim()
    }
}
