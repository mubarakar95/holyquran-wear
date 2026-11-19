package ws.quran.wear.data.repository

import android.content.Context
import android.content.res.AssetFileDescriptor
import kotlinx.serialization.json.*
import ws.quran.qvp.QvpOrnaments
import ws.quran.wear.data.model.*
import java.io.InputStreamReader

class QuranRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val defaultFatihah = Surah(1, 1, 7, "makkah", "الفاتحة", "Al-Fatihah", "The Opener")

    private var surahsCache: List<Surah> = listOf(defaultFatihah)
    private var pagesCache: List<QuranPage> = emptyList()
    private var rubusCache: List<RubuAlHizb> = emptyList()
    private var juzCache: List<Juz> = emptyList()
    private var ornamentsCache: QvpOrnaments? = null

    private var isInitialized = false

    @Synchronized
    fun ensureInitialized() {
        if (isInitialized) return

        try {
            context.assets.open("data/atlas.json").use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    val rootJson = json.parseToJsonElement(reader.readText()).jsonObject

                    // 1. Parse surahs
                    val surahsArray = rootJson["surahs"]?.jsonArray ?: JsonArray(emptyList())
                    surahsCache = surahsArray.map { element ->
                        val obj = element.jsonObject
                        Surah(
                            n = obj["n"]?.jsonPrimitive?.int ?: 0,
                            page = obj["page"]?.jsonPrimitive?.int ?: 1,
                            ayahs = obj["ayahs"]?.jsonPrimitive?.int ?: 0,
                            place = obj["place"]?.jsonPrimitive?.content ?: "",
                            arabic = obj["arabic"]?.jsonPrimitive?.content ?: "",
                            latin = obj["latin"]?.jsonPrimitive?.content ?: "",
                            english = obj["english"]?.jsonPrimitive?.content ?: ""
                        )
                    }

                    // 2. Parse pages: [pageNo, firstAyah, lastAyah, wordCount]
                    val pagesArray = rootJson["pages"]?.jsonArray ?: JsonArray(emptyList())
                    pagesCache = pagesArray.map { element ->
                        val arr = element.jsonArray
                        QuranPage(
                            pageNumber = arr[0].jsonPrimitive.int,
                            firstAyah = arr[1].jsonPrimitive.content,
                            lastAyah = arr[2].jsonPrimitive.content,
                            wordCount = arr[3].jsonPrimitive.int
                        )
                    }

                    // 3. Parse rubu_al_hizbs: [rubuNo, "surah:ayah", pageNo]
                    val rubusArray = rootJson["rubu_al_hizbs"]?.jsonArray ?: JsonArray(emptyList())
                    rubusCache = rubusArray.map { element ->
                        val arr = element.jsonArray
                        RubuAlHizb(
                            number = arr[0].jsonPrimitive.int,
                            surahAyah = arr[1].jsonPrimitive.content,
                            pageNumber = arr[2].jsonPrimitive.int
                        )
                    }

                    // 4. Derive the 30 Juz from rubu 1, 9, 17, 25, ... (each Juz has 8 rubus)
                    val surahMap = surahsCache.associateBy { it.n }
                    juzCache = (1..30).map { juzNumber ->
                        val startingRubuIndex = (juzNumber - 1) * 8
                        val startingRubu = rubusCache.getOrNull(startingRubuIndex)
                        val surahAyah = startingRubu?.surahAyah ?: "1:1"
                        val parts = surahAyah.split(":")
                        val surahNum = parts.getOrNull(0)?.toIntOrNull() ?: 1
                        val ayahNum = parts.getOrNull(1)?.toIntOrNull() ?: 1
                        val surah = surahMap[surahNum]
                        val surahArabic = surah?.arabic ?: ""
                        val surahLatin = surah?.latin ?: ""

                        Juz(
                            number = juzNumber,
                            startPage = startingRubu?.pageNumber ?: 1,
                            startSurahNumber = surahNum,
                            startAyahNumber = ayahNum,
                            startSurahArabic = surahArabic,
                            startSurahLatin = surahLatin
                        )
                    }

                    isInitialized = true
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("QuranWear", "Error initializing atlas.json", e)
        }
    }

    fun getSurahs(): List<Surah> {
        ensureInitialized()
        return surahsCache
    }

    fun getSurah(number: Int): Surah? {
        ensureInitialized()
        return surahsCache.find { it.n == number }
    }

    fun getSurahForPage(page: Int): Surah {
        ensureInitialized()
        // Find the last surah that starts on or before this page
        return surahsCache.lastOrNull { it.page <= page } ?: surahsCache.firstOrNull() ?: defaultFatihah
    }

    fun getPage(pageNumber: Int): QuranPage? {
        ensureInitialized()
        return pagesCache.getOrNull(pageNumber - 1)
    }

    fun getJuzList(): List<Juz> {
        ensureInitialized()
        return juzCache
    }

    fun getJuzForPage(page: Int): Juz {
        ensureInitialized()
        return juzCache.lastOrNull { it.startPage <= page } ?: juzCache.firstOrNull() ?: Juz(1, 1, 1, 1, "الفاتحة", "Al-Fatihah")
    }

    fun loadPageBytes(pageNumber: Int): ByteArray {
        val fileName = String.format("qvp/%03d.qvp", pageNumber)
        return try {
            val bytes = context.assets.open(fileName).use { it.readBytes() }
            android.util.Log.i("QuranWear", "Loaded page $pageNumber: ${bytes.size} bytes")
            bytes
        } catch (e: Exception) {
            android.util.Log.e("QuranWear", "Failed to load page $pageNumber", e)
            ByteArray(0)
        }
    }

    /**
     * Opens the AssetFileDescriptor for memory-mapping the QVP asset.
     */
    fun openPageAssetFd(pageNumber: Int): AssetFileDescriptor {
        val fileName = String.format("qvp/%03d.qvp", pageNumber)
        return context.assets.openFd(fileName)
    }

    /**
     * Loads and caches the QVO ornaments asset for dressing pages.
     */
    @Synchronized
    fun getOrnaments(): QvpOrnaments? {
        if (ornamentsCache != null && !ornamentsCache!!.isClosed) {
            return ornamentsCache
        }
        return try {
            val bytes = context.assets.open("data/ornaments.qvo").use { it.readBytes() }
            val orn = QvpOrnaments(bytes)
            android.util.Log.i("QuranWear", "Loaded ornaments.qvo successfully with ${orn.count} ornaments")
            ornamentsCache = orn
            orn
        } catch (e: Exception) {
            android.util.Log.e("QuranWear", "Could not load ornaments.qvo", e)
            null
        }
    }
}
