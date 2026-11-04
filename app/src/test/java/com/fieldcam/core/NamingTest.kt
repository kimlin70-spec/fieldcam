package com.fieldcam.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NamingTest {
    @Test
    fun dateStampIsMmDdYy() {
        assertEquals("110426", Naming.dateStamp(LocalDate.of(2026, 11, 4)))
        assertEquals("010131", Naming.dateStamp(LocalDate.of(2031, 1, 1)))
    }

    @Test
    fun routineFileName() {
        val t = Target.Routine("Boiler2")
        assertEquals("Pictures/FieldCam/Routine/", t.relativePath)
        assertEquals("110426_Boiler2_007.jpg", Naming.fileName(t.prefix("110426"), 7))
    }

    @Test
    fun overhaulFileName() {
        val t = Target.Overhaul("B2OH26", "ESP", "1A3")
        assertEquals("Pictures/FieldCam/B2OH26/", t.relativePath)
        assertEquals("ESP · 1A3", t.title)
        assertEquals("B2OH26_ESP_1A3_110426_002.jpg", Naming.fileName(t.prefix("110426"), 2))
    }

    @Test
    fun seqGrowsPast999() {
        assertEquals("p_1000.jpg", Naming.fileName("p_", 1000))
    }

    @Test
    fun sanitizeNames() {
        assertEquals("RW-shop", Naming.sanitize("RW shop"))
        assertEquals("Inlet-S", Naming.sanitize(" Inlet_S "))
        assertEquals("Boiler2", Naming.sanitize("보일러Boiler2!"))
        assertEquals("", Naming.sanitize("___"))
        assertTrue(Naming.isValid("1A1"))
        assertFalse(Naming.isValid("a b"))
        assertFalse(Naming.isValid(""))
    }

    @Test
    fun filterKeepsTrailingHyphenWhileTyping() {
        assertEquals("Inlet-", Naming.filter("Inlet-"))
        assertEquals("Inlet-", Naming.filter("Inlet "))
        assertEquals("RW-shop", Naming.filter("RW shop"))
        assertEquals("1A1", Naming.filter("1A1!"))
        assertEquals("Inlet", Naming.sanitize("Inlet-"))
    }

    @Test
    fun configSanitizedTrimsEveryName() {
        val c = Config(
            routine = listOf("Boiler1-", " RW shop"),
            projects = listOf(Project("B2OH26-", listOf(Group("ESP-", listOf("1A1", "-Inlet-S-"))))),
        ).sanitized()
        assertEquals(listOf("Boiler1", "RW-shop"), c.routine)
        assertEquals("B2OH26", c.projects[0].name)
        assertEquals("ESP", c.projects[0].groups[0].name)
        assertEquals(listOf("1A1", "Inlet-S"), c.projects[0].groups[0].items)
    }

    @Test
    fun seqParsing() {
        val p = "110426_Boiler2_"
        assertEquals(7, Naming.seqOf("110426_Boiler2_007.jpg", p))
        assertEquals(1000, Naming.seqOf("110426_Boiler2_1000.jpg", p))
        assertNull(Naming.seqOf("110426_Boiler2_007 (1).jpg", p))
        assertNull(Naming.seqOf("110426_Boiler22_007.jpg", p))
        assertNull(Naming.seqOf("110526_Boiler2_007.jpg", p))
    }

    @Test
    fun nextSeqUsesMaxOfFolderAndMemory() {
        val p = "110426_Boiler2_"
        val files = listOf("110426_Boiler2_001.jpg", "110426_Boiler2_003.jpg", "110426_STG_009.jpg", "other.jpg")
        assertEquals(4, Naming.nextSeq(files, p))
        assertEquals(6, Naming.nextSeq(files, p, floor = 5))
        assertEquals(1, Naming.nextSeq(emptyList(), p))
    }

    @Test
    fun prefixesDoNotCollideAcrossSimilarNames() {
        // "1A1" 과 "1A10" 처럼 한쪽이 다른 쪽의 접두어여도 뒤에 날짜가 붙어서 구분된다.
        val a = Target.Overhaul("P", "ESP", "1A1").prefix("110426")
        val b = Target.Overhaul("P", "ESP", "1A10").prefix("110426")
        assertNull(Naming.seqOf(Naming.fileName(b, 1), a))
    }
}
