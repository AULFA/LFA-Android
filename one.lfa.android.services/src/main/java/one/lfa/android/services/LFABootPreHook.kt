package one.lfa.android.services

import android.content.Context
import android.os.Environment
import com.google.common.base.Preconditions
import org.nypl.simplified.boot.api.BootPreHookType
import org.slf4j.LoggerFactory
import java.io.File

/**
 * A trivial boot hook that renames the old profile directory to the 2019-12-03 standard used
 * by the upstream NYPL code.
 */

class LFABootPreHook : BootPreHookType {

  private val logger = LoggerFactory.getLogger(LFABootPreHook::class.java)

  private fun determineDiskDataDirectory(context: Context): File {

    /*
     * If external storage is mounted and is on a device that doesn't allow
     * the storage to be removed, use the external storage for data.
     */

    if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
      this.logger.debug("trying external storage")
      if (!Environment.isExternalStorageRemovable()) {
        val result = context.getExternalFilesDir(null)
        this.logger.debug("external storage is not removable, using it ({})", result)
        Preconditions.checkArgument(result!!.isDirectory, "Data directory {} is a directory", result)
        return result
      }
    }

    /*
     * Otherwise, use internal storage.
     */

    val result = context.filesDir
    this.logger.debug("no non-removable external storage, using internal storage ({})", result)
    Preconditions.checkArgument(result.isDirectory, "Data directory {} is a directory", result)
    return result
  }

  override fun execute(context: Context) {
    val baseDirectory =
      this.determineDiskDataDirectory(context)

    val newBase =
      File(baseDirectory, "v4.0")
    val oldProfiles =
      File(baseDirectory, "profiles")
    val newProfiles =
      File(newBase, "profiles")

    this.logger.debug("baseDirectory: {}", baseDirectory)
    this.logger.debug("newBase:       {}", newBase)
    this.logger.debug("oldProfiles:   {} (is directory {})", oldProfiles, oldProfiles.isDirectory)
    this.logger.debug("newProfiles:   {} (is directory {})", newProfiles, newProfiles.isDirectory)

    newBase.mkdirs()
    if (oldProfiles.isDirectory && !newProfiles.isDirectory) {
      this.logger.debug(
        "{} is a directory, and {} is not a directory, so renaming will be attempted",
        oldProfiles,
        newProfiles)

      val renamed = oldProfiles.renameTo(newProfiles)
      if (!renamed) {
        this.logger.error("rename {} to {} failed!", oldProfiles, newProfiles)
      }
    } else {
      this.logger.debug("pre boot hook did not need to run")
    }
  }
}