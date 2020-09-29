package one.lfa.android.services

import android.content.Context
import org.nypl.simplified.boot.api.BootPreHookType
import org.slf4j.LoggerFactory
import java.io.File

/**
 * A trivial boot hook that copies any old external storage to the upstream internal storage
 * used by the NYPL app.
 */

class LFABootPreHook : BootPreHookType {

  private val logger = LoggerFactory.getLogger(LFABootPreHook::class.java)

  /**
   * A copying callback used to observe operations during unit testing.
   */

  var onCopy : (File, File) -> Unit = { _,_ ->

  }

  /**
   * A deletion callback used to observe operations during unit testing.
   */

  var onDelete : (File) -> Unit = { _ ->

  }

  /**
   * A directory creation callback used to observe operations during unit testing.
   */

  var onCreateDirectory: (File) -> Unit = { _ ->

  }

  override fun execute(context: Context) {
    try {
      this.logger.debug("starting")

      val externalDir = context.getExternalFilesDir(null)
      if (externalDir == null) {
        this.logger.debug("external storage is null, no migration needed")
        return
      }

      val internalProfilesDir =
        File(File(context.filesDir, "v4.0"), "profiles")
      val externalV4 =
        File(externalDir, "v4.0")
      val externalV4Profiles =
        File(externalV4, "profiles")
      val externalProfilesOld =
        File(externalDir, "profiles")

      if (externalProfilesOld.isDirectory) {
        this.logger.debug("old-style external storage detected: {}", externalProfilesOld)
        this.logger.debug("copying old-style external {} -> v4.0 external {}", externalProfilesOld, externalV4Profiles)
        this.copy(externalProfilesOld, externalV4Profiles)
        this.delete(externalProfilesOld)
        this.logger.debug("deleted old-style external {}", externalProfilesOld)
      }

      if (externalV4.isDirectory) {
        this.logger.debug("v4.0 external storage detected: {}", externalV4)
        this.logger.debug("copying v4.0 external {} -> internal v4.0 {}", externalV4Profiles, internalProfilesDir)
        this.copy(externalV4Profiles, internalProfilesDir)
        this.delete(externalV4)
        this.logger.debug("deleted v4.0 external {}", externalV4)
      }
    } catch (e: Exception) {
      this.logger.error("critical exception raised during boot hook: ", e)
    } finally {
      this.logger.debug("finished pre-boot hook")
    }
  }

  private fun delete(target: File) {
    this.announceDelete(target)
    target.deleteRecursively()
  }

  private fun copy(
    source: File,
    target: File
  ) {
    val everything =
      source.walkTopDown().toList()

    val directories =
      everything.filter { it.isDirectory }.sorted()
    val files =
      everything.filter { it.isFile }.sorted()

    for (sourceFile in directories) {
      val relative = sourceFile.toRelativeString(source)
      val targetFile = File(target, relative)
      try {
        this.announceCreateDirectory(targetFile)
        targetFile.mkdirs()
      } catch (e: Exception) {
        this.logger.error("unable to copy {} -> {}", sourceFile, targetFile)
      }
    }

    for (sourceFile in files) {
      val relative = sourceFile.toRelativeString(source)
      val targetFile = File(target, relative)

      try {
        this.announceCopy(sourceFile, targetFile)
        sourceFile.copyTo(targetFile, overwrite = true)
      } catch (e: Exception) {
        this.logger.error("unable to copy {} -> {}", sourceFile, targetFile)
      }
    }
  }

  private fun announceCreateDirectory(file: File) {
    try {
      this.logger.debug("mkdirs {}", file)
      this.onCreateDirectory(file)
    } catch (e: Exception) {
      // We don't care about exceptions here.
    }
  }

  private fun announceDelete(file: File) {
    try {
      this.logger.debug("delete {}", file)
      this.onDelete(file)
    } catch (e: Exception) {
      // We don't care about exceptions here.
    }
  }

  private fun announceCopy(
    source: File,
    target: File
  ) {
    try {
      this.logger.debug("copy {} -> {}", source, target)
      this.onCopy(source, target)
    } catch (e: Exception) {
      // We don't care about exceptions here.
    }
  }
}
