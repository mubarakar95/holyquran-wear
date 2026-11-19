package ws.quran.wear

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class AtlasJsonValidationTest {

    @Test
    fun testAtlasJsonIntegrity() {
        val atlasFile = File("src/main/assets/data/atlas.json")
        assertTrue("atlas.json must exist in assets", atlasFile.exists())

        val json = Json { ignoreUnknownKeys = true }
        val root = FileInputStream(atlasFile).use { stream ->
            InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                json.parseToJsonElement(reader.readText()).jsonObject
            }
        }

        // 1. Verify 114 Surahs
        val surahs = root["surahs"]?.jsonArray
        assertNotNull("Surahs array must be present", surahs)
        assertEquals("Must contain 114 surahs", 114, surahs!!.size)

        // Surah 1: Al-Fatihah
        val fatihah = surahs[0].jsonObject
        assertEquals(1, fatihah["n"]?.jsonPrimitive?.int)
        assertEquals(1, fatihah["page"]?.jsonPrimitive?.int)
        assertEquals(7, fatihah["ayahs"]?.jsonPrimitive?.int)
        assertEquals("الفاتحة", fatihah["arabic"]?.jsonPrimitive?.content)

        // Surah 18: Al-Kahf
        val kahf = surahs[17].jsonObject
        assertEquals(18, kahf["n"]?.jsonPrimitive?.int)
        assertEquals(293, kahf["page"]?.jsonPrimitive?.int)
        assertEquals(110, kahf["ayahs"]?.jsonPrimitive?.int)

        // Surah 114: An-Nas
        val nas = surahs[113].jsonObject
        assertEquals(114, nas["n"]?.jsonPrimitive?.int)
        assertEquals(604, nas["page"]?.jsonPrimitive?.int)
        assertEquals(6, nas["ayahs"]?.jsonPrimitive?.int)

        // 2. Verify 604 Pages
        val pages = root["pages"]?.jsonArray
        assertNotNull("Pages array must be present", pages)
        assertEquals("Must contain 604 pages", 604, pages!!.size)

        // 3. Verify 240 Rubu al-Hizbs
        val rubus = root["rubu_al_hizbs"]?.jsonArray
        assertNotNull("Rubu array must be present", rubus)
        assertEquals("Must contain 240 rubu al-hizb entries", 240, rubus!!.size)
    }
}
