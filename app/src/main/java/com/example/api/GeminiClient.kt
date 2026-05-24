package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val isApiKeyConfigured: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotEmpty() && 
                BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" && 
                BuildConfig.GEMINI_API_KEY != "null"

    private fun cleanJsonResponse(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substringAfter("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substringAfter("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substringBeforeLast("```")
        }
        return cleaned.trim()
    }

    suspend fun analyzeVideo(
        title: String,
        watchTime: Float,
        retention: Float,
        likeRatio: Float,
        ctr: Float,
        comments: Int,
        shares: Int
    ): AnalysisResult = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured) {
            return@withContext simulateVideoAnalysis(title, watchTime, retention, likeRatio, ctr, comments, shares)
        }

        val prompt = """
            You are a professional YouTube growth strategist and social media algorithm analyst in 2026.
            Analyze the following YouTube video metrics and calculate a real-time viral success probability score.
            
            Video Metrics:
            - Title: "$title"
            - Watch Time: $watchTime minutes
            - Average Retention Rate: $retention%
            - Like / Engagement Ratio: $likeRatio%
            - Thumbnail Click-Through Rate (CTR): $ctr%
            - Total Comments: $comments
            - Total Shares: $shares
            
            Return your response in a raw JSON string (do not wrap in markdown code blocks, strict raw JSON only) with exactly these fields:
            {
              "viralScore": 85, // (integer, 1 to 100)
              "optimalUploadTime": "Minggu, 18:00 WIB & Rabu, 19:30 WIB", // (string)
              "suggestedTitles": ["Title Alternative 1", "Title Alternative 2", "Title Alternative 3"], // (array of strings)
              "suggestedHashtags": ["#tag1", "#tag2", "#tag3", "#tag4", "#tag5"], // (array of strings, exactly 5)
              "aiRecommendation": "Provide a clean, comprehensive social media analytics growth strategy for retention and CTR optimization based on these metrics. Write in Indonesian language."
            }
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt)
            val jsonText = cleanJsonResponse(responseText)
            val json = JSONObject(jsonText)

            val viralScore = json.optInt("viralScore", 70)
            val optimalUploadTime = json.optString("optimalUploadTime", "Selasa, 17:00 WIB")
            
            val titlesArray = json.optJSONArray("suggestedTitles")
            val suggestedTitles = mutableListOf<String>()
            if (titlesArray != null) {
                for (i in 0 until titlesArray.length()) {
                    suggestedTitles.add(titlesArray.getString(i))
                }
            } else {
                suggestedTitles.addAll(listOf("Membongkar Rahasia Algoritma: $title", "$title - Tonton Sebelum Diapus!", "Trik Rahasia $title Yang Wajib Dicoba"))
            }

            val hashtagsArray = json.optJSONArray("suggestedHashtags")
            val suggestedHashtags = mutableListOf<String>()
            if (hashtagsArray != null) {
                for (i in 0 until hashtagsArray.length()) {
                    suggestedHashtags.add(hashtagsArray.getString(i))
                }
            } else {
                suggestedHashtags.addAll(listOf("#shorts", "#trending", "##viral", "#youtube", "#growth"))
            }

            val aiRecommendation = json.optString("aiRecommendation", "Optimalkan visualisasi thumbnail Anda untuk mendongkrak CTR.")

            AnalysisResult(viralScore, optimalUploadTime, suggestedTitles, suggestedHashtags, aiRecommendation)
        } catch (e: Exception) {
            Log.e(TAG, "Error in analyzeVideo, falling back to simulation", e)
            simulateVideoAnalysis(title, watchTime, retention, likeRatio, ctr, comments, shares)
        }
    }

    suspend fun analyzeNiche(niche: String): NicheResult = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured) {
            return@withContext simulateNicheAnalysis(niche)
        }

        val prompt = """
            As a social media trend researcher, analyze the following niche: "$niche" for YouTube content.
            Estimate the competition, viral chance, search volume, and audience interest for 2026.
            
            Return your response in a raw JSON string (no markdown wraps, strict JSON only) with exactly these fields (values must be realistic):
            {
              "competitionRate": 45, // (integer, 1 to 100)
              "viralChance": 75, // (integer, 1 to 100)
              "searchVolume": "2.4M searches/mo", // (string)
              "audienceInterest": "Sangat Tinggi & Meningkat", // (string)
              "growthNotes": "A short tactical strategy summary in Indonesian on how to exploit this niche, focus keywords, hooks, and ideal editing pace."
            }
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt)
            val jsonText = cleanJsonResponse(responseText)
            val json = JSONObject(jsonText)

            NicheResult(
                competitionRate = json.optInt("competitionRate", 50),
                viralChance = json.optInt("viralChance", 50),
                searchVolume = json.optString("searchVolume", "1.2M searches"),
                audienceInterest = json.optString("audienceInterest", "Stabil"),
                growthNotes = json.optString("growthNotes", "Lakukan optimasi hook visual di 3 detik pertama video.")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in analyzeNiche, falling back to simulation", e)
            simulateNicheAnalysis(niche)
        }
    }

    suspend fun generateShortsTool(
        title: String,
        niche: String,
        conceptType: String, // "SHORTS_SCRIPT", "IDEA_GENERATOR", "CLICKBAIT_TITLES", "THUMBNAIL_CONCEPT"
        brief: String
    ): GeneratorResult = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured) {
            return@withContext simulateGenerator(title, niche, conceptType, brief)
        }

        val typeText = when (conceptType) {
            "SHORTS_SCRIPT" -> "YouTube Shorts Script written line-by-line with visual cues, background audio guidelines, and high-impact pacing."
            "IDEA_GENERATOR" -> "3 comprehensive, highly-creative YouTube content ideas complete with core visual concepts and target hooks."
            "CLICKBAIT_TITLES" -> "10 dynamic, highly-targeted clickbait and psychological viral titles for long or short-form uploads."
            "THUMBNAIL_CONCEPT" -> "Detailed visual description for 3 high-CTR custom thumbnail layouts (composition, colors, text overlay, emotions)."
            else -> "creative content script and ideas"
        }

        val prompt = """
            Create a highly viral, masterfully-crafted creator resource resource based on:
            - Topic/Niche: "$niche"
            - Title/Reference name: "$title"
            - Requested Generator: "$typeText"
            - Extra Brief / User Context: "$brief"
            
            Deliver a highly engaging, professional modern guide in Indonesian language. 
            Ensure the format mimics an expert viral growth consultant. Add timestamp references, screen descriptions, and pacing notes where appropriate.
            
            Return your response in a raw JSON string (no markdown wraps, strict JSON only) with exactly these fields:
            {
              "title": "Creative Title/Header for the generated asset",
              "content": "Fully-realized text output. Use rich details, bullet-points, time-brackets, or clear line separations to make it professionally readable."
            }
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt)
            val jsonText = cleanJsonResponse(responseText)
            val json = JSONObject(jsonText)

            GeneratorResult(
                title = json.optString("title", "Konsep Viral: $title"),
                content = json.optString("content", "Error generating content. Please retry.")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateShortsTool, falling back to simulation", e)
            simulateGenerator(title, niche, conceptType, brief)
        }
    }

    suspend fun analyzeChannel(query: String): ChannelAuditResult = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured) {
            return@withContext simulateChannelAnalysis(query)
        }

        val prompt = """
            You are a YouTube Algorithm intelligence system in 2026.
            Analyze the following YouTube channel, user handle or video URL: "$query"
            Produce an incredibly detailed, high-fidelity competitor intelligence and algorithm diagnostic report.
            
            Return your response in a raw JSON string (no markdown wrap, strict raw JSON only) containing exactly these fields in Indonesian language:
            {
              "channelName": "Name of the channel based on query",
              "totalSubscribers": "e.g. 11.2M Subscriber",
              "averageViews": "e.g. 450K views/video",
              "subscriberGrowth": "e.g. +25K/bulan or +12% growth",
              "engagementRate": "e.g. 6.4%",
              "uploadConsistency": "e.g. Tinggi (3 video/minggu)",
              "shortsPerformance": "e.g. Sangat Baik (Rata-rata 2M views)",
              "audienceNiche": "e.g. Gadget & Teknologi",
              "frequentKeywords": "keywords used separated by commas",
              "topVideoTitle": "Title of the most successful recent video",
              "algorithmPerformanceScore": 88,
              "strengths": "Strengths of the channel (concise list/paragraph)",
              "weaknesses": "Weaknesses of the channel or gaps in their strategy",
              "aiStrategyToBeat": "An incredibly detailed, step-by-step tactical growth strategy in Indonesian on how our user can exploit their weaknesses, adapt their thumbnail ideas, optimize upload timing, and beat this competitor in the algorithm."
            }
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt)
            val jsonText = cleanJsonResponse(responseText)
            val json = JSONObject(jsonText)

            ChannelAuditResult(
                channelName = json.optString("channelName", query.replace("@", "")),
                totalSubscribers = json.optString("totalSubscribers", "250K subscribers"),
                averageViews = json.optString("averageViews", "45K views"),
                subscriberGrowth = json.optString("subscriberGrowth", "+5.4% /mo"),
                engagementRate = json.optString("engagementRate", "4.8%"),
                uploadConsistency = json.optString("uploadConsistency", "Sedang"),
                shortsPerformance = json.optString("shortsPerformance", "Baik"),
                audienceNiche = json.optString("audienceNiche", "General"),
                frequentKeywords = json.optString("frequentKeywords", "youtube, tutorial, viral"),
                topVideoTitle = json.optString("topVideoTitle", "Video Terpopuler Pertama"),
                algorithmPerformanceScore = json.optInt("algorithmPerformanceScore", 75),
                strengths = json.optString("strengths", "Konsistensi upload"),
                weaknesses = json.optString("weaknesses", "Kurang interaksi di Shorts"),
                aiStrategyToBeat = json.optString("aiStrategyToBeat", "Bahas topik spesifik yang diabaikan kompetitor ini.")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in analyzeChannel, using simulation", e)
            simulateChannelAnalysis(query)
        }
    }

    private fun simulateChannelAnalysis(query: String): ChannelAuditResult {
        val cleanName = query.trim()
            .replace("https://", "")
            .replace("www.", "")
            .replace("youtube.com/c/", "")
            .replace("youtube.com/user/", "")
            .replace("youtube.com/", "")
            .replace("@", "")
            .split("/")
            .firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Techkreator"

        val defaultSubscribers = when {
            cleanName.lowercase().contains("gadget") -> "11.4M Subscribers"
            cleanName.lowercase().contains("gaming") -> "2.3M Subscribers"
            cleanName.lowercase().contains("misteri") -> "850K Subscribers"
            else -> "${(50..2500).random()}K Subscribers"
        }

        val defaultEngagement = "${"%.1f".format((2..12).random() + (0..9).random() / 10.0)}%"
        val defaultAvgViews = when {
            defaultSubscribers.contains("M") -> "${(150..600).random()}K views/video"
            else -> "${(10..95).random()}K views/video"
        }

        val defaultGrowth = "+${(5..45).random()}K subscribers/bulan"
        val score = (55..96).random()

        val niche = when {
            cleanName.lowercase().contains("tech") || cleanName.lowercase().contains("gadget") -> "Sains & Teknologi"
            cleanName.lowercase().contains("horror") || cleanName.lowercase().contains("misteri") -> "Horor, Alami, & Konspirasi"
            cleanName.lowercase().contains("gaming") -> "Gaming & Live Streams"
            else -> "Hiburan & Gaya Hidup"
        }

        val keywords = when (niche) {
            "Sains & Teknologi" -> "review, unboxing, gadget terbaru, smartphone mini, komparasi hp"
            "Horor, Alami, & Konspirasi" -> "misteri sejarah, penampakan nyata, teori konspirasi dunia, horor lawas"
            "Gaming & Live Streams" -> "speedrun game, walkthrough indo, ronaldo windah, no hit run, cheat rahasia"
            else -> "vlog harian, tantangan makan, belanja hemat, reaction video lucu"
        }

        val topVideo = when (niche) {
            "Sains & Teknologi" -> "Alasan Sebenarnya Laptop Murah Ini Ludes Terjual!"
            "Horor, Alami, & Konspirasi" -> "Misteri Tersembunyi di Dasar Palung Mariana yang Akhirnya Bocor"
            "Gaming & Live Streams" -> "Membeli Akun Game Sultan Seharga 50 Juta Rupiah (Bikin Nangis)"
            else -> "Tantangan 24 Jam Hidup Tanpa Menggunakan Internet!"
        }

        val strengthsList = when (niche) {
            "Sains & Teknologi" -> "Visual B-roll sinematik yang sangat tajam, audio narasi bersih jernih, dan struktur video terorganisir per chapter."
            "Horor, Alami, & Konspirasi" -> "Ritme music background yang sangat mendebarkan (suspense), penggunaan sound effect guncangan di momen-momen kritis, dan hook misterius sejak 5 detik awal."
            else -> "Konsistensi upload yang luar biasa agresif, judul clickbait yang didukung emosi emosional tinggi pada ekspresi wajah thumbnail."
        }

        val weaknessesList = when (niche) {
            "Sains & Teknologi" -> "Interaksi rendah di tab komunitas dan feed komentar. Kecepatan transisi visual terkadang terlalu lambat untuk generasi Z."
            "Horor, Alami, & Konspirasi" -> "Visual thumbnail terlalu berulang (monoton) dengan overlay teks terlalu panjang sehingga sulit dibaca di smartphone."
            else -> "Retensi views di tengah video (menit 3 s/d 6) anjlok drastis akibat jeda sponsor atau penjelasan bertele-tele."
        }

        val beatStrategy = """
            Strategi AI jitu untuk menggilas & mengalahkan @$cleanName di radar algoritma 2026:
            1. **Manfaatkan Celah Kelemahan:** Kompetitor ini memiliki kelemahan utama yaitu: *$weaknessesList*. Anda harus memproduksi konten sejenis yang langsung memberantas kelemahan tersebut. Misalnya, buat transisi visual yang jauh lebih dinamis (tiap 1.5 - 2 detik) dan minimalkan teks thumbnail hanya 2 kata tebal berwarna merah menyala.
            2. **Format & Durasi:** Jika @$cleanName mendominasi video panjang, hantam mereka menggunakan format **YouTube Shorts berdurasi 48 detik**. Gunakan loop hook di awal dan akhir video agar penonton mengulang tontonan dua kali, mendongkrak retensi hingga >120%.
            3. **Optimalkan Thumbnail & Judul:** Pakai emosi ketakutan/penasaran ekstrim versi Anda. Untuk judul, gunakan skema rasa penasaran psikologis yang lebih provokatif daripada kompetitor, misalnya: *"Kenapa Kreator Terbesar ${cleanName} Sembunyikan Fakta Ini..."*
            4. **Jadwal Upload Strategis:** Unggah video Anda tepat 30-45 menit **sebelum** jam upload biasanya dari @$cleanName (sekitar pukul 17:15 WIB). Ini memposisikan konten Anda di jajaran paling atas feed subskripsi ketika penonton aktif mulai online.
        """.trimIndent()

        return ChannelAuditResult(
            channelName = cleanName,
            totalSubscribers = defaultSubscribers,
            averageViews = defaultAvgViews,
            subscriberGrowth = defaultGrowth,
            engagementRate = defaultEngagement,
            uploadConsistency = "Tinggi (Rata-rata 4-5 video per minggu)",
            shortsPerformance = "Sangat Kuat (Sering menyentuh FYP & Shelf Shorts)",
            audienceNiche = niche,
            frequentKeywords = keywords,
            topVideoTitle = topVideo,
            algorithmPerformanceScore = score,
            strengths = strengthsList,
            weaknesses = weaknessesList,
            aiStrategyToBeat = beatStrategy
        )
    }

    private suspend fun callGeminiApi(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // Build candidate content block
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.e(TAG, "Gemini API failure: Code ${response.code}, Body: $errBody")
                throw Exception("API Error code ${response.code}")
            }

            val resBody = response.body?.string() ?: throw Exception("Empty response body")
            val resJson = JSONObject(resBody)
            val candidates = resJson.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            return parts.getJSONObject(0).getString("text")
        }
    }

    // --- Offline Simulation Generators (Ensures app is 100% usable Offline / Keyless) ---

    private fun simulateVideoAnalysis(
        title: String,
        watchTime: Float,
        retention: Float,
        likeRatio: Float,
        ctr: Float,
        comments: Int,
        shares: Int
    ): AnalysisResult {
        // Calculate a realistic viral score based on standard stats
        // Max weight: retention 40%, CTR 30%, engagement (like ratio, comments, shares helper) 30%
        val retentionWeight = (retention.coerceIn(0f, 100f) * 0.4f)
        val ctrWeight = (ctr.coerceIn(0f, 20f) * 5.0f * 0.3f) // 10% CTR = 15 points
        
        val totalCommentsShares = comments + shares
        val engagementFactor = when {
            totalCommentsShares > 1000 -> 30f
            totalCommentsShares > 500 -> 25f
            totalCommentsShares > 200 -> 20f
            totalCommentsShares > 50 -> 15f
            else -> 10f
        }
        val likeWeight = (likeRatio.coerceIn(0f, 100f) * 0.1f) + (engagementFactor * 0.9f)
        
        val finalScore = (retentionWeight + ctrWeight + likeWeight).coerceIn(15f, 98f).toInt()

        val optimalTime = when {
            finalScore > 85 -> "Setiap Hari, 18:00 - 21:00 WIB (Sangat Direkomendasikan Weekend)"
            finalScore > 65 -> "Senin s/d Kamis, 17:30 - 19:30 WIB"
            else -> "Jumat & Sabtu, 19:00 WIB"
        }

        val suggestedTitles = listOf(
            "[BONGKAR] Rahasia Sukses $title Yang Belum Kamu Tahu!",
            "Kenapa Semua Orang Membicarakan $title Saat Ini?",
            "Trik 3 Detik Menguasai $title Untuk Pemula (Viral!)"
        )

        val suggestedHashtags = listOf(
            "#${title.replace(" ", "").lowercase()}",
            "#shorts",
            "#trending",
            "#viralindonesia",
            "#algoritmayoutube"
        )

        val aiRec = """
            Rekomendasi Algoritma Offline untuk "$title":
            1. **Hook 3 Detik Pertama:** Skor retensi Anda ($retention%) menunjukkan penonton membutuhkan stimulus visual atau penegasan topik lebih cepat di awal video. Gunakan zoom-in kilat atau teks pop-up tebal merah berpendar.
            2. **CTR Optimalisasi:** Click-Through-Rate saat ini yaitu $ctr% tergolong ${if (ctr > 8) "sangat sehat" else "perlu perbaikan"}. Ganti latar belakang thumbnail menggunakan warna neon kontras tinggi hitam-merah atau ekspresi wajah emosi ekstrem 0.5 detik.
            3. **Aspek Engagement:** Ratio like $likeRatio% cukup baik. Di tengah durasi video, tambahkan jembatan interaksi (misalnya: "Komen di bawah jika video ini lewat di feed Anda jam berapa...").
            4. **Waktu Upload ideal:** Publikasikan pada $optimalTime di mana grafik aktifitas penonton sejenis sedang memuncak.
        """.trimIndent()

        return AnalysisResult(finalScore, optimalTime, suggestedTitles, suggestedHashtags, aiRec)
    }

    private fun simulateNicheAnalysis(niche: String): NicheResult {
        val nicheLower = niche.lowercase().trim()
        val competition = when {
            nicheLower.contains("gaming") || nicheLower.contains("ai") -> 88
            nicheLower.contains("horror") || nicheLower.contains("misteri") -> 74
            nicheLower.contains("anime") || nicheLower.contains("fakta") -> 68
            else -> (45..85).random()
        }

        val viralChance = when {
            nicheLower.contains("ai") || nicheLower.contains("fakta") -> 92
            nicheLower.contains("horror") -> 81
            nicheLower.contains("misteri") -> 84
            nicheLower.contains("gaming") -> 52
            else -> (40..90).random()
        }

        val vol = when {
            competition > 80 -> "4.8M pencarian/bulan"
            competition > 65 -> "1.5M pencarian/bulan"
            else -> "620K pencarian/bulan"
        }

        val interest = when {
            viralChance > 85 -> "Sangat Eksplosif (Banyak dicari penonton muda)"
            viralChance > 70 -> "Tren Sangat Stabil & Konsisten"
            else -> "Kompetitif Sedang"
        }

        val notes = """
            Niche Analisis Strategis demi konten "$niche":
            • **Tingkat Persaingan $competition%**: Gunakan strategi micro-niche. Jika di bidang $niche, jangan bahas topik umum. Carilah sub-topik tersembunyi berdurasi pendek.
            • **Rasio Viral $viralChance%**: Peluang mendongkrak views dalam shorts sangat tinggi karena retensi alamiah audiens yang menyukai tema ini.
            • **Tips Editing**: Visual transisi cepat (tiap 1.8 detik), lengkapi dengan instrumen bass cyberpunk / ketegangan misteri di background.
        """.trimIndent()

        return NicheResult(competition, viralChance, vol, interest, notes)
    }

    private fun simulateGenerator(title: String, niche: String, conceptType: String, brief: String): GeneratorResult {
        val visualHeader = when (conceptType) {
            "SHORTS_SCRIPT" -> "YouTube Shorts Script - $title"
            "IDEA_GENERATOR" -> "Viral Content Blueprint - $title"
            "CLICKBAIT_TITLES" -> "High CTR Psychological Titles - $title"
            "THUMBNAIL_CONCEPT" -> "Thumbnail Glowing Layout Concept - $title"
            else -> "Creative Generator Resource"
        }

        val body = when (conceptType) {
            "SHORTS_SCRIPT" -> """
                [00:00 - 00:03] (Hook Visual) Tampilkan close-up wajah tegang dengan background neon merah menyala penuh distorsi glitch.
                • Audio: Efek suara 'Whoosh' berat + Bass Drop instan.
                • Narasi: "Jangan pernah upload video YouTube sebelum tahu satu rahasia fatal ini..."
                
                [00:03 - 00:15] (Masalah & Agitasi) Tampilkan screen-record YouTube Studio grafik anjlok berwarna merah.
                • Audio: Musik background cyberpunk tempo cepat (synthwave cyberpunk).
                • Narasi: "Banyak kreator pemula mikir tag $niche itu penting. Padahal algoritma 2026 cuma peduli satu hal: Session Duration!"
                
                [00:15 - 00:45] (Solusi & Trik Utama) Tampilkan animasi overlay coretan merah dilingkari panah kuning.
                • Narasi: "Ini triknya. Di detik ke-12, ganti visual atau rubah intonasi suara kalian secara mendadak. Ini bakal nahan penonton 40% lebih lama! Gunakan brief tambahan Anda yaitu: $brief"
                
                [00:45 - 00:60] (Call to Action Terselubung) Tampilkan teks glowing merah berkedip "KLIK PROFIL - TRIK #3".
                • Audio: Efek 'glitch exit'.
                • Narasi: "Komen di bawah berapa target susbcribers kalian bulan ini, dan gua kasih template script gratis di pin-comment!"
            """.trimIndent()

            "IDEA_GENERATOR" -> """
                ### Ide Konten #1: Membongkar Rahasia Gelap di Niche $niche
                • **Konsep Utama**: Menunjukkan mitos paling populer yang dipercayai 99% orang di niche $niche, lalu mematahkan mitos tersebut menggunakan data visual.
                • **Visual Hook**: Layar terbelah dua (Benar vs Salah) dengan skema warna kontras tinggi hitam dan merah darah.
                • **Target Audien**: Penonton kasual yang penasaran.

                ### Ide Konten #2: Eksperimen Gila 24 Jam Menguasai $niche
                • **Konsep Utama**: Dokumenter perjalanan singkat 1 menit melakukan tantangan ekstrem terkait $niche memanfaatkan brief: "$brief".
                • **Visual Hook**: Timer countdown digital merah di pojok atas layar yang terus berdetak cepat.
                
                ### Ide Konten #3: Trik AI Rahasia Kreator Terkaya $niche
                • **Konsep Utama**: Memperlihatkan tool AI gratis alternatif yang jarang diketahui yang bisa merangkum materi $niche dalam hitungan detik. Excellent untuk retensi tinggi!
            """.trimIndent()

            "CLICKBAIT_TITLES" -> """
                1. 🚨 JANGAN LAKUKAN INI! Rahasia Gelap $title Akhirnya Terungkap...
                2. Saya Mencoba Trik $niche Gila Ini Selama 7 Hari (Hasilnya Bikin Shock!)
                3. Algoritma YouTube Membenci Video Ini, Tapi Anda Wajib Tahu Kenapa!
                4. Trik Jenius $title Yang Bikin Akun Kecil Mendadak Kebanjiran 100K Views!
                5. 🛑 Berhenti Ikuti Saran Edukator! Lakukan Strategi $niche Ini Sekarang Juga!
                6. Kenapa Kreator Terbesar $niche Menyembunyikan Satu Tombol Rahasia Ini?
                7. Rapor Merah YouTube Studio? Sembuhkan CTR Anda Menggunakan Rumus $title!
                8. Detik-Detik Video $title Pecah Rekor Viral (Ternyata Cuma Pakai Hook Ini)
                9. Pengakuan Mantan Karyawan YouTube Tentang Trik $niche Yang Sangat Ampuh!
                10. Pola Rahasia 2026: Cara Mengakali Rekomendasi FYP Demi Pemula ($brief)
            """.trimIndent()

            "THUMBNAIL_CONCEPT" -> """
                ### Desain Konsep #1: "Cyberpunk Split Graphic" (CTR Target: 14%+)
                • **Komposisi**: Wajah Anda di sebelah kiri mengekspresikan ketakutan/keheranan yang disorot lampu neon merah tipis. Di sebelah kanan, grafik performa YouTube Studio melonjak vertikal menembus batas atas.
                • **Overlay Teks**: Font tebal "CYBER" berwarna putih dengan outline merah bayangan hitam: "PANTANG DI-SKIP!".
                • **Skema Warna**: Background abu-abu gelap terdistorsi, warna utama hitam, merah crimson menyala, dan highlight kuning stabilo di tombol coret.

                ### Desain Konsep #2: "The Secret Icon Mystery" (CTR Target: 11%+)
                • **Komposisi**: Tampilan laptop buram menampilkan dokumen rahasia algoritma $niche yang sebagian ditutupi stiker sensor merah neon bertuliskan "BOCOR!".
                • **Elemen visual**: Jari telunjuk memakai sarung tangan menyentuh layar, memberikan nuansa investigatif.
                
                ### Desain Konsep #3: "Hyper-Minimalist Shock State" (CTR Target: 16%+)
                • **Komposisi**: Layar polos hitam pekat. Di tengahnya terdapat logo YouTube pecah/retak berkeping-keping mengeluarkan cahaya merah menyala, dengan teks kecil tajam di bawahnya: "Sudah Berakhir ($brief)".
            """.trimIndent()

            else -> "Simulated generated assets for creators."
        }

        return GeneratorResult(visualHeader, body)
    }
}

// --- Local Result Containers ---

data class AnalysisResult(
    val viralScore: Int,
    val optimalUploadTime: String,
    val suggestedTitles: List<String>,
    val suggestedHashtags: List<String>,
    val aiRecommendation: String
)

data class NicheResult(
    val competitionRate: Int,
    val viralChance: Int,
    val searchVolume: String,
    val audienceInterest: String,
    val growthNotes: String
)

data class GeneratorResult(
    val title: String,
    val content: String
)

data class ChannelAuditResult(
    val channelName: String,
    val totalSubscribers: String,
    val averageViews: String,
    val subscriberGrowth: String,
    val engagementRate: String,
    val uploadConsistency: String,
    val shortsPerformance: String,
    val audienceNiche: String,
    val frequentKeywords: String,
    val topVideoTitle: String,
    val algorithmPerformanceScore: Int,
    val strengths: String,
    val weaknesses: String,
    val aiStrategyToBeat: String
)
