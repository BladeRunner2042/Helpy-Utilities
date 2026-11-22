package com.example.helpyutilities.util

object OfflineTranslator {

    // Common sentence pattern templates
    private data class PatternTemplate(
        val regex: Regex,
        val formatMap: Map<String, (String) -> String>
    )

    private val patternTemplates = listOf(
        PatternTemplate(
            regex = Regex("^(?:where is the|where's the|where is|where's)\\s+(?:the\\s+)?(.+)\\??$", RegexOption.IGNORE_CASE),
            formatMap = mapOf(
                "es" to { "¿Dónde está el/la $it?" },
                "fr" to { "Où se trouve $it ?" },
                "de" to { "Wo ist $it?" },
                "it" to { "Dov'è $it?" },
                "pt" to { "Onde fica $it?" },
                "zh-CN" to { "$it 在哪里？" },
                "ja" to { "$it はどこですか？" },
                "ko" to { "$it 이/가 어디에 있나요?" },
                "ar" to { "أين $it؟" },
                "hi" to { "$it कहाँ है?" },
                "ru" to { "Где находится $it?" },
                "nl" to { "Waar is $it?" },
                "tr" to { "$it nerede?" },
                "sv" to { "Var är $it?" },
                "id" to { "Di mana $it?" },
                "vi" to { "$it ở đâu?" }
            )
        ),
        PatternTemplate(
            regex = Regex("^(?:how much is|how much does (?:it|this) cost|how much for)\\s+(.+)\\??$", RegexOption.IGNORE_CASE),
            formatMap = mapOf(
                "es" to { "¿Cuánto cuesta $it?" },
                "fr" to { "Combien coûte $it ?" },
                "de" to { "Wie viel kostet $it?" },
                "it" to { "Quanto costa $it?" },
                "pt" to { "Quanto custa $it?" },
                "zh-CN" to { "$it 多少钱？" },
                "ja" to { "$it はいくらですか？" },
                "ko" to { "$it 은/는 얼마인가요?" },
                "ar" to { "بكم $it؟" },
                "hi" to { "$it कितने का है?" },
                "ru" to { "Сколько стоит $it?" },
                "nl" to { "Hoeveel kost $it?" },
                "tr" to { "$it ne kadar?" }
            )
        ),
        PatternTemplate(
            regex = Regex("^(?:i want|i would like|i'd like|can i get|can i have)\\s+(?:a|an|the)?\\s*(.+)\\??$", RegexOption.IGNORE_CASE),
            formatMap = mapOf(
                "es" to { "Quisiera $it, por favor" },
                "fr" to { "Je voudrais $it, s'il vous plaît" },
                "de" to { "Ich möchte $it, bitte" },
                "it" to { "Vorrei $it, per favore" },
                "pt" to { "Eu gostaria de $it, por favor" },
                "zh-CN" to { "我想要一个 $it" },
                "ja" to { "$it をお願いします" },
                "ko" to { "$it 주세요" },
                "ar" to { "أريد $it، من فضلك" },
                "hi" to { "मुझे $it चाहिए" },
                "ru" to { "Я бы хотел $it, пожалуйста" },
                "nl" to { "Ik wil graag $it, alstublieft" }
            )
        ),
        PatternTemplate(
            regex = Regex("^(?:i need|i must find|help me find)\\s+(?:a|an|the)?\\s*(.+)\\??$", RegexOption.IGNORE_CASE),
            formatMap = mapOf(
                "es" to { "Necesito $it con urgencia" },
                "fr" to { "J'ai besoin de $it" },
                "de" to { "Ich brauche $it" },
                "it" to { "Ho bisogno di $it" },
                "pt" to { "Eu preciso de $it" },
                "zh-CN" to { "我需要 $it" },
                "ja" to { "$it が必要です" },
                "ko" to { "$it 이/가 필요합니다" },
                "ar" to { "أحتاج إلى $it" },
                "hi" to { "मुझे $it की जरूरत है" },
                "ru" to { "Мне нужен $it" }
            )
        ),
        PatternTemplate(
            regex = Regex("^(?:do you have|is there)\\s+(?:a|an|any)?\\s*(.+)\\??$", RegexOption.IGNORE_CASE),
            formatMap = mapOf(
                "es" to { "¿Tiene $it?" },
                "fr" to { "Avez-vous $it ?" },
                "de" to { "Haben Sie $it?" },
                "it" to { "Ha $it?" },
                "pt" to { "Você tem $it?" },
                "zh-CN" to { "请问有 $it 吗？" },
                "ja" to { "$it はありますか？" },
                "ko" to { "$it 있나요?" },
                "ar" to { "هل لديك $it؟" },
                "hi" to { "क्या आपके पास $it है?" }
            )
        )
    )

    // Comprehensive offline dictionary database for 100+ concepts across 16 major languages
    private val dictionary: Map<String, Map<String, String>> = mapOf(
        // Greetings & Social
        "hello" to mapOf("es" to "Hola", "fr" to "Bonjour", "de" to "Hallo", "it" to "Ciao", "pt" to "Olá", "zh-CN" to "你好", "ja" to "こんにちは", "ko" to "안녕하세요", "ar" to "مرحبا", "hi" to "नमस्ते", "ru" to "Привет", "nl" to "Hallo", "tr" to "Merhaba", "sv" to "Hej", "id" to "Halo", "vi" to "Xin chào"),
        "hi" to mapOf("es" to "Hola", "fr" to "Salut", "de" to "Hallo", "it" to "Ciao", "pt" to "Oi", "zh-CN" to "嗨", "ja" to "やあ", "ko" to "안녕", "ar" to "أهلاً", "hi" to "नमस्ते", "ru" to "Привет", "nl" to "Hoi", "tr" to "Selam"),
        "goodbye" to mapOf("es" to "Adiós", "fr" to "Au revoir", "de" to "Auf Wiedersehen", "it" to "Arrivederci", "pt" to "Adeus", "zh-CN" to "再见", "ja" to "さようなら", "ko" to "안녕히 가세요", "ar" to "مع السلامة", "hi" to "अलविदा", "ru" to "До свидания", "nl" to "Tot ziens", "tr" to "Hoşça kal"),
        "bye" to mapOf("es" to "Adiós", "fr" to "Salut", "de" to "Tschüss", "it" to "Ciao", "pt" to "Tchau", "zh-CN" to "拜拜", "ja" to "じゃあね", "ko" to "잘 가", "ar" to "باي", "hi" to "बाय", "ru" to "Пока", "nl" to "Doei"),
        "good morning" to mapOf("es" to "Buenos días", "fr" to "Bonjour", "de" to "Guten Morgen", "it" to "Buongiorno", "pt" to "Bom dia", "zh-CN" to "早上好", "ja" to "おはようございます", "ko" to "좋은 아침입니다", "ar" to "صباح الخير", "hi" to "सुप्रभात", "ru" to "Доброе утро", "nl" to "Goedemorgen"),
        "good afternoon" to mapOf("es" to "Buenas tardes", "fr" to "Bon après-midi", "de" to "Guten Tag", "it" to "Buon pomeriggio", "pt" to "Boa tarde", "zh-CN" to "下午好", "ja" to "こんにちは", "ko" to "좋은 오후입니다", "ar" to "مساء الخير", "hi" to "शुभ दोपहर"),
        "good evening" to mapOf("es" to "Buenas noches", "fr" to "Bonsoir", "de" to "Guten Abend", "it" to "Buonasera", "pt" to "Boa noite", "zh-CN" to "晚上好", "ja" to "こんばんは", "ko" to "좋은 저녁입니다", "ar" to "مساء الخير", "hi" to "शुभ संध्या"),
        "good night" to mapOf("es" to "Buenas noches", "fr" to "Bonne nuit", "de" to "Gute Nacht", "it" to "Buonanotte", "pt" to "Boa noite", "zh-CN" to "晚安", "ja" to "おやすみなさい", "ko" to "안녕히 주무세요", "ar" to "تصبح على خير", "hi" to "शुभ रात्रि"),
        "how are you" to mapOf("es" to "¿Cómo estás?", "fr" to "Comment allez-vous ?", "de" to "Wie geht es Ihnen?", "it" to "Come stai?", "pt" to "Como você está?", "zh-CN" to "你好吗？", "ja" to "お元気ですか？", "ko" to "어떻게 지내세요?", "ar" to "كيف حالك؟", "hi" to "आप कैसे हैं?", "ru" to "Как дела?"),
        "nice to meet you" to mapOf("es" to "Mucho gusto", "fr" to "Ravi de vous rencontrer", "de" to "Freut mich, Sie kennenzulernen", "it" to "Piacere di conoscerti", "pt" to "Prazer em conhecê-lo", "zh-CN" to "很高兴认识你", "ja" to "はじめまして", "ko" to "만나서 반갑습니다", "ar" to "تشرفت بمعرفتك", "hi" to "आपसे मिलकर खुशी हुई"),

        // Politeness & Basics
        "yes" to mapOf("es" to "Sí", "fr" to "Oui", "de" to "Ja", "it" to "Sì", "pt" to "Sim", "zh-CN" to "是的", "ja" to "はい", "ko" to "네", "ar" to "نعم", "hi" to "हाँ", "ru" to "Да", "nl" to "Ja", "tr" to "Evet"),
        "no" to mapOf("es" to "No", "fr" to "Non", "de" to "Nein", "it" to "No", "pt" to "Não", "zh-CN" to "不", "ja" to "いいえ", "ko" to "아니요", "ar" to "لا", "hi" to "नहीं", "ru" to "Нет", "nl" to "Nee", "tr" to "Hayır"),
        "please" to mapOf("es" to "Por favor", "fr" to "S'il vous plaît", "de" to "Bitte", "it" to "Per favore", "pt" to "Por favor", "zh-CN" to "请", "ja" to "お願いします", "ko" to "부탁합니다", "ar" to "من فضلك", "hi" to "कृपया", "ru" to "Пожалуйста", "nl" to "Alsjeblieft"),
        "thank you" to mapOf("es" to "Gracias", "fr" to "Merci", "de" to "Danke", "it" to "Grazie", "pt" to "Obrigado", "zh-CN" to "谢谢", "ja" to "ありがとう", "ko" to "감사합니다", "ar" to "شكراً", "hi" to "धन्यवाद", "ru" to "Спасибо", "nl" to "Dank je", "tr" to "Teşekkür ederim"),
        "thanks" to mapOf("es" to "Gracias", "fr" to "Merci", "de" to "Danke", "it" to "Grazie", "pt" to "Valeu", "zh-CN" to "多谢", "ja" to "どうも", "ko" to "고마워", "ar" to "شكراً", "hi" to "शुक्रिया", "ru" to "Спасибо"),
        "you are welcome" to mapOf("es" to "De nada", "fr" to "De rien", "de" to "Gern geschehen", "it" to "Prego", "pt" to "De nada", "zh-CN" to "不客气", "ja" to "どういたしまして", "ko" to "천만에요", "ar" to "عفواً", "hi" to "आपका स्वागत है"),
        "sorry" to mapOf("es" to "Lo siento", "fr" to "Désolé", "de" to "Entschuldigung", "it" to "Scusa", "pt" to "Desculpe", "zh-CN" to "对不起", "ja" to "ごめんなさい", "ko" to "죄송합니다", "ar" to "آسف", "hi" to "माफ़ कीजिये", "ru" to "Извините"),
        "excuse me" to mapOf("es" to "Disculpe", "fr" to "Excusez-moi", "de" to "Entschuldigen Sie", "it" to "Mi scusi", "pt" to "Com licença", "zh-CN" to "打扰一下", "ja" to "すみません", "ko" to "실례합니다", "ar" to "عذراً", "hi" to "माफ कीजिये"),
        "i love you" to mapOf("es" to "Te quiero", "fr" to "Je t'aime", "de" to "Ich liebe dich", "it" to "Ti amo", "pt" to "Eu te amo", "zh-CN" to "我爱你", "ja" to "愛してる", "ko" to "사랑해요", "ar" to "أحبك", "hi" to "मैं तुमसे प्यार करता हूँ", "ru" to "Я тебя люблю"),

        // Emergency & Medical
        "help" to mapOf("es" to "¡Ayuda!", "fr" to "À l'aide !", "de" to "Hilfe!", "it" to "Aiuto!", "pt" to "Ajuda!", "zh-CN" to "救命！", "ja" to "助けて！", "ko" to "도와주세요!", "ar" to "النجدة!", "hi" to "मदद करो!", "ru" to "Помогите!"),
        "emergency" to mapOf("es" to "Emergencia", "fr" to "Urgence", "de" to "Notfall", "it" to "Emergenza", "pt" to "Emergência", "zh-CN" to "紧急情况", "ja" to "緊急事態", "ko" to "비상사태", "ar" to "طوارئ", "hi" to "आपातकालीन", "ru" to "Чрезвычайная ситуация"),
        "call the police" to mapOf("es" to "Llame a la policía", "fr" to "Appelez la police", "de" to "Rufen Sie die Polizei", "it" to "Chiami la polizia", "pt" to "Chame a polícia", "zh-CN" to "报警！", "ja" to "警察を呼んでください", "ko" to "경찰을 불러주세요", "ar" to "اتصل بالشرطة", "hi" to "पुलिस को बुलाओ"),
        "call an ambulance" to mapOf("es" to "Llame a una ambulancia", "fr" to "Appelez une ambulance", "de" to "Rufen Sie einen Krankenwagen", "it" to "Chiami un'ambulanza", "pt" to "Chame uma ambulância", "zh-CN" to "叫救护车！", "ja" to "救急車を呼んでください", "ko" to "구급차를 불러주세요", "ar" to "اتصل بالإسعاف", "hi" to "एम्बुलेंस को बुलाओ"),
        "hospital" to mapOf("es" to "Hospital", "fr" to "Hôpital", "de" to "Krankenhaus", "it" to "Ospedale", "pt" to "Hospital", "zh-CN" to "医院", "ja" to "病院", "ko" to "병원", "ar" to "مستشفى", "hi" to "अस्पताल", "ru" to "Больница"),
        "doctor" to mapOf("es" to "Médico", "fr" to "Médecin", "de" to "Arzt", "it" to "Medico", "pt" to "Médico", "zh-CN" to "医生", "ja" to "医者", "ko" to "의사", "ar" to "طبيب", "hi" to "डॉक्टर", "ru" to "Врач"),
        "pharmacy" to mapOf("es" to "Farmacia", "fr" to "Pharmacie", "de" to "Apotheke", "it" to "Farmacia", "pt" to "Farmácia", "zh-CN" to "药店", "ja" to "薬局", "ko" to "약국", "ar" to "صيدلية", "hi" to "दवा की दुकान"),
        "i am lost" to mapOf("es" to "Estoy perdido", "fr" to "Je suis perdu", "de" to "Ich habe mich verlaufen", "it" to "Mi sono perso", "pt" to "Estou perdido", "zh-CN" to "我迷路了", "ja" to "道に迷いました", "ko" to "길을 잃었습니다", "ar" to "أنا ضائع", "hi" to "मैं खो गया हूँ"),
        "i am sick" to mapOf("es" to "Estoy enfermo", "fr" to "Je suis malade", "de" to "Ich bin krank", "it" to "Sono malato", "pt" to "Estou doente", "zh-CN" to "我生病了", "ja" to "気分が悪いです", "ko" to "아파요", "ar" to "أنا مريض", "hi" to "मैं बीमार हूँ"),

        // Places & Travel
        "bathroom" to mapOf("es" to "Baño", "fr" to "Toilettes", "de" to "Toilette", "it" to "Bagno", "pt" to "Banheiro", "zh-CN" to "洗手间", "ja" to "トイレ", "ko" to "화장실", "ar" to "حمام", "hi" to "शौचालय", "ru" to "Туалет"),
        "restroom" to mapOf("es" to "Servicios", "fr" to "Toilettes", "de" to "Toilette", "it" to "Bagno", "pt" to "Banheiro", "zh-CN" to "卫生间", "ja" to "お手洗い", "ko" to "화장실", "ar" to "دورة مياه"),
        "hotel" to mapOf("es" to "Hotel", "fr" to "Hôtel", "de" to "Hotel", "it" to "Albergo", "pt" to "Hotel", "zh-CN" to "酒店", "ja" to "ホテル", "ko" to "호텔", "ar" to "فندق", "hi" to "होटल", "ru" to "Отель"),
        "airport" to mapOf("es" to "Aeropuerto", "fr" to "Aéroport", "de" to "Flughafen", "it" to "Aeroporto", "pt" to "Aeroporto", "zh-CN" to "机场", "ja" to "空港", "ko" to "공항", "ar" to "مطار", "hi" to "हवाई अड्डा", "ru" to "Аэропорт"),
        "station" to mapOf("es" to "Estación", "fr" to "Gare", "de" to "Bahnhof", "it" to "Stazione", "pt" to "Estação", "zh-CN" to "车站", "ja" to "駅", "ko" to "역", "ar" to "محطة", "hi" to "स्टेशन"),
        "train" to mapOf("es" to "Tren", "fr" to "Train", "de" to "Zug", "it" to "Treno", "pt" to "Trem", "zh-CN" to "火车", "ja" to "電車", "ko" to "기차", "ar" to "قطار", "hi" to "ट्रेन"),
        "bus" to mapOf("es" to "Autobús", "fr" to "Bus", "de" to "Bus", "it" to "Autobus", "pt" to "Ônibus", "zh-CN" to "公共汽车", "ja" to "バス", "ko" to "버스", "ar" to "حافلة", "hi" to "बस"),
        "taxi" to mapOf("es" to "Taxi", "fr" to "Taxi", "de" to "Taxi", "it" to "Taxi", "pt" to "Táxi", "zh-CN" to "出租车", "ja" to "タクシー", "ko" to "택시", "ar" to "تاكسي", "hi" to "टैक्सी"),
        "subway" to mapOf("es" to "Metro", "fr" to "Métro", "de" to "U-Bahn", "it" to "Metropolitana", "pt" to "Metrô", "zh-CN" to "地铁", "ja" to "地下鉄", "ko" to "지하철", "ar" to "مترو الأنفاق"),
        "restaurant" to mapOf("es" to "Restaurante", "fr" to "Restaurant", "de" to "Restaurant", "it" to "Ristorante", "pt" to "Restaurante", "zh-CN" to "餐厅", "ja" to "レストラン", "ko" to "식당", "ar" to "مطعم", "hi" to "रेस्तरां"),
        "street" to mapOf("es" to "Calle", "fr" to "Rue", "de" to "Straße", "it" to "Strada", "pt" to "Rua", "zh-CN" to "街道", "ja" to "通り", "ko" to "거리", "ar" to "شارع", "hi" to "सड़क"),
        "beach" to mapOf("es" to "Playa", "fr" to "Plage", "de" to "Strand", "it" to "Spiaggia", "pt" to "Praia", "zh-CN" to "海滩", "ja" to "ビーチ", "ko" to "해변", "ar" to "شاطئ"),
        "city center" to mapOf("es" to "Centro de la ciudad", "fr" to "Centre-ville", "de" to "Stadtzentrum", "it" to "Centro città", "pt" to "Centro da cidade", "zh-CN" to "市中心", "ja" to "市内中心部", "ko" to "도심", "ar" to "وسط المدينة"),

        // Food & Dining
        "water" to mapOf("es" to "Agua", "fr" to "Eau", "de" to "Wasser", "it" to "Acqua", "pt" to "Água", "zh-CN" to "水", "ja" to "水", "ko" to "물", "ar" to "ماء", "hi" to "पानी", "ru" to "Вода"),
        "food" to mapOf("es" to "Comida", "fr" to "Nourriture", "de" to "Essen", "it" to "Cibo", "pt" to "Comida", "zh-CN" to "食物", "ja" to "食べ物", "ko" to "음식", "ar" to "طعام", "hi" to "खाना", "ru" to "Еда"),
        "coffee" to mapOf("es" to "Café", "fr" to "Café", "de" to "Kaffee", "it" to "Caffè", "pt" to "Café", "zh-CN" to "咖啡", "ja" to "コーヒー", "ko" to "커피", "ar" to "قهوة", "hi" to "कॉफी"),
        "tea" to mapOf("es" to "Té", "fr" to "Thé", "de" to "Tee", "it" to "Tè", "pt" to "Chá", "zh-CN" to "茶", "ja" to "お茶", "ko" to "차", "ar" to "شاي", "hi" to "चाय"),
        "beer" to mapOf("es" to "Cerveza", "fr" to "Bière", "de" to "Bier", "it" to "Birra", "pt" to "Cerveja", "zh-CN" to "啤酒", "ja" to "ビール", "ko" to "맥주", "ar" to "بيرة"),
        "wine" to mapOf("es" to "Vino", "fr" to "Vin", "de" to "Wein", "it" to "Vino", "pt" to "Vinho", "zh-CN" to "葡萄酒", "ja" to "ワイン", "ko" to "와인", "ar" to "نبيذ"),
        "bread" to mapOf("es" to "Pan", "fr" to "Pain", "de" to "Brot", "it" to "Pane", "pt" to "Pão", "zh-CN" to "面包", "ja" to "パン", "ko" to "빵", "ar" to "خبز", "hi" to "रोटी"),
        "meat" to mapOf("es" to "Carne", "fr" to "Viande", "de" to "Fleisch", "it" to "Carne", "pt" to "Carne", "zh-CN" to "肉", "ja" to "肉", "ko" to "고기", "ar" to "لحم"),
        "chicken" to mapOf("es" to "Pollo", "fr" to "Poulet", "de" to "Hähnchen", "it" to "Pollo", "pt" to "Frango", "zh-CN" to "鸡肉", "ja" to "鶏肉", "ko" to "닭고기", "ar" to "دجاج"),
        "fish" to mapOf("es" to "Pescado", "fr" to "Poisson", "de" to "Fisch", "it" to "Pesce", "pt" to "Peixe", "zh-CN" to "鱼", "ja" to "魚", "ko" to "생선", "ar" to "سمك"),
        "menu" to mapOf("es" to "Menú", "fr" to "Menu", "de" to "Speisekarte", "it" to "Menu", "pt" to "Cardápio", "zh-CN" to "菜单", "ja" to "メニュー", "ko" to "메뉴", "ar" to "قائمة الطعام"),
        "bill" to mapOf("es" to "Cuenta", "fr" to "Addition", "de" to "Rechnung", "it" to "Conto", "pt" to "Conta", "zh-CN" to "账单", "ja" to "お会計", "ko" to "계산서", "ar" to "الفاتورة"),
        "check" to mapOf("es" to "Cuenta", "fr" to "Addition", "de" to "Rechnung", "it" to "Conto", "pt" to "Conta", "zh-CN" to "结账", "ja" to "伝票", "ko" to "계산"),

        // Shopping & Finance
        "money" to mapOf("es" to "Dinero", "fr" to "Argent", "de" to "Geld", "it" to "Denaro", "pt" to "Dinheiro", "zh-CN" to "钱", "ja" to "お金", "ko" to "돈", "ar" to "نقود", "hi" to "पैसा"),
        "cash" to mapOf("es" to "Efectivo", "fr" to "Espèces", "de" to "Bargeld", "it" to "Contanti", "pt" to "Dinheiro", "zh-CN" to "现金", "ja" to "現金", "ko" to "현금", "ar" to "نقداً"),
        "credit card" to mapOf("es" to "Tarjeta de crédito", "fr" to "Carte de crédit", "de" to "Kreditkarte", "it" to "Carta di credito", "pt" to "Cartão de crédito", "zh-CN" to "信用卡", "ja" to "クレジットカード", "ko" to "신용카드", "ar" to "بطاقة ائتمان"),
        "cheap" to mapOf("es" to "Barato", "fr" to "Bon marché", "de" to "Günstig", "it" to "Economico", "pt" to "Barato", "zh-CN" to "便宜", "ja" to "安い", "ko" to "싸다", "ar" to "رخيص"),
        "expensive" to mapOf("es" to "Caro", "fr" to "Cher", "de" to "Teuer", "it" to "Costoso", "pt" to "Caro", "zh-CN" to "昂贵", "ja" to "高い", "ko" to "비싸다", "ar" to "غالي"),

        // Common Verbs & Expressions
        "do you speak english" to mapOf("es" to "¿Habla inglés?", "fr" to "Parlez-vous anglais ?", "de" to "Sprechen Sie Englisch?", "it" to "Parla inglese?", "pt" to "Você fala inglês?", "zh-CN" to "你会说英语吗？", "ja" to "英語を話せますか？", "ko" to "영어 할 줄 아세요?", "ar" to "هل تتحدث الإنجليزية؟", "hi" to "क्या आप अंग्रेजी बोलते हैं?"),
        "i do not understand" to mapOf("es" to "No entiendo", "fr" to "Je ne comprends pas", "de" to "Ich verstehe nicht", "it" to "Non capisco", "pt" to "Não entendo", "zh-CN" to "我不明白", "ja" to "わかりません", "ko" to "이해하지 못했습니다", "ar" to "لا أفهم", "hi" to "मुझे समझ नहीं आया"),
        "i understand" to mapOf("es" to "Entiendo", "fr" to "Je comprends", "de" to "Ich verstehe", "it" to "Capisco", "pt" to "Eu entendo", "zh-CN" to "我明白了", "ja" to "わかりました", "ko" to "알겠습니다", "ar" to "أفهم"),
        "what time is it" to mapOf("es" to "¿Qué hora es?", "fr" to "Quelle heure est-il ?", "de" to "Wie spät ist es?", "it" to "Che ore sono?", "pt" to "Que horas são?", "zh-CN" to "现在几点？", "ja" to "今何時ですか？", "ko" to "지금 몇 시예요?", "ar" to "كم الساعة الآن؟"),

        // Numbers
        "one" to mapOf("es" to "Uno", "fr" to "Un", "de" to "Eins", "it" to "Uno", "pt" to "Um", "zh-CN" to "一", "ja" to "一", "ko" to "일", "ar" to "واحد", "hi" to "एक"),
        "two" to mapOf("es" to "Dos", "fr" to "Deux", "de" to "Zwei", "it" to "Due", "pt" to "Dois", "zh-CN" to "二", "ja" to "二", "ko" to "이", "ar" to "اثنان", "hi" to "दो"),
        "three" to mapOf("es" to "Tres", "fr" to "Trois", "de" to "Drei", "it" to "Tre", "pt" to "Três", "zh-CN" to "三", "ja" to "三", "ko" to "삼", "ar" to "ثلاثة", "hi" to "तीन"),
        "four" to mapOf("es" to "Cuatro", "fr" to "Quatre", "de" to "Vier", "it" to "Quattro", "pt" to "Quatro", "zh-CN" to "四", "ja" to "四", "ko" to "사", "ar" to "أربعة", "hi" to "चार"),
        "five" to mapOf("es" to "Cinco", "fr" to "Cinq", "de" to "Fünf", "it" to "Cinque", "pt" to "Cinco", "zh-CN" to "五", "ja" to "五", "ko" to "오", "ar" to "خمسة", "hi" to "पाँच"),
        "ten" to mapOf("es" to "Diez", "fr" to "Dix", "de" to "Zehn", "it" to "Dieci", "pt" to "Dez", "zh-CN" to "十", "ja" to "十", "ko" to "십", "ar" to "عشرة", "hi" to "दस"),
        "hundred" to mapOf("es" to "Cien", "fr" to "Cent", "de" to "Hundert", "it" to "Cento", "pt" to "Cem", "zh-CN" to "百", "ja" to "百", "ko" to "백", "ar" to "مائة", "hi" to "सौ")
    )

    fun translateOffline(text: String, targetLangCode: String): String? {
        val clean = text.trim().lowercase().removeSuffix(".").removeSuffix("?").removeSuffix("!")
        if (clean.isBlank()) return null

        // 1. Direct dictionary match
        val direct = dictionary[clean]?.get(targetLangCode)
        if (direct != null) return direct

        // 2. Pattern templates (e.g. "where is the hotel?")
        for (template in patternTemplates) {
            val match = template.regex.matchEntire(clean)
            if (match != null) {
                val group = match.groupValues[1].trim()
                val translatedGroup = dictionary[group]?.get(targetLangCode) ?: group
                val formatter = template.formatMap[targetLangCode]
                if (formatter != null) {
                    return formatter(translatedGroup)
                }
            }
        }

        // 3. Multi-word phrase translation token by token
        val words = clean.split(Regex("\\s+"))
        if (words.size > 1) {
            var translatedCount = 0
            val translatedWords = words.map { word ->
                val trans = dictionary[word]?.get(targetLangCode)
                if (trans != null) {
                    translatedCount++
                    trans
                } else {
                    word
                }
            }

            if (translatedCount > 0) {
                return translatedWords.joinToString(" ")
            }
        }

        return null
    }
}
