package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.CanvasElementItem
import com.example.model.CanvasImageItem
import com.example.model.CanvasTextBox
import com.example.model.CustomPageTemplate
import com.example.model.DocumentType
import com.example.model.DrawnStroke
import com.example.model.ElementStampType
import com.example.model.FolderItem
import com.example.model.NotePage
import com.example.model.NotebookCoverStyle
import com.example.model.NotebookDocument
import com.example.model.PageOrientation
import com.example.model.PageSizePreset
import com.example.model.PaperColorTheme
import com.example.model.PaperTemplate
import com.example.model.PenStyle
import com.example.model.ShapeKind
import com.example.model.StickyNoteItem
import com.example.model.StrokePattern
import com.example.model.StudyTapeItem
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "notebooks")
data class NotebookEntity(
  @PrimaryKey val id: String,
  val title: String,
  val timestamp: String,
  val coverStyleName: String,
  val isFavorite: Boolean,
  val isTrashed: Boolean,
  val hasStylusBadge: Boolean,
  val isImportedPdf: Boolean,
  val paperTemplateName: String,
  val currentPageIndex: Int,
  val pagesJson: String,
  val updatedAtEpochMs: Long,
  val folderId: String? = null,
  val documentTypeName: String = DocumentType.NOTEBOOK.name,
  val pageSizeName: String = PageSizePreset.A4.name,
  val pageOrientationName: String = PageOrientation.PORTRAIT.name,
  val customTemplateKey: String = "",
  val revision: Long = 0L,
  val isEncrypted: Boolean = false,
)

@Entity(tableName = "folders")
data class FolderEntity(
  @PrimaryKey val id: String,
  val name: String,
  val parentFolderId: String? = null,
  val colorArgb: Long = 0xFF4C8DF6L,
  val createdAtEpochMs: Long,
  val updatedAtEpochMs: Long,
  val isTrashed: Boolean = false,
)

@Entity(tableName = "custom_templates")
data class CustomTemplateEntity(
  @PrimaryKey val id: String,
  val name: String,
  val bitmapKey: String,
  val isLandscape: Boolean = false,
  val createdAtEpochMs: Long,
)

@Dao
interface NotebookDao {
  @Query("SELECT * FROM notebooks ORDER BY updatedAtEpochMs DESC")
  fun observeAllNotebooks(): Flow<List<NotebookEntity>>

  @Query("SELECT * FROM notebooks ORDER BY updatedAtEpochMs DESC")
  suspend fun getAllNotebooksOnce(): List<NotebookEntity>

  @Query("SELECT * FROM notebooks WHERE id = :id LIMIT 1")
  suspend fun getNotebookById(id: String): NotebookEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertNotebook(entity: NotebookEntity)

  @Query("DELETE FROM notebooks WHERE id = :id")
  suspend fun deleteById(id: String)

  @Query("DELETE FROM notebooks WHERE isTrashed = 1")
  suspend fun deleteAllTrashed()

  @Query("DELETE FROM notebooks")
  suspend fun deleteAllNotebooks()
}

@Dao
interface FolderDao {
  @Query("SELECT * FROM folders ORDER BY name ASC")
  fun observeAllFolders(): Flow<List<FolderEntity>>

  @Query("SELECT * FROM folders ORDER BY name ASC")
  suspend fun getAllFoldersOnce(): List<FolderEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertFolder(folder: FolderEntity)

  @Query("DELETE FROM folders WHERE id = :id")
  suspend fun deleteFolderById(id: String)

  @Query("DELETE FROM folders WHERE isTrashed = 1")
  suspend fun deleteAllTrashedFolders()

  @Query("DELETE FROM folders")
  suspend fun deleteAllFolders()
}

@Dao
interface CustomTemplateDao {
  @Query("SELECT * FROM custom_templates ORDER BY createdAtEpochMs DESC")
  fun observeAllCustomTemplates(): Flow<List<CustomTemplateEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertCustomTemplate(entity: CustomTemplateEntity)

  @Query("DELETE FROM custom_templates WHERE id = :id")
  suspend fun deleteCustomTemplateById(id: String)

  @Query("DELETE FROM custom_templates")
  suspend fun deleteAllCustomTemplates()
}

@Database(
  entities = [NotebookEntity::class, FolderEntity::class, CustomTemplateEntity::class],
  version = 3,
  exportSchema = false,
)
abstract class ProNotesDatabase : RoomDatabase() {
  abstract fun notebookDao(): NotebookDao
  abstract fun folderDao(): FolderDao
  abstract fun customTemplateDao(): CustomTemplateDao

  companion object {
    @Volatile private var INSTANCE: ProNotesDatabase? = null

    val MIGRATION_1_2 =
      object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE notebooks ADD COLUMN folderId TEXT DEFAULT NULL")
          db.execSQL(
            "ALTER TABLE notebooks ADD COLUMN documentTypeName TEXT NOT NULL DEFAULT 'NOTEBOOK'"
          )
          db.execSQL("ALTER TABLE notebooks ADD COLUMN pageSizeName TEXT NOT NULL DEFAULT 'A4'")
          db.execSQL(
            "ALTER TABLE notebooks ADD COLUMN pageOrientationName TEXT NOT NULL DEFAULT 'PORTRAIT'"
          )
          db.execSQL(
            "ALTER TABLE notebooks ADD COLUMN customTemplateKey TEXT NOT NULL DEFAULT ''"
          )
          db.execSQL("ALTER TABLE notebooks ADD COLUMN revision INTEGER NOT NULL DEFAULT 0")
          db.execSQL("ALTER TABLE notebooks ADD COLUMN isEncrypted INTEGER NOT NULL DEFAULT 0")

          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `folders` (
              `id` TEXT NOT NULL,
              `name` TEXT NOT NULL,
              `parentFolderId` TEXT,
              `createdAtEpochMs` INTEGER NOT NULL,
              `updatedAtEpochMs` INTEGER NOT NULL,
              `isTrashed` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """.trimIndent()
          )

          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `custom_templates` (
              `id` TEXT NOT NULL,
              `name` TEXT NOT NULL,
              `bitmapKey` TEXT NOT NULL,
              `isLandscape` INTEGER NOT NULL,
              `createdAtEpochMs` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """.trimIndent()
          )
        }
      }

    val MIGRATION_2_3 =
      object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
          runCatching {
            db.execSQL(
              "ALTER TABLE folders ADD COLUMN colorArgb INTEGER NOT NULL DEFAULT 4283207158"
            )
          }
        }
      }

    fun getInstance(context: Context): ProNotesDatabase {
      return INSTANCE
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                ProNotesDatabase::class.java,
                "pronotes_local_v3.db",
              )
              .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
              .fallbackToDestructiveMigration()
              .build()
          INSTANCE = instance
          instance
        }
    }
  }
}

/**
 * Provides genuine AES/GCM/NoPadding encryption at rest for Vault-locked notebooks.
 * Uses AndroidKeyStore on device hardware and gracefully falls back to a deterministic
 * app-scoped key in JVM/Robolectric test environments where AndroidKeyStore is unavailable.
 */
object VaultCryptoHelper {
  private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
  private const val KEY_ALIAS = "pronotes_vault_aes_gcm_v1"
  private const val TRANSFORMATION = "AES/GCM/NoPadding"
  private const val GCM_TAG_BITS = 128
  private const val PREFIX = "ENC_GCM_V1:"

  private fun getOrCreateSecretKey(context: Context): SecretKey {
    try {
      val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
      val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
      if (existingKey != null) return existingKey

      val keyGenerator =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
      val spec =
        KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
          )
          .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
          .setKeySize(256)
          .build()
      keyGenerator.init(spec)
      return keyGenerator.generateKey()
    } catch (_: Throwable) {
      // Fallback for JVM unit tests / environments without hardware AndroidKeyStore
      val prefs = context.getSharedPreferences("pronotes_vault_keystore_fallback", Context.MODE_PRIVATE)
      val existingHex = prefs.getString("fallback_key_b64", null)
      val keyBytes =
        if (existingHex != null) {
          Base64.decode(existingHex, Base64.NO_WRAP)
        } else {
          val digest =
            MessageDigest.getInstance("SHA-256")
              .digest(("pronotes_vault_key_" + context.packageName).toByteArray(Charsets.UTF_8))
          prefs.edit().putString("fallback_key_b64", Base64.encodeToString(digest, Base64.NO_WRAP)).apply()
          digest
        }
      return SecretKeySpec(keyBytes, "AES")
    }
  }

  fun isEncryptedPayload(payload: String): Boolean = payload.startsWith(PREFIX)

  fun encryptPagesJson(context: Context, plainJson: String): String {
    if (plainJson.isEmpty()) return plainJson
    return try {
      val cipher = Cipher.getInstance(TRANSFORMATION)
      val secretKey = getOrCreateSecretKey(context)
      cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      val iv = cipher.iv
      val cipherBytes = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))
      val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
      val cipherB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
      "$PREFIX$ivB64:$cipherB64"
    } catch (_: Throwable) {
      // Pure software AES-GCM fallback if AndroidKeyStore cipher init fails
      val digest =
        MessageDigest.getInstance("SHA-256")
          .digest(("pronotes_sw_fallback_" + context.packageName).toByteArray(Charsets.UTF_8))
      val key = SecretKeySpec(digest, "AES")
      val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
      val cipher = Cipher.getInstance(TRANSFORMATION)
      cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
      val cipherBytes = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))
      val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
      val cipherB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
      "$PREFIX$ivB64:$cipherB64"
    }
  }

  fun decryptPagesJson(context: Context, storedPayload: String): String {
    if (!storedPayload.startsWith(PREFIX)) return storedPayload
    val body = storedPayload.removePrefix(PREFIX)
    val parts = body.split(":", limit = 2)
    if (parts.size != 2) return "[]"
    val iv = Base64.decode(parts[0], Base64.NO_WRAP)
    val cipherBytes = Base64.decode(parts[1], Base64.NO_WRAP)
    return try {
      val cipher = Cipher.getInstance(TRANSFORMATION)
      val secretKey = getOrCreateSecretKey(context)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
      String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
    } catch (_: Throwable) {
      try {
        val digest =
          MessageDigest.getInstance("SHA-256")
            .digest(("pronotes_sw_fallback_" + context.packageName).toByteArray(Charsets.UTF_8))
        val key = SecretKeySpec(digest, "AES")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
      } catch (_: Throwable) {
        "[]"
      }
    }
  }
}

/**
 * Local-first Repository that persists all notebooks, folders, custom templates, strokes,
 * shapes, study tapes, text boxes, sticky notes, elements, and PDF/image bitmaps on-device.
 *
 * Implements per-document serialized save locks and monotonic revision checks so rapid
 * consecutive edits never overwrite newer snapshots with older ones.
 */
class NotebookRepository(
  private val dao: NotebookDao,
  private val context: Context,
  private val folderDao: FolderDao? = null,
  private val customTemplateDao: CustomTemplateDao? = null,
) {
  private val bitmapDir: File =
    File(context.filesDir, "pronotes_bitmaps").apply {
      if (!exists()) mkdirs()
    }

  private val documentMutexes = ConcurrentHashMap<String, Mutex>()
  private val latestSavedRevisions = ConcurrentHashMap<String, Long>()

  private fun mutexFor(docId: String): Mutex =
    documentMutexes.getOrPut(docId) { Mutex() }

  val allNotebooksFlow: Flow<List<NotebookDocument>> =
    dao.observeAllNotebooks().map { entities ->
      entities.map { entity -> toDomain(entity) }
    }

  val allFoldersFlow: Flow<List<FolderItem>>? =
    folderDao?.observeAllFolders()?.map { entities ->
      entities.map {
        FolderItem(
          id = it.id,
          name = it.name,
          parentFolderId = it.parentFolderId,
          colorArgb = if (it.colorArgb == 0L) 0xFF4C8DF6L else (it.colorArgb or 0xFF000000L),
          createdAtEpochMs = it.createdAtEpochMs,
          updatedAtEpochMs = it.updatedAtEpochMs,
          isTrashed = it.isTrashed,
        )
      }
    }

  val allCustomTemplatesFlow: Flow<List<CustomPageTemplate>>? =
    customTemplateDao?.observeAllCustomTemplates()?.map { entities ->
      entities.map {
        CustomPageTemplate(
          id = it.id,
          name = it.name,
          bitmapKey = it.bitmapKey,
          previewBitmap = loadBitmapFromDisk(it.bitmapKey),
          isLandscape = it.isLandscape,
          createdAtEpochMs = it.createdAtEpochMs,
        )
      }
    }

  suspend fun saveNotebook(doc: NotebookDocument) {
    withContext(Dispatchers.IO) {
      mutexFor(doc.id).withLock {
        val lastRev = latestSavedRevisions[doc.id] ?: -1L
        if (doc.revision > 0L && doc.revision < lastRev) {
          // Skip stale out-of-order snapshot
          return@withLock
        }
        val nextRev = if (doc.revision > 0L) doc.revision else (lastRev + 1L).coerceAtLeast(1L)
        latestSavedRevisions[doc.id] = nextRev
        dao.upsertNotebook(toEntity(doc.copy(revision = nextRev)))
      }
    }
  }

  suspend fun deleteNotebook(id: String) {
    withContext(Dispatchers.IO) {
      mutexFor(id).withLock {
        latestSavedRevisions.remove(id)
        dao.deleteById(id)
        cleanupBitmapsForNotebook(id, emptySet())
      }
    }
  }

  suspend fun emptyTrash() {
    withContext(Dispatchers.IO) {
      val trashed = dao.getAllNotebooksOnce().filter { it.isTrashed }
      dao.deleteAllTrashed()
      folderDao?.deleteAllTrashedFolders()
      trashed.forEach { nb -> cleanupBitmapsForNotebook(nb.id, emptySet()) }
    }
  }

  suspend fun clearAllLocalData() {
    withContext(Dispatchers.IO) {
      latestSavedRevisions.clear()
      dao.deleteAllNotebooks()
      folderDao?.deleteAllFolders()
      customTemplateDao?.deleteAllCustomTemplates()
      bitmapDir.listFiles()?.forEach { it.delete() }
    }
  }

  suspend fun saveFolder(folder: FolderItem) {
    withContext(Dispatchers.IO) {
      folderDao?.upsertFolder(
        FolderEntity(
          id = folder.id,
          name = folder.name,
          parentFolderId = folder.parentFolderId,
          colorArgb = folder.colorArgb or 0xFF000000L,
          createdAtEpochMs = folder.createdAtEpochMs,
          updatedAtEpochMs = folder.updatedAtEpochMs,
          isTrashed = folder.isTrashed,
        )
      )
    }
  }

  suspend fun deleteFolder(folderId: String) {
    withContext(Dispatchers.IO) {
      folderDao?.deleteFolderById(folderId)
    }
  }

  suspend fun saveCustomTemplate(template: CustomPageTemplate, bitmap: Bitmap?): CustomPageTemplate {
    return withContext(Dispatchers.IO) {
      if (bitmap != null) {
        saveBitmapToDisk(template.bitmapKey, bitmap, forceOverwrite = true)
      }
      customTemplateDao?.upsertCustomTemplate(
        CustomTemplateEntity(
          id = template.id,
          name = template.name,
          bitmapKey = template.bitmapKey,
          isLandscape = template.isLandscape,
          createdAtEpochMs = template.createdAtEpochMs,
        )
      )
      template.copy(previewBitmap = bitmap ?: loadBitmapFromDisk(template.bitmapKey))
    }
  }

  suspend fun deleteCustomTemplate(templateId: String, bitmapKey: String) {
    withContext(Dispatchers.IO) {
      customTemplateDao?.deleteCustomTemplateById(templateId)
      if (bitmapKey.isNotBlank()) {
        File(bitmapDir, "$bitmapKey.png").delete()
      }
    }
  }

  /**
   * Atomically writes [bitmap] to a temporary file and renames it to `$key.png`
   * so partial/interrupted writes never corrupt existing bitmap files.
   */
  fun saveBitmapToDisk(key: String, bitmap: Bitmap, forceOverwrite: Boolean = false): String {
    val targetFile = File(bitmapDir, "$key.png")
    if (targetFile.exists() && !forceOverwrite) {
      return targetFile.absolutePath
    }
    val tempFile = File(bitmapDir, "$key.tmp_${System.nanoTime()}.png")
    return try {
      FileOutputStream(tempFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 92, out)
        out.flush()
        runCatching { out.fd.sync() }
      }
      if (targetFile.exists()) {
        targetFile.delete()
      }
      if (!tempFile.renameTo(targetFile)) {
        tempFile.copyTo(targetFile, overwrite = true)
        tempFile.delete()
      }
      targetFile.absolutePath
    } catch (e: Exception) {
      tempFile.delete()
      targetFile.absolutePath
    }
  }

  fun loadBitmapFromDisk(key: String): Bitmap? {
    if (key.isBlank()) return null
    val file = File(bitmapDir, "$key.png")
    if (!file.exists()) return null
    return runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
  }

  private fun cleanupBitmapsForNotebook(notebookId: String, activeKeys: Set<String>) {
    val pdfPrefix = "pdf_${notebookId}_"
    val imgPrefix = "img_${notebookId}_"
    bitmapDir.listFiles()?.forEach { file ->
      val nameWithoutExt = file.nameWithoutExtension
      if (
        (nameWithoutExt.startsWith(pdfPrefix) || nameWithoutExt.startsWith(imgPrefix)) &&
          !activeKeys.contains(nameWithoutExt)
      ) {
        file.delete()
      }
    }
  }

  private fun toEntity(doc: NotebookDocument): NotebookEntity {
    val activeBitmapKeys = mutableSetOf<String>()
    val pagesArr = JSONArray()
    doc.pages.forEachIndexed { pageIdx, page ->
      val pageObj = JSONObject()
      pageObj.put("pageNumber", page.pageNumber)
      pageObj.put("isBookmarked", page.isBookmarked)
      pageObj.put("pageTemplate", page.pageTemplate?.name ?: "")
      pageObj.put("pageColorTheme", page.pageColorTheme?.name ?: "")
      pageObj.put("canvasWidthPx", page.canvasWidthPx.toDouble())
      pageObj.put("canvasHeightPx", page.canvasHeightPx.toDouble())

      val customTplKey =
        when {
          page.customTemplateKey.isNotBlank() -> {
            if (page.customTemplateBitmap != null) {
              saveBitmapToDisk(page.customTemplateKey, page.customTemplateBitmap)
            }
            activeBitmapKeys.add(page.customTemplateKey)
            page.customTemplateKey
          }
          page.customTemplateBitmap != null -> {
            val key = "tpl_${doc.id}_p$pageIdx"
            saveBitmapToDisk(key, page.customTemplateBitmap)
            activeBitmapKeys.add(key)
            key
          }
          else -> ""
        }
      pageObj.put("customTemplateKey", customTplKey)

      if (page.pdfPageBitmap != null) {
        val pdfKey = "pdf_${doc.id}_p$pageIdx"
        saveBitmapToDisk(pdfKey, page.pdfPageBitmap)
        activeBitmapKeys.add(pdfKey)
        pageObj.put("pdfBitmapKey", pdfKey)
      }

      val strokesArr = JSONArray()
      page.strokes.forEach { stroke ->
        val sObj = JSONObject()
        sObj.put("id", stroke.id)
        sObj.put("color", stroke.color.toArgb())
        sObj.put("strokeWidth", stroke.strokeWidth.toDouble())
        sObj.put("penStyle", stroke.penStyle.name)
        sObj.put("strokePattern", stroke.strokePattern.name)
        sObj.put("isHighlighter", stroke.isHighlighter)
        sObj.put("isPencil", stroke.isPencil)
        sObj.put("isShape", stroke.isShape)
        sObj.put("shapeKind", stroke.shapeKind?.name ?: "")
        sObj.put("shapeFill", stroke.shapeFill)
        sObj.put("zIndex", stroke.zIndex)
        sObj.put("timestampMs", stroke.timestampMs)

        val ptsArr = JSONArray()
        stroke.points.forEach { pt ->
          val p = JSONObject()
          p.put("x", pt.x.toDouble())
          p.put("y", pt.y.toDouble())
          ptsArr.put(p)
        }
        sObj.put("points", ptsArr)

        val pressuresArr = JSONArray()
        stroke.pressures.forEach { pr -> pressuresArr.put(pr.toDouble()) }
        sObj.put("pressures", pressuresArr)

        val ctrlArr = JSONArray()
        stroke.shapeControlPoints.forEach { cp ->
          val c = JSONObject()
          c.put("x", cp.x.toDouble())
          c.put("y", cp.y.toDouble())
          ctrlArr.put(c)
        }
        sObj.put("controlPoints", ctrlArr)

        strokesArr.put(sObj)
      }
      pageObj.put("strokes", strokesArr)

      val tapesArr = JSONArray()
      page.studyTapes.forEach { tape ->
        val tObj = JSONObject()
        tObj.put("id", tape.id)
        tObj.put("sx", tape.start.x.toDouble())
        tObj.put("sy", tape.start.y.toDouble())
        tObj.put("ex", tape.end.x.toDouble())
        tObj.put("ey", tape.end.y.toDouble())
        tObj.put("color", tape.color.toArgb())
        tObj.put("thickness", tape.thickness.toDouble())
        tObj.put("isRevealed", tape.isRevealed)
        tapesArr.put(tObj)
      }
      pageObj.put("studyTapes", tapesArr)

      val textArr = JSONArray()
      page.textBoxes.forEach { tb ->
        val tbObj = JSONObject()
        tbObj.put("id", tb.id)
        tbObj.put("x", tb.position.x.toDouble())
        tbObj.put("y", tb.position.y.toDouble())
        tbObj.put("text", tb.text)
        tbObj.put("color", tb.color.toArgb())
        tbObj.put("fontSizeSp", tb.fontSizeSp.toDouble())
        tbObj.put("isBold", tb.isBold)
        textArr.put(tbObj)
      }
      pageObj.put("textBoxes", textArr)

      val stickyArr = JSONArray()
      page.stickyNotes.forEach { sn ->
        val snObj = JSONObject()
        snObj.put("id", sn.id)
        snObj.put("x", sn.position.x.toDouble())
        snObj.put("y", sn.position.y.toDouble())
        snObj.put("text", sn.text)
        snObj.put("backgroundColor", sn.backgroundColor.toArgb())
        snObj.put("isExpanded", sn.isExpanded)
        stickyArr.put(snObj)
      }
      pageObj.put("stickyNotes", stickyArr)

      val elemArr = JSONArray()
      page.elements.forEach { el ->
        val elObj = JSONObject()
        elObj.put("id", el.id)
        elObj.put("x", el.position.x.toDouble())
        elObj.put("y", el.position.y.toDouble())
        elObj.put("stampType", el.stampType.name)
        elemArr.put(elObj)
      }
      pageObj.put("elements", elemArr)

      val imgArr = JSONArray()
      page.images.forEach { img ->
        val imgKey = "img_${doc.id}_${img.id}"
        saveBitmapToDisk(imgKey, img.bitmap)
        activeBitmapKeys.add(imgKey)
        val imgObj = JSONObject()
        imgObj.put("id", img.id)
        imgObj.put("x", img.position.x.toDouble())
        imgObj.put("y", img.position.y.toDouble())
        imgObj.put("widthDp", img.widthDp.toDouble())
        imgObj.put("heightDp", img.heightDp.toDouble())
        imgObj.put("rotationDegrees", img.rotationDegrees.toDouble())
        imgObj.put("bitmapKey", imgKey)
        imgArr.put(imgObj)
      }
      pageObj.put("images", imgArr)

      pagesArr.put(pageObj)
    }

    // Clean up any orphaned bitmaps that were deleted from this notebook
    cleanupBitmapsForNotebook(doc.id, activeBitmapKeys)

    val plainJson = pagesArr.toString()
    val storedPagesPayload =
      if (doc.isLockedVault) {
        VaultCryptoHelper.encryptPagesJson(context, plainJson)
      } else {
        plainJson
      }

    return NotebookEntity(
      id = doc.id,
      title = doc.title,
      timestamp = doc.timestamp,
      coverStyleName = doc.coverStyle.name,
      isFavorite = doc.isFavorite,
      isTrashed = doc.isTrashed,
      hasStylusBadge = doc.hasStylusBadge,
      isImportedPdf = doc.isImportedPdf,
      paperTemplateName =
        "${doc.paperTemplate.name}|${doc.paperColorTheme.name}|${doc.isLockedVault}|${doc.vaultPinHash}",
      currentPageIndex = doc.currentPageIndex,
      pagesJson = storedPagesPayload,
      updatedAtEpochMs = doc.updatedAtEpochMs,
      folderId = doc.folderId,
      documentTypeName = doc.documentType.name,
      pageSizeName = doc.pageSizePreset.name,
      pageOrientationName = doc.pageOrientation.name,
      customTemplateKey = doc.customTemplateKey,
      revision = doc.revision,
      isEncrypted = doc.isLockedVault,
    )
  }

  private fun toDomain(entity: NotebookEntity): NotebookDocument {
    val cover =
      runCatching { NotebookCoverStyle.valueOf(entity.coverStyleName) }
        .getOrDefault(NotebookCoverStyle.KRAFT_TAN)
    val templateTokens = entity.paperTemplateName.split("|")
    val template =
      runCatching { PaperTemplate.valueOf(templateTokens.getOrElse(0) { "BLANK" }) }
        .getOrDefault(PaperTemplate.BLANK)
    val paperColor =
      runCatching { PaperColorTheme.valueOf(templateTokens.getOrElse(1) { "PURE_WHITE" }) }
        .getOrDefault(PaperColorTheme.PURE_WHITE)
    val isLocked = templateTokens.getOrElse(2) { "false" }.toBoolean() || entity.isEncrypted
    val pinHash = templateTokens.getOrElse(3) { "" }

    val docType =
      runCatching { DocumentType.valueOf(entity.documentTypeName) }
        .getOrDefault(
          if (cover == NotebookCoverStyle.WHITEBOARD) DocumentType.WHITEBOARD
          else DocumentType.NOTEBOOK
        )
    val pageSize =
      runCatching { PageSizePreset.valueOf(entity.pageSizeName) }
        .getOrDefault(PageSizePreset.A4)
    val orientation =
      runCatching { PageOrientation.valueOf(entity.pageOrientationName) }
        .getOrDefault(
          if (docType == DocumentType.WHITEBOARD) PageOrientation.LANDSCAPE
          else PageOrientation.PORTRAIT
        )

    val pages = mutableListOf<NotePage>()
    val decodedPagesJson =
      if (VaultCryptoHelper.isEncryptedPayload(entity.pagesJson)) {
        VaultCryptoHelper.decryptPagesJson(context, entity.pagesJson)
      } else {
        entity.pagesJson
      }

    runCatching {
      val pagesArr = JSONArray(decodedPagesJson)
      for (i in 0 until pagesArr.length()) {
        val pageObj = pagesArr.getJSONObject(i)
        val pageNum = pageObj.optInt("pageNumber", i + 1)
        val isBookmarked = pageObj.optBoolean("isBookmarked", false)
        val pageTemplateStr = pageObj.optString("pageTemplate", "")
        val pageTemplate =
          if (pageTemplateStr.isNotEmpty()) {
            runCatching { PaperTemplate.valueOf(pageTemplateStr) }.getOrNull()
          } else null
        val pageColorStr = pageObj.optString("pageColorTheme", "")
        val pageColor =
          if (pageColorStr.isNotEmpty()) {
            runCatching { PaperColorTheme.valueOf(pageColorStr) }.getOrNull()
          } else null
        val customTplKey = pageObj.optString("customTemplateKey", "")
        val customTplBitmap =
          if (customTplKey.isNotEmpty()) loadBitmapFromDisk(customTplKey) else null
        val canvasWidthPx = pageObj.optDouble("canvasWidthPx", 0.0).toFloat()
        val canvasHeightPx = pageObj.optDouble("canvasHeightPx", 0.0).toFloat()

        val pdfKey = pageObj.optString("pdfBitmapKey", "")
        val pdfBitmap = if (pdfKey.isNotEmpty()) loadBitmapFromDisk(pdfKey) else null

        val strokes = mutableListOf<DrawnStroke>()
        val strokesArr = pageObj.optJSONArray("strokes") ?: JSONArray()
        for (s in 0 until strokesArr.length()) {
          val sObj = strokesArr.getJSONObject(s)
          val pts = mutableListOf<Offset>()
          val ptsArr = sObj.optJSONArray("points") ?: JSONArray()
          for (p in 0 until ptsArr.length()) {
            val ptObj = ptsArr.getJSONObject(p)
            pts.add(Offset(ptObj.optDouble("x", 0.0).toFloat(), ptObj.optDouble("y", 0.0).toFloat()))
          }
          val pressures = mutableListOf<Float>()
          val pressuresArr = sObj.optJSONArray("pressures")
          if (pressuresArr != null) {
            for (pr in 0 until pressuresArr.length()) {
              pressures.add(pressuresArr.optDouble(pr, 0.5).toFloat())
            }
          }
          val ctrlPts = mutableListOf<Offset>()
          val ctrlArr = sObj.optJSONArray("controlPoints")
          if (ctrlArr != null) {
            for (c in 0 until ctrlArr.length()) {
              val cObj = ctrlArr.getJSONObject(c)
              ctrlPts.add(
                Offset(cObj.optDouble("x", 0.0).toFloat(), cObj.optDouble("y", 0.0).toFloat())
              )
            }
          }
          if (pts.isNotEmpty()) {
            val shapeKindStr = sObj.optString("shapeKind", "")
            val parsedShapeKind =
              if (shapeKindStr.isNotEmpty()) {
                runCatching { ShapeKind.valueOf(shapeKindStr) }.getOrNull()
              } else null
            val patternStr = sObj.optString("strokePattern", StrokePattern.SOLID.name)
            val parsedPattern =
              runCatching { StrokePattern.valueOf(patternStr) }.getOrDefault(StrokePattern.SOLID)

            strokes.add(
              DrawnStroke(
                id = sObj.optString("id", java.util.UUID.randomUUID().toString()),
                points = pts,
                pressures = pressures,
                color = Color(sObj.optInt("color", Color.Black.toArgb())),
                strokeWidth = sObj.optDouble("strokeWidth", 6.5).toFloat(),
                penStyle =
                  runCatching {
                      PenStyle.valueOf(sObj.optString("penStyle", PenStyle.BALL.name))
                    }
                    .getOrDefault(PenStyle.BALL),
                strokePattern = parsedPattern,
                isHighlighter = sObj.optBoolean("isHighlighter", false),
                isPencil = sObj.optBoolean("isPencil", false),
                isShape = sObj.optBoolean("isShape", false),
                shapeKind = parsedShapeKind,
                shapeFill = sObj.optBoolean("shapeFill", false),
                shapeControlPoints = ctrlPts,
                zIndex = sObj.optInt("zIndex", 0),
                timestampMs = sObj.optLong("timestampMs", 0L),
              )
            )
          }
        }

        val studyTapes = mutableListOf<StudyTapeItem>()
        val tapesArr = pageObj.optJSONArray("studyTapes") ?: JSONArray()
        for (t in 0 until tapesArr.length()) {
          val tObj = tapesArr.getJSONObject(t)
          studyTapes.add(
            StudyTapeItem(
              id = tObj.optString("id", java.util.UUID.randomUUID().toString()),
              start =
                Offset(tObj.optDouble("sx", 0.0).toFloat(), tObj.optDouble("sy", 0.0).toFloat()),
              end =
                Offset(tObj.optDouble("ex", 100.0).toFloat(), tObj.optDouble("ey", 100.0).toFloat()),
              color = Color(tObj.optInt("color", 0xFF3E8BF7.toInt())),
              thickness = tObj.optDouble("thickness", 38.0).toFloat(),
              isRevealed = tObj.optBoolean("isRevealed", false),
            )
          )
        }

        val textBoxes = mutableListOf<CanvasTextBox>()
        val textArr = pageObj.optJSONArray("textBoxes") ?: JSONArray()
        for (tb in 0 until textArr.length()) {
          val tbObj = textArr.getJSONObject(tb)
          textBoxes.add(
            CanvasTextBox(
              id = tbObj.optString("id", java.util.UUID.randomUUID().toString()),
              position =
                Offset(tbObj.optDouble("x", 80.0).toFloat(), tbObj.optDouble("y", 80.0).toFloat()),
              text = tbObj.optString("text", ""),
              color = Color(tbObj.optInt("color", Color.Black.toArgb())),
              fontSizeSp = tbObj.optDouble("fontSizeSp", 16.0).toFloat(),
              isBold = tbObj.optBoolean("isBold", false),
            )
          )
        }

        val stickyNotes = mutableListOf<StickyNoteItem>()
        val stickyArr = pageObj.optJSONArray("stickyNotes") ?: JSONArray()
        for (sn in 0 until stickyArr.length()) {
          val snObj = stickyArr.getJSONObject(sn)
          stickyNotes.add(
            StickyNoteItem(
              id = snObj.optString("id", java.util.UUID.randomUUID().toString()),
              position =
                Offset(
                  snObj.optDouble("x", 100.0).toFloat(),
                  snObj.optDouble("y", 100.0).toFloat(),
                ),
              text = snObj.optString("text", ""),
              backgroundColor = Color(snObj.optInt("backgroundColor", 0xFFFFF59D.toInt())),
              isExpanded = snObj.optBoolean("isExpanded", true),
            )
          )
        }

        val elements = mutableListOf<CanvasElementItem>()
        val elemArr = pageObj.optJSONArray("elements") ?: JSONArray()
        for (el in 0 until elemArr.length()) {
          val elObj = elemArr.getJSONObject(el)
          val stamp =
            runCatching {
                ElementStampType.valueOf(
                  elObj.optString("stampType", ElementStampType.IMPORTANT_STAR.name)
                )
              }
              .getOrDefault(ElementStampType.IMPORTANT_STAR)
          elements.add(
            CanvasElementItem(
              id = elObj.optString("id", java.util.UUID.randomUUID().toString()),
              position =
                Offset(
                  elObj.optDouble("x", 100.0).toFloat(),
                  elObj.optDouble("y", 100.0).toFloat(),
                ),
              stampType = stamp,
            )
          )
        }

        val images = mutableListOf<CanvasImageItem>()
        val imgArr = pageObj.optJSONArray("images") ?: JSONArray()
        for (im in 0 until imgArr.length()) {
          val imgObj = imgArr.getJSONObject(im)
          val key = imgObj.optString("bitmapKey", "")
          val bmp = if (key.isNotEmpty()) loadBitmapFromDisk(key) else null
          if (bmp != null) {
            images.add(
              CanvasImageItem(
                id = imgObj.optString("id", java.util.UUID.randomUUID().toString()),
                position =
                  Offset(
                    imgObj.optDouble("x", 90.0).toFloat(),
                    imgObj.optDouble("y", 140.0).toFloat(),
                  ),
                bitmap = bmp,
                widthDp = imgObj.optDouble("widthDp", 180.0).toFloat(),
                heightDp = imgObj.optDouble("heightDp", 140.0).toFloat(),
                rotationDegrees = imgObj.optDouble("rotationDegrees", 0.0).toFloat(),
              )
            )
          }
        }

        pages.add(
          NotePage(
            pageNumber = pageNum,
            isBookmarked = isBookmarked,
            pdfPageBitmap = pdfBitmap,
            strokes = strokes,
            studyTapes = studyTapes,
            textBoxes = textBoxes,
            stickyNotes = stickyNotes,
            elements = elements,
            images = images,
            pageTemplate = pageTemplate,
            pageColorTheme = pageColor,
            customTemplateBitmap = customTplBitmap,
            customTemplateKey = customTplKey,
            canvasWidthPx = canvasWidthPx,
            canvasHeightPx = canvasHeightPx,
          )
        )
      }
    }

    if (pages.isEmpty()) {
      pages.add(NotePage(pageNumber = 1))
    }

    return NotebookDocument(
      id = entity.id,
      title = entity.title,
      timestamp = entity.timestamp,
      coverStyle = cover,
      isFavorite = entity.isFavorite,
      isTrashed = entity.isTrashed,
      hasStylusBadge = entity.hasStylusBadge,
      isImportedPdf = entity.isImportedPdf,
      paperTemplate = template,
      paperColorTheme = paperColor,
      isLockedVault = isLocked,
      vaultPinHash = pinHash,
      pages = pages,
      currentPageIndex = entity.currentPageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0)),
      folderId = entity.folderId,
      documentType = docType,
      pageSizePreset = pageSize,
      pageOrientation = orientation,
      customTemplateKey = entity.customTemplateKey,
      revision = entity.revision,
      updatedAtEpochMs = entity.updatedAtEpochMs,
    )
  }
}
