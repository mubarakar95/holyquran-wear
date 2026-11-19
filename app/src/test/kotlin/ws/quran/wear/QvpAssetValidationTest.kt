package ws.quran.wear

import ws.quran.qvp.QvpColor
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.FileInputStream

class QvpAssetValidationTest {

    @Test
    fun testAll604PagesExistAndHaveValidQvp1Headers() {
        val qvpDir = File("src/main/assets/qvp")
        assertTrue("Assets QVP directory must exist", qvpDir.exists() && qvpDir.isDirectory)

        val files = qvpDir.listFiles { _, name -> name.endsWith(".qvp") } ?: emptyArray()
        assertEquals("Must contain exactly 604 QVP page files", 604, files.size)

        for (page in 1..604) {
            val fileName = String.format("%03d.qvp", page)
            val file = File(qvpDir, fileName)
            assertTrue("Page file $fileName must exist", file.exists())
            assertTrue("Page file $fileName must be non-empty", file.length() > 1024)

            // Read the first 4 bytes to verify magic bytes QVP1 (0x51 0x56 0x50 0x31)
            FileInputStream(file).use { fis ->
                val magic = ByteArray(4)
                val read = fis.read(magic)
                assertEquals(4, read)
                val magicStr = String(magic, Charsets.US_ASCII)
                assertEquals("Magic bytes must be QVP1 for $fileName", "QVP1", magicStr)
            }
        }
    }

    @Test
    fun testOrnamentsAssetExistsAndHasValidQvo1Header() {
        val ornamentsFile = File("src/main/assets/data/ornaments.qvo")
        assertTrue("Ornaments file must exist", ornamentsFile.exists())
        assertTrue("Ornaments file must be non-empty", ornamentsFile.length() > 1024)

        FileInputStream(ornamentsFile).use { fis ->
            val magic = ByteArray(4)
            val read = fis.read(magic)
            assertEquals(4, read)
            val magicStr = String(magic, Charsets.US_ASCII)
            assertEquals("Magic bytes must be QVO1", "QVO1", magicStr)
        }
    }

    @Test
    fun testQvpColorRgbaAndArgbConsistency() {
        // Light mode ink color
        val lightRgba = QvpColor.parse("#231F20")
        assertEquals(0x231F20FF, lightRgba.toLong() and 0xFFFFFFFFL)
        val lightArgb = QvpColor.argb(lightRgba)
        assertEquals(0xFF231F20.toInt(), lightArgb)

        // Dark mode ink color
        val darkRgba = QvpColor.parse("#E8D5B5")
        assertEquals(0xE8D5B5FF.toInt(), darkRgba)
        val darkArgb = QvpColor.argb(darkRgba)
        assertEquals(0xFFE8D5B5.toInt(), darkArgb)
    }
}
