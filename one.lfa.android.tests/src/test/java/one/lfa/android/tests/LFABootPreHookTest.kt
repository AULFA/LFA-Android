package one.lfa.android.tests

import android.content.Context
import one.lfa.android.services.LFABootPreHook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File

class LFABootPreHookTest {

  private val text0 =
    "We Mandalorians have a saying. A warrior who doesn't hope for battle has no hope during battle."
  private val text1 =
    "One may learn a great deal of a people by the stories they tell of others."
  private val text1r =
    "No one can say where his path will take him, even for the duration of a single day."
  private val text2 =
    "The most beautiful form of mastery is the art of letting go."
  private val text2r =
    "Surely you must understand that the means are no less important than the ends."

  private val directories: MutableList<File> = mutableListOf()
  private val copies: MutableList<File> = mutableListOf()
  private val deletions: MutableList<File> = mutableListOf()
  private lateinit var context: Context
  private lateinit var hook: LFABootPreHook

  @Before
  fun testSetup() {
    this.hook =
      LFABootPreHook()
    this.context =
      Mockito.mock(Context::class.java)

    this.directories.clear()
    this.copies.clear()
    this.deletions.clear()

    this.hook.onCopy = this::countCopy
    this.hook.onCreateDirectory = this::countCreateDirectory
    this.hook.onDelete = this::countDelete
  }

  /**
   * If there's no external storage, nothing is copied or deleted.
   */

  @Test
  fun testNullExternalStorage() {
    this.hook.execute(this.context)

    assertEquals(0, this.copies.size)
    assertEquals(0, this.deletions.size)
  }

  /**
   * v4.0 externals are copied to v4.0 internals.
   */

  @Test
  fun testExternalV4ToInternalV4() {
    val newInt = File(TestDirectories.temporaryDirectory(), "v4.0")
    val newProfiles = File(newInt, "profiles")
    newProfiles.mkdirs()

    val oldV4 = File(TestDirectories.temporaryDirectory(), "v4.0")
    val oldV4Profiles = File(oldV4, "profiles")
    oldV4Profiles.mkdirs()

    assertNotEquals(newInt, oldV4)

    File(oldV4Profiles, "0.txt")
      .writeText(this.text0)
    File(oldV4Profiles, "1.txt")
      .writeText(this.text1)
    File(newProfiles, "1.txt")
      .writeText(this.text1r)

    Mockito.`when`(this.context.filesDir)
      .thenReturn(newInt.parentFile)
    Mockito.`when`(this.context.getExternalFilesDir(null))
      .thenReturn(oldV4.parentFile)

    this.hook.execute(this.context)

    assertEquals(File(newInt, "profiles"), this.directories.removeAt(0))
    assertEquals(0, this.directories.size)

    assertEquals(File(oldV4Profiles, "0.txt"), this.copies.removeAt(0))
    assertEquals(File(oldV4Profiles, "1.txt"), this.copies.removeAt(0))
    assertEquals(0, this.copies.size)

    assertEquals(oldV4, this.deletions.removeAt(0))
    assertEquals(0, this.deletions.size)

    assertEquals(false, File(oldV4, "0.txt").exists())
    assertEquals(false, File(oldV4, "1.txt").exists())

    assertEquals(this.text0, File(newProfiles, "0.txt").readText())
    assertEquals(this.text1, File(newProfiles, "1.txt").readText())
  }

  /**
   * Old style externals are copied to v4.0 internals, going via externals first.
   */

  @Test
  fun testOldExternalToInternalV4() {
    val newInt = File(TestDirectories.temporaryDirectory(), "v4.0")
    val newProfiles = File(newInt, "profiles")
    newProfiles.mkdirs()

    val oldBase = TestDirectories.temporaryDirectory()
    val old = File(oldBase, "profiles")
    val oldV4 = File(oldBase, "v4.0")
    val oldV4Profiles = File(oldV4, "profiles")
    oldV4Profiles.mkdirs()
    old.mkdirs()

    assertNotEquals(newInt, oldV4)
    assertNotEquals(newInt, old)
    assertNotEquals(oldV4, old)

    File(old, "0.txt")
      .writeText(this.text0)
    File(old, "1.txt")
      .writeText(this.text1)
    File(old, "2.txt")
      .writeText(this.text2r)

    File(newInt, "1.txt")
      .writeText(this.text1r)
    File(oldV4, "2.txt")
      .writeText(this.text2)

    Mockito.`when`(this.context.filesDir)
      .thenReturn(newInt.parentFile)
    Mockito.`when`(this.context.getExternalFilesDir(null))
      .thenReturn(old.parentFile)

    this.hook.execute(this.context)

    assertEquals(oldV4Profiles, this.directories.removeAt(0))
    assertEquals(newProfiles, this.directories.removeAt(0))
    assertEquals(0, this.directories.size)

    assertEquals(File(old, "0.txt"), this.copies.removeAt(0))
    assertEquals(File(old, "1.txt"), this.copies.removeAt(0))
    assertEquals(File(old, "2.txt"), this.copies.removeAt(0))
    assertEquals(File(oldV4Profiles, "0.txt"), this.copies.removeAt(0))
    assertEquals(File(oldV4Profiles, "1.txt"), this.copies.removeAt(0))
    assertEquals(File(oldV4Profiles, "2.txt"), this.copies.removeAt(0))
    assertEquals(0, this.copies.size)

    assertEquals(old, this.deletions.removeAt(0))
    assertEquals(oldV4, this.deletions.removeAt(0))
    assertEquals(0, this.deletions.size)

    assertEquals(false, File(old, "0.txt").exists())
    assertEquals(false, File(old, "1.txt").exists())
    assertEquals(false, File(old, "2.txt").exists())

    assertEquals(false, File(oldV4Profiles, "0.txt").exists())
    assertEquals(false, File(oldV4Profiles, "1.txt").exists())
    assertEquals(false, File(oldV4Profiles, "2.txt").exists())

    assertEquals(this.text0, File(newProfiles, "0.txt").readText())
    assertEquals(this.text1, File(newProfiles, "1.txt").readText())
    assertEquals(this.text2r, File(newProfiles, "2.txt").readText())
  }

  private fun countDelete(file: File) {
    this.deletions.add(file)
  }

  private fun countCopy(source: File, target: File) {
    this.copies.add(source)
  }

  private fun countCreateDirectory(target: File) {
    this.directories.add(target)
  }
}
