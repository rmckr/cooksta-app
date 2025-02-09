package com.example.cooksta_app.model

data class Post(
    val imageUrl: String,
    val videoUrl: String?,
    val description: String = "1.Vorbereitung: Heize den Ofen auf 180 °C Ober-/Unterhitze vor und lege ein Backblech mit Backpapier aus. \n2. Teig anrühren: Gib die weiche Butter, den Zucker und den braunen Zucker in eine Schüssel. Schlage die Zutaten mit einem Handmixer oder einer Küchenmaschine schaumig. Füge das Ei und den Vanilleextrakt hinzu und rühre weiter, bis eine gleichmäßige Masse entsteht.\n3. Trockene Zutaten hinzufügen: Siebe das Mehl und das Backpulver in eine separate Schüssel. Vermische beides und gib es dann nach und nach zu der Butter-Zucker-Mischung. Rühre so lange, bis ein glatter Teig entsteht.\n4. Schokolade einrühren: Hacke die Zartbitterschokolade in kleine Stücke und hebe sie unter den Teig.\n5. Kekse formen: Mit einem Teelöffel nimm portionsweise Teig und forme kleine Kugeln. Lege sie mit ausreichend Abstand auf das vorbereitete Backblech. Die Kekse laufen beim Backen etwas auseinander, daher sollte der Abstand groß genug sein.\n6. Backen: Backe die Kekse für etwa 10-12 Minuten, bis sie goldbraun sind. Lass sie dann kurz abkühlen, bevor du sie vorsichtig vom Blech nimmst.\n7. Genießen: Die Schokoladenkekse sind noch warm besonders lecker. Lass sie vollständig auskühlen und bewahre sie in einer luftdichten Dose auf, wenn du sie später genießen möchtest.",
    val ingredients: Map<String, Float> = mapOf(Pair("Backpulver (TL)", 2f), Pair("Butter (g)", 200f), Pair("Eier (Stück)", 1f)),
    var isLiked: Boolean = false,
    var isSaved: Boolean = false
)