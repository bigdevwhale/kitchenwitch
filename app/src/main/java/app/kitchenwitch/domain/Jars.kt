package app.kitchenwitch.domain

/**
 * A jar on the witch's shelf: a spice or a pantry thing that changes a dish. [stems] are lowercase word starts
 * (ё written as е) that find it in an ingredient line, in either language. [lore] is the old kitchen belief about it.
 */
class Jar(val id: String, val ru: String, val en: String, val color: Long, val loreRu: String, val loreEn: String, val stems: List<String>, val far: Boolean = false) {
    val name get() = tr(ru, en)
    val lore get() = tr(loreRu, loreEn)
}

object Jars {
    val all = listOf(
        Jar("salt", "Соль", "Salt", 0xE9E4DC, "очищение; с неё начинается любое заклинание", "cleansing; every spell begins with it", listOf("сол", "salt")),
        Jar("pepper", "Чёрный перец", "Black pepper", 0x2C2420, "смелость; гонит прочь всё лишнее", "courage, and sending away what's not wanted", listOf("черный перец", "перец черн", "перец горош", "black pepper", "peppercorn")),
        Jar("bay", "Лавровый лист", "Bay leaf", 0x7D8A4A, "исполняет желания – их пишут прямо на листе", "wishes come true: write the wish on the leaf", listOf("лавров", "bay lea")),
        Jar("rosemary", "Розмарин", "Rosemary", 0x5F7A52, "память и защита дома", "remembrance and a guarded home", listOf("розмарин", "rosemary")),
        Jar("basil", "Базилик", "Basil", 0x4F8A45, "удача и деньги", "luck and money", listOf("базилик", "basil")),
        Jar("cinnamon", "Корица", "Cinnamon", 0x9A5A32, "тепло и успех, «чтобы всё сладилось»", "warmth and success, so that things come together", listOf("кориц", "cinnamon")),
        Jar("paprika", "Паприка", "Paprika", 0xB8432E, "энергия и смелость", "energy and nerve", listOf("паприк", "paprika")),
        Jar("caraway", "Тмин", "Caraway", 0x6E5A3A, "верность – чтобы ничего не терялось", "faithfulness, so nothing gets lost", listOf("тмин", "caraway")),
        Jar("nutmeg", "Мускатный орех", "Nutmeg", 0x8A6040, "удача в начинаниях", "luck in new undertakings", listOf("мускат", "nutmeg")),
        Jar("clove", "Гвоздика", "Cloves", 0x4A2A22, "защита от сплетен", "protection from gossip", listOf("гвоздик", "clove")),
        Jar("vanilla", "Ваниль", "Vanilla", 0x2E2018, "нежность и любовь", "tenderness and love", listOf("ванил", "vanilla")),
        Jar("dill", "Укроп", "Dill", 0x8AA25A, "радость в доме", "joy in the house", listOf("укроп", "dill")),
        Jar("honey", "Мёд", "Honey", 0xD99A2A, "сладость отношений", "sweetness between people", listOf("мед ", "меда", "медом", "мед,", "honey")),
        Jar("ginger", "Имбирь", "Ginger", 0xD6B36A, "сила и скорость – любое дело пойдёт быстрее", "strength and speed; it hurries any work", listOf("имбир", "ginger")),
        Jar("turmeric", "Куркума", "Turmeric", 0xE0A020, "солнце в банке, ясность", "sun in a jar; clarity", listOf("куркум", "turmeric")),
        Jar("thyme", "Тимьян", "Thyme", 0x6B7A55, "смелость перед трудным", "courage before something hard", listOf("тимьян", "чабрец", "thyme")),
        Jar("sage", "Шалфей", "Sage", 0x8A9A80, "мудрость и очищение", "wisdom and cleansing", listOf("шалфе", "sage")),
        Jar("mint", "Мята", "Mint", 0x6FAF7A, "свежая голова и лёгкая дорога", "a clear head and an easy road", listOf("мят", "mint")),
        Jar("oregano", "Орегано", "Oregano", 0x6E8040, "радость и покой", "joy and peace", listOf("орегано", "душиц", "oregano")),
        Jar("garlic", "Чеснок", "Garlic", 0xEFE6D2, "защита от всякой нечисти", "protection from anything uninvited", listOf("чеснок", "чеснока", "garlic")),
        Jar("cardamom", "Кардамон", "Cardamom", 0x9AAA6A, "влечение и ясность", "attraction and clarity", listOf("кардамон", "cardamom")),
        Jar("coriander", "Кориандр", "Coriander seed", 0xB09A6A, "мир в семье", "peace in the family", listOf("кориандр", "coriander")),
        Jar("mustard", "Горчица", "Mustard", 0xC8A030, "храбрость и ясный ум", "bravery and a sharp mind", listOf("горчиц", "mustard")),
        Jar("chili", "Перец чили", "Chilli", 0xC0301E, "огонь: разжигает любое дело", "fire; it kindles anything", listOf("чили", "халапен", "chili", "chilli", "jalapen")),
        Jar("vinegar", "Уксус", "Vinegar", 0xC8B88A, "очищает и помогает расстаться с ненужным", "cleansing; cutting what's not needed", listOf("уксус", "vinegar")),
        Jar("soy", "Соевый соус", "Soy sauce", 0x3A2418, "глубина и долгая память", "depth and a long memory", listOf("соев", "soy sauce", "soy")),
        Jar("cocoa", "Какао", "Cocoa", 0x5A3624, "утешение в тёмный вечер", "comfort on a dark evening", listOf("какао", "шоколад", "cocoa", "chocolate")),
        Jar("sumac", "Сумах", "Sumac", 0x8A2C3A, "кислинка, от которой тянет на новое", "a sour note that whets the appetite for new things", listOf("сумах", "sumac"), far = true),
        Jar("saffron", "Шафран", "Saffron", 0xE05A1A, "к достатку; солнце в тонких нитях", "plenty; the sun in threads", listOf("шафран", "saffron"), far = true),
        Jar("star_anise", "Бадьян", "Star anise", 0x5A3020, "путеводная звезда, удача в дороге", "a guiding star; luck on the road", listOf("бадьян", "star anise"), far = true),
        Jar("miso", "Мисо", "Miso", 0xB88A4A, "терпение: вкус делает само время", "patience; time makes the taste", listOf("мисо", "miso"), far = true),
        Jar("tahini", "Тахини", "Tahini", 0xCFB48A, "лёгкость и мир за столом", "ease and peace at the table", listOf("тахин", "кунжутн паст", "tahini", "tahina"), far = true),
        Jar("cumin", "Зира", "Cumin", 0x7A5A30, "связь с далёкими землями", "a tie to faraway lands", listOf("зир", "кумин", "cumin"), far = true),
        Jar("garam", "Гарам масала", "Garam masala", 0x7A4020, "огонь, который греет и собирает всех вместе", "a flame that warms and gathers", listOf("гарам", "масал", "garam", "masala"), far = true),
        Jar("khmeli", "Хмели-сунели", "Khmeli suneli", 0x7A7A3A, "гостеприимство – за такой стол хочется вернуться", "hospitality; a table you want to come back to", listOf("хмели", "khmeli"), far = true),
        Jar("fish_sauce", "Рыбный соус", "Fish sauce", 0x8A5A2A, "смелость пробовать непривычное", "the nerve to try strange things", listOf("рыбн соус", "рыбный соус", "fish sauce"), far = true),
        Jar("gochujang", "Кочхуджан", "Gochujang", 0xA8281E, "жар в холодный день", "heat on a cold day", listOf("кочхуджан", "гочуджан", "gochujang"), far = true),
        Jar("harissa", "Харисса", "Harissa", 0xB0381E, "страсть и защита", "passion and protection", listOf("харисс", "harissa"), far = true),
        Jar("fennel", "Фенхель", "Fennel seed", 0xA8B070, "сила и долгий век", "strength and long life", listOf("фенхел", "fennel"), far = true),
        Jar("smoked", "Копчёная паприка", "Smoked paprika", 0x8A2A1A, "дым костра в доме", "campfire smoke indoors", listOf("копчен паприк", "копченая паприк", "smoked paprika"), far = true),
        Jar("za_atar", "Заатар", "Za'atar", 0x7A8040, "ясная память, острый ум", "a clear memory and a sharp mind", listOf("заатар", "za'atar", "zaatar", "zatar"), far = true),
        Jar("cardamom_black", "Чёрный кардамон", "Black cardamom", 0x3A2A20, "тайна и долгий жар", "mystery and a slow fire", listOf("черный кардамон", "black cardamom"), far = true),
    )
    val byId = all.associateBy { it.id }

    private fun norm(s: String) = " " + s.lowercase().replace('ё', 'е') + " "

    /** The jar an ingredient line names, if any. More specific stems win (smoked paprika before paprika). */
    fun of(line: String): Jar? {
        val t = norm(line)
        return all.flatMap { j -> j.stems.map { j to it } }.sortedByDescending { it.second.length }
            .firstOrNull { (_, st) -> t.contains(" $st") || t.contains("-$st") }?.first
    }
}
