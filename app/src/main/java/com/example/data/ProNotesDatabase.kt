package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.example.model.CanvasElementItem
import com.example.model.CanvasImageItem
import com.example.model.CanvasTextBox
import com.example.model.DrawnStroke
import com.example.model.ElementStampType
import com.example.model.NotePage
import com.example.model.NotebookCoverStyle
import com.example.model.NotebookDocument
import com.example.model.PaperColorTheme
import com.example.model.PaperTemplate
import com.example.model.PenStyle
import com.example.model.ShapeKind
import com.example.model.StickyNoteItem
import com.example.model.StrokePattern
import com.example.model.StudyTapeItem
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "notebooks")
data class NotebookEntity(
  @PrimaryKey val id: String,
  val title: String,
  val timestamp: String,
  val updatedAtEpochMs: Long,
  val coverStyleName: String,
  val isFavorite: Boolean,
  val isTrashed: Boolean,
  val hasStylusBadge: Boolean,
  val isImportedPdf: Boolean,
  val paperTemplateName: String,
  val currentPageIndex: Int,
  val pagesJson: String,
)

@Dao
interface NotebookDao {
  @Query("SELECT * FROM notebooks ORDER BY updatedAtEpochMs DESC")
  fun getAllNotebooksFlow(): Flow<List<NotebookEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertNotebook(entity: NotebookEntity)

  @Query("DELETE FROM notebooks WHERE id = :id")
  suspend fun deleteNotebookById(id: String)

  @Query("DELETE FROM notebooks WHERE isTrashed = 1")
  suspend fun emptyTrash()

  @Query("DELETE FROM notebooks")
  suspend fun deleteAllNotebooks()
}

@Database(entities = [NotebookEntity::class], version = 1, exportSchema = false)
abstract class ProNotesDatabase : RoomDatabase() {
  abstract fun notebookDao(): NotebookDao

  companion object {
    @Volatile private var INSTANCE: ProNotesDatabase? = null

    fun getInstance(context: Context): ProNotesDatabase {
      return INSTANCE
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                ProNotesDatabase::class.java,
                "pronotes_local_v3.db",
              )
              .fallbackToDestructiveMigration(dropAllTables = true)
              .build()
          INSTANCE = instance
          instance
        }
    }
  }
}

class NotebookRepository(
  private val notebookDao: NotebookDao,
  private val appContext: Context,
) {
  private val bitmapCache = mutableMapOf<String, Bitmap>()

  val allNotebooksFlow: Flow<List<NotebookDocument>> =
    notebookDao.getAllNotebooksFlow().map { entities ->
      entities.map { entity -> entityToDomain(entity) }
    }

  suspend fun saveNotebook(doc: NotebookDocument) {
    withContext(Dispatchers.IO) {
      val entity = domainToEntity(doc)
      notebookDao.upsertNotebook(entity)
    }
  }

  suspend fun deleteNotebook(id: String) {
    withContext(Dispatchers.IO) {
      notebookDao.deleteNotebookById(id)
    }
  }

  suspend fun emptyTrash() {
    withContext(Dispatchers.IO) {
      notebookDao.emptyTrash()
    }
  }

  suspend fun clearAllLocalData() {
    withContext(Dispatchers.IO) {
      notebookDao.deleteAllNotebooks()
      bitmapCache.clear()
      File(appContext.filesDir, "page_bitmaps").deleteRecursively()
    }
  }

  private fun saveBitmapToDisk(key: String, bitmap: Bitmap): String {
    bitmapCache[key] = bitmap
    val dir = File(appContext.filesDir, "page_bitmaps")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "$key.png")
    if (!file.exists()) {
      runCatching {
        FileOutputStream(file).use { out ->
          bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
      }
    }
    return file.absolutePath
  }

  private fun loadBitmapFromDisk(key: String, path: String): Bitmap? {
    bitmapCache[key]?.let { return it }
    if (path.isBlank()) return null
    val file = File(path)
    if (!file.exists()) return null
    return runCatching {
      BitmapFactory.decodeFile(file.absolutePath)?.also { bmp ->
        bitmapCache[key] = bmp
      }
    }.getOrNull()
  }

  private fun domainToEntity(doc: NotebookDocument): NotebookEntity {
    val pagesArr = JSONArray()
    doc.pages.forEachIndexed { pageIdx, page ->
      val pageObj = JSONObject()
      pageObj.put("pageNumber", page.pageNumber)
      pageObj.put("isBookmarked", page.isBookmarked)

      if (page.pdfPageBitmap != null) {
        val bmpKey = "${doc.id}_pdf_$pageIdx"
        val path = saveBitmapToDisk(bmpKey, page.pdfPageBitmap)
        pageObj.put("pdfBitmapKey", bmpKey)
        pageObj.put("pdfBitmapPath", path)
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
          val p = JSONArray()
          p.put(pt.x.toDouble())
          p.put(pt.y.toDouble())
          ptsArr.put(p)
        }
        sObj.put("points", ptsArr)

        val ctrlArr = JSONArray()
        stroke.shapeControlPoints.forEach { pt ->
          val p = JSONArray()
          p.put(pt.x.toDouble())
          p.put(pt.y.toDouble())
          ctrlArr.put(p)
        }
        sObj.put("ctrlPoints", ctrlArr)

        val pressArr = JSONArray()
        stroke.pressures.forEach { pr -> pressArr.put(pr.toDouble()) }
        sObj.put("pressures", pressArr)
        strokesArr.put(sObj)
      }
      pageObj.put("strokes", strokesArr)

      val tapesArr = JSONArray()
      page.studyTapes.forEach { tape ->
        val tObj = JSONObject()
        tObj.put("id", tape.id)
        tObj.put("startX", tape.start.x.toDouble())
        tObj.put("startY", tape.start.y.toDouble())
        tObj.put("endX", tape.end.x.toDouble())
        tObj.put("endY", tape.end.y.toDouble())
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
        snObj.put("bgColor", sn.backgroundColor.toArgb())
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
        val imgObj = JSONObject()
        val imgKey = "img_${img.id}"
        val path = saveBitmapToDisk(imgKey, img.bitmap)
        imgObj.put("id", img.id)
        imgObj.put("x", img.position.x.toDouble())
        imgObj.put("y", img.position.y.toDouble())
        imgObj.put("widthDp", img.widthDp.toDouble())
        imgObj.put("heightDp", img.heightDp.toDouble())
        imgObj.put("rotationDegrees", img.rotationDegrees.toDouble())
        imgObj.put("imgKey", imgKey)
        imgObj.put("imgPath", path)
        imgArr.put(imgObj)
      }
      pageObj.put("images", imgArr)

      pagesArr.put(pageObj)
    }

    val packedTemplateMeta =
      listOf(
        doc.paperTemplate.name,
        doc.paperColorTheme.name,
        if (doc.isLockedVault) "1" else "0",
        doc.vaultPinHash,
      ).joinToString("|")

    return NotebookEntity(
      id = doc.id,
      title = doc.title,
      timestamp = doc.timestamp,
      updatedAtEpochMs = System.currentTimeMillis(),
      coverStyleName = doc.coverStyle.name,
      isFavorite = doc.isFavorite,
      isTrashed = doc.isTrashed,
      hasStylusBadge = doc.hasStylusBadge,
      isImportedPdf = doc.isImportedPdf,
      paperTemplateName = packedTemplateMeta,
      currentPageIndex = doc.currentPageIndex,
      pagesJson = pagesArr.toString(),
    )
  }

  private fun entityToDomain(entity: NotebookEntity): NotebookDocument {
    val pages = mutableListOf<NotePage>()
    runCatching {
      val pagesArr = JSONArray(entity.pagesJson)
      for (i in 0 until pagesArr.length()) {
        val pageObj = pagesArr.getJSONObject(i)
        val pdfKey = pageObj.optString("pdfBitmapKey", "")
        val pdfPath = pageObj.optString("pdfBitmapPath", "")
        val pdfBmp = if (pdfKey.isNotEmpty()) loadBitmapFromDisk(pdfKey, pdfPath) else null

        val strokes = mutableListOf<DrawnStroke>()
        val strokesArr = pageObj.optJSONArray("strokes") ?: JSONArray()
        for (sIdx in 0 until strokesArr.length()) {
          val sObj = strokesArr.getJSONObject(sIdx)
          val pts = mutableListOf<Offset>()
          val ptsArr = sObj.optJSONArray("points") ?: JSONArray()
          for (pIdx in 0 until ptsArr.length()) {
            val pair = ptsArr.getJSONArray(pIdx)
            pts.add(Offset(pair.getDouble(0).toFloat(), pair.getDouble(1).toFloat()))
          }
          val ctrlPts = mutableListOf<Offset>()
          val ctrlArr = sObj.optJSONArray("ctrlPoints") ?: JSONArray()
          for (cIdx in 0 until ctrlArr.length()) {
            val pair = ctrlArr.getJSONArray(cIdx)
            ctrlPts.add(Offset(pair.getDouble(0).toFloat(), pair.getDouble(1).toFloat()))
          }
          val pressures = mutableListOf<Float>()
          val pressArr = sObj.optJSONArray("pressures") ?: JSONArray()
          for (prIdx in 0 until pressArr.length()) {
            pressures.add(pressArr.getDouble(prIdx).toFloat())
          }
          val shapeKindStr = sObj.optString("shapeKind", "")
          val shapeKind =
            if (shapeKindStr.isNotEmpty()) {
              runCatching { ShapeKind.valueOf(shapeKindStr) }.getOrNull()
            } else null

          strokes.add(
            DrawnStroke(
              id = sObj.optString("id", java.util.UUID.randomUUID().toString()),
              points = pts,
              pressures = pressures,
              color = Color(sObj.optInt("color", android.graphics.Color.BLUE)),
              strokeWidth = sObj.optDouble("strokeWidth", 6.5).toFloat(),
              penStyle =
                runCatching { PenStyle.valueOf(sObj.optString("penStyle", "BALL")) }
                  .getOrDefault(PenStyle.BALL),
              strokePattern =
                runCatching { StrokePattern.valueOf(sObj.optString("strokePattern", "SOLID")) }
                  .getOrDefault(StrokePattern.SOLID),
              isHighlighter = sObj.optBoolean("isHighlighter", false),
              isPencil = sObj.optBoolean("isPencil", false),
              isShape = sObj.optBoolean("isShape", false),
              shapeKind = shapeKind,
              shapeFill = sObj.optBoolean("shapeFill", false),
              shapeControlPoints = ctrlPts,
              zIndex = sObj.optInt("zIndex", 0),
              timestampMs = sObj.optLong("timestampMs", 0L),
            )
          )
        }

        val tapes = mutableListOf<StudyTapeItem>()
        val tapesArr = pageObj.optJSONArray("studyTapes") ?: JSONArray()
        for (tIdx in 0 until tapesArr.length()) {
          val tObj = tapesArr.getJSONObject(tIdx)
          tapes.add(
            StudyTapeItem(
              id = tObj.optString("id"),
              start = Offset(tObj.optDouble("startX").toFloat(), tObj.optDouble("startY").toFloat()),
              end = Offset(tObj.optDouble("endX").toFloat(), tObj.optDouble("endY").toFloat()),
              color = Color(tObj.optInt("color")),
              thickness = tObj.optDouble("thickness", 38.0).toFloat(),
              isRevealed = tObj.optBoolean("isRevealed", false),
            )
          )
        }

        val textBoxes = mutableListOf<CanvasTextBox>()
        val textArr = pageObj.optJSONArray("textBoxes") ?: JSONArray()
        for (tbIdx in 0 until textArr.length()) {
          val tbObj = textArr.getJSONObject(tbIdx)
          textBoxes.add(
            CanvasTextBox(
              id = tbObj.optString("id"),
              position = Offset(tbObj.optDouble("x").toFloat(), tbObj.optDouble("y").toFloat()),
              text = tbObj.optString("text"),
              color = Color(tbObj.optInt("color")),
              fontSizeSp = tbObj.optDouble("fontSizeSp", 16.0).toFloat(),
              isBold = tbObj.optBoolean("isBold", false),
            )
          )
        }

        val stickyNotes = mutableListOf<StickyNoteItem>()
        val stickyArr = pageObj.optJSONArray("stickyNotes") ?: JSONArray()
        for (snIdx in 0 until stickyArr.length()) {
          val snObj = stickyArr.getJSONObject(snIdx)
          stickyNotes.add(
            StickyNoteItem(
              id = snObj.optString("id"),
              position = Offset(snObj.optDouble("x").toFloat(), snObj.optDouble("y").toFloat()),
              text = snObj.optString("text"),
              backgroundColor = Color(snObj.optInt("bgColor")),
              isExpanded = snObj.optBoolean("isExpanded", true),
            )
          )
        }

        val elements = mutableListOf<CanvasElementItem>()
        val elemArr = pageObj.optJSONArray("elements") ?: JSONArray()
        for (elIdx in 0 until elemArr.length()) {
          val elObj = elemArr.getJSONObject(elIdx)
          val stamp =
            runCatching { ElementStampType.valueOf(elObj.optString("stampType")) }
              .getOrDefault(ElementStampType.IMPORTANT_STAR)
          elements.add(
            CanvasElementItem(
              id = elObj.optString("id"),
              position = Offset(elObj.optDouble("x").toFloat(), elObj.optDouble("y").toFloat()),
              stampType = stamp,
            )
          )
        }

        val images = mutableListOf<CanvasImageItem>()
        val imgArr = pageObj.optJSONArray("images") ?: JSONArray()
        for (imIdx in 0 until imgArr.length()) {
          val imgObj = imgArr.getJSONObject(imIdx)
          val bmp = loadBitmapFromDisk(imgObj.optString("imgKey"), imgObj.optString("imgPath"))
          if (bmp != null) {
            images.add(
              CanvasImageItem(
                id = imgObj.optString("id"),
                position = Offset(imgObj.optDouble("x").toFloat(), imgObj.optDouble("y").toFloat()),
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
            pageNumber = pageObj.optInt("pageNumber", i + 1),
            isBookmarked = pageObj.optBoolean("isBookmarked", false),
            pdfPageBitmap = pdfBmp,
            strokes = strokes,
            studyTapes = tapes,
            textBoxes = textBoxes,
            stickyNotes = stickyNotes,
            elements = elements,
            images = images,
          )
        )
      }
    }

    if (pages.isEmpty()) {
      pages.add(NotePage(pageNumber = 1))
    }

    val metaParts = entity.paperTemplateName.split("|")
    val templateName = metaParts.getOrNull(0).orEmpty()
    val paperColorName = metaParts.getOrNull(1).orEmpty()
    val isLocked = metaParts.getOrNull(2) == "1"
    val pinHash = metaParts.getOrNull(3).orEmpty()

    return NotebookDocument(
      id = entity.id,
      title = entity.title,
      timestamp = entity.timestamp,
      coverStyle =
        runCatching { NotebookCoverStyle.valueOf(entity.coverStyleName) }
          .getOrDefault(NotebookCoverStyle.KRAFT_TAN),
      isFavorite = entity.isFavorite,
      isTrashed = entity.isTrashed,
      hasStylusBadge = entity.hasStylusBadge,
      isImportedPdf = entity.isImportedPdf,
      paperTemplate =
        runCatching { PaperTemplate.valueOf(templateName) }
          .getOrDefault(PaperTemplate.BLANK),
      paperColorTheme =
        runCatching { PaperColorTheme.valueOf(paperColorName) }
          .getOrDefault(PaperColorTheme.PURE_WHITE),
      isLockedVault = isLocked,
      vaultPinHash = pinHash,
      pages = pages,
      currentPageIndex = entity.currentPageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0)),
    )
  }
}
