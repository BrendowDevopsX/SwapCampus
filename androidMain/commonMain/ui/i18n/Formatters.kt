@Composable
expect fun currentLanguageTag(): String
expect fun formatPrice(price: Double, languageTag: String): String
expect fun formatDate(date: LocalDate, languageTag: String): String