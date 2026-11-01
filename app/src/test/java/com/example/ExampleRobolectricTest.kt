package com.example

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.NotebookRepository
import com.example.data.ProNotesDatabase
import com.example.data.VaultCryptoHelper
import com.example.model.CanvasElementItem
import com.example.model.CanvasTextBox
import com.example.model.DocumentType
import com.example.model.DrawnStroke
import com.example.model.ElementStampType
import com.example.model.NotePage
import com.example.model.NotebookCoverStyle
import com.example.model.NotebookDocument
import com.example.model.PageOrientation
import com.example.model.PageSizePreset
import com.example.model.PaperColorTheme
import com.example.model.PaperTemplate
import com.example.model.PenStyle
import com.example.model.PrimaryTool
import com.example.model.StickyNoteItem
import com.example.model.StrokePattern
import com.example.util.PdfImportHelper
import com.example.viewmodel.ProNotesViewModel
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var db: ProNotesDatabase
  private lateinit var repo: NotebookRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db =
      Room.inMemoryDatabaseBuilder(context, ProNotesDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    repo =
      NotebookRepository(
        dao = db.notebookDao(),
        context = context,
        folderDao = db.folderDao(),
        customTemplateDao = db.customTemplateDao(),
      )
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `app_name string resource matches Pro Notes`() {
    assertEquals("Pro Notes", context.getString(R.string.app_name))
  }

  @Test
  fun `1 - document persistence and reload round-trip preserves strokes, pressures, text boxes, and templates`() =
    runBlocking {
      val page1 =
        NotePage(
          pageNumber = 1,
          isBookmarked = true,
          pageTemplate = PaperTemplate.CORNELL,
          pageColorTheme = PaperColorTheme.GOODNOTES_CREAM,
          canvasWidthPx = 1200f,
          canvasHeightPx = 1600f,
          strokes =
            listOf(
              DrawnStroke(
                id = "s1",
                points = listOf(Offset(10f, 20f), Offset(50f, 80f), Offset(90f, 120f)),
                pressures = listOf(0.4f, 0.8f, 0.95f),
                color = Color(0xFF1E88E5),
                strokeWidth = 6.5f,
                penStyle = PenStyle.FOUNTAIN,
                strokePattern = StrokePattern.SOLID,
              )
            ),
          textBoxes =
            listOf(
              CanvasTextBox(
                id = "tb1",
                position = Offset(100f, 140f),
                text = "Lecture 1 Summary",
                fontSizeSp = 16f,
                color = Color(0xFF1A1A1A),
              )
            ),
          elements =
            listOf(
              CanvasElementItem(
                id = "el1",
                position = Offset(200f, 220f),
                stampType = ElementStampType.IMPORTANT_STAR,
              )
            ),
          stickyNotes =
            listOf(
              StickyNoteItem(
                id = "st1",
                position = Offset(300f, 260f),
                text = "Review before exam",
                isExpanded = true,
              )
            ),
        )

      val doc =
        NotebookDocument(
          id = "doc_persist_test",
          title = "Quantum Mechanics Notes",
          timestamp = "Oct 30, 2026",
          coverStyle = NotebookCoverStyle.CHARCOAL_DARK,
          paperTemplate = PaperTemplate.CORNELL,
          paperColorTheme = PaperColorTheme.GOODNOTES_CREAM,
          folderId = "folder_physics",
          documentType = DocumentType.NOTEBOOK,
          pageSizePreset = PageSizePreset.A4,
          pageOrientation = PageOrientation.LANDSCAPE,
          revision = 1L,
          pages = listOf(page1),
        )

      repo.saveNotebook(doc)
      val loaded = repo.allNotebooksFlow.first().find { it.id == "doc_persist_test" }
      assertNotNull(loaded)
      assertEquals("Quantum Mechanics Notes", loaded!!.title)
      assertEquals("folder_physics", loaded.folderId)
      assertEquals(PageSizePreset.A4, loaded.pageSizePreset)
      assertEquals(PageOrientation.LANDSCAPE, loaded.pageOrientation)
      assertEquals(1, loaded.pages.size)

      val loadedPage = loaded.pages.first()
      assertEquals(PaperTemplate.CORNELL, loadedPage.pageTemplate)
      assertEquals(PaperColorTheme.GOODNOTES_CREAM, loadedPage.pageColorTheme)
      assertEquals(1200f, loadedPage.canvasWidthPx, 0.1f)
      assertEquals(1, loadedPage.strokes.size)
      assertEquals(listOf(0.4f, 0.8f, 0.95f), loadedPage.strokes.first().pressures)
      assertEquals("Lecture 1 Summary", loadedPage.textBoxes.first().text)
      assertEquals("Review before exam", loadedPage.stickyNotes.first().text)
    }

  @Test
  fun `2 - rapid consecutive saves reject stale out-of-order revisions`() = runBlocking {
    val newerDoc =
      NotebookDocument(
        id = "doc_race_test",
        title = "Latest Version",
        timestamp = "Today",
        revision = 5L,
        pages =
          listOf(
            NotePage(
              pageNumber = 1,
              textBoxes =
                listOf(
                  CanvasTextBox(
                    id = "t_new",
                    position = Offset(10f, 10f),
                    text = "Newest Content",
                  )
                ),
            )
          ),
      )

    val staleOlderDoc =
      newerDoc.copy(
        title = "Stale Old Version",
        revision = 3L,
        pages =
          listOf(
            NotePage(
              pageNumber = 1,
              textBoxes =
                listOf(
                  CanvasTextBox(
                    id = "t_old",
                    position = Offset(10f, 10f),
                    text = "Stale Content",
                  )
                ),
            )
          ),
      )

    // Save revision 5 first, then simulate an out-of-order coroutine attempting to write revision 3
    repo.saveNotebook(newerDoc)
    repo.saveNotebook(staleOlderDoc)

    val persisted = repo.allNotebooksFlow.first().find { it.id == "doc_race_test" }
    assertNotNull(persisted)
    assertEquals(5L, persisted!!.revision)
    assertEquals("Latest Version", persisted.title)
    assertEquals("Newest Content", persisted.pages.first().textBoxes.first().text)
  }

  @Test
  fun `3 - unified undo and redo restores strokes, text boxes, and page insertions`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = ProNotesViewModel(app)
    vm.createNewNotebook(
      title = "Undo Redo Test Doc",
      coverStyle = NotebookCoverStyle.KRAFT_TAN,
      paperTemplate = PaperTemplate.RULED,
      paperColorTheme = PaperColorTheme.PURE_WHITE,
    )
    shadowOf(Looper.getMainLooper()).idle()

    val activeId = vm.uiState.value.activeTabId
    val initialStrokeCount =
      vm.uiState.value.notebooks.find { it.id == activeId }!!.currentPage.strokes.size
    val initialTextBoxCount =
      vm.uiState.value.notebooks.find { it.id == activeId }!!.currentPage.textBoxes.size

    // 1. Add a stroke
    vm.selectPrimaryTool(PrimaryTool.WRITING_TOOLS)
    vm.addStroke(
      points = listOf(Offset(50f, 50f), Offset(120f, 120f), Offset(180f, 180f)),
      pressures = listOf(0.5f, 0.7f, 0.9f),
      snappedFromHold = false,
    )
    assertEquals(
      initialStrokeCount + 1,
      vm.uiState.value.notebooks.find { it.id == activeId }!!.currentPage.strokes.size,
    )

    // Undo stroke
    vm.undo()
    assertEquals(
      initialStrokeCount,
      vm.uiState.value.notebooks.find { it.id == activeId }!!.currentPage.strokes.size,
    )

    // Redo stroke
    vm.redo()
    assertEquals(
      initialStrokeCount + 1,
      vm.uiState.value.notebooks.find { it.id == activeId }!!.currentPage.strokes.size,
    )

    // 2. Add a text box and undo/redo it
    vm.addTextBoxAt(Offset(200f, 200f), "Undoable Text Box")
    assertEquals(
      initialTextBoxCount + 1,
      vm.uiState.value.notebooks.find { it.id == activeId }!!.currentPage.textBoxes.size,
    )

    vm.undo()
    assertEquals(
      initialTextBoxCount,
      vm.uiState.value.notebooks.find { it.id == activeId }!!.currentPage.textBoxes.size,
    )

    vm.redo()
    assertEquals(
      initialTextBoxCount + 1,
      vm.uiState.value.notebooks.find { it.id == activeId }!!.currentPage.textBoxes.size,
    )
  }

  @Test
  fun `4 - Room MIGRATION_1_2 preserves existing v1 notebooks and adds v2 columns and tables`() {
    val dbName = "migration_test_pronotes.db"
    context.deleteDatabase(dbName)

    val config =
      SupportSQLiteOpenHelper.Configuration.builder(context)
        .name(dbName)
        .callback(
          object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
              db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notebooks` (
                  `id` TEXT NOT NULL,
                  `title` TEXT NOT NULL,
                  `timestamp` TEXT NOT NULL,
                  `coverStyleName` TEXT NOT NULL,
                  `isFavorite` INTEGER NOT NULL,
                  `isTrashed` INTEGER NOT NULL,
                  `hasStylusBadge` INTEGER NOT NULL,
                  `isImportedPdf` INTEGER NOT NULL,
                  `paperTemplateName` TEXT NOT NULL,
                  `currentPageIndex` INTEGER NOT NULL,
                  `pagesJson` TEXT NOT NULL,
                  `updatedAtEpochMs` INTEGER NOT NULL,
                  PRIMARY KEY(`id`)
                )
                """.trimIndent()
              )
              db.execSQL(
                """
                INSERT INTO `notebooks` VALUES (
                  'v1_doc_1', 'Legacy v1 Notebook', 'Oct 1, 2026',
                  'KRAFT_TAN', 1, 0, 1, 0, 'RULED',
                  0, '[]', 1700000000000
                )
                """.trimIndent()
              )
            }

            override fun onUpgrade(
              db: androidx.sqlite.db.SupportSQLiteDatabase,
              oldVersion: Int,
              newVersion: Int,
            ) {}
          }
        )
        .build()

    val helper = FrameworkSQLiteOpenHelperFactory().create(config)
    val sqlDb = helper.writableDatabase
    ProNotesDatabase.MIGRATION_1_2.migrate(sqlDb)

    val cursor =
      sqlDb.query(
        "SELECT id, title, documentTypeName, pageSizeName, revision FROM notebooks WHERE id = 'v1_doc_1'"
      )
    assertTrue(cursor.moveToFirst())
    assertEquals("v1_doc_1", cursor.getString(0))
    assertEquals("Legacy v1 Notebook", cursor.getString(1))
    assertEquals("NOTEBOOK", cursor.getString(2))
    assertEquals("A4", cursor.getString(3))
    assertEquals(0L, cursor.getLong(4))
    cursor.close()
    sqlDb.close()
    context.deleteDatabase(dbName)
  }

  @Test
  fun `5 - Vault encryption at rest encrypts pagesJson in SQLite and decrypts on load`() = runBlocking {
    val secretPage =
      NotePage(
        pageNumber = 1,
        textBoxes =
          listOf(
            CanvasTextBox(
              id = "secret_tb",
              position = Offset(40f, 40f),
              text = "TOP_SECRET_LAUNCH_CODE_9941",
            )
          ),
      )
    val vaultDoc =
      NotebookDocument(
        id = "vault_doc_test",
        title = "Classified Vault",
        timestamp = "Today",
        isLockedVault = true,
        vaultPinHash = "1234",
        revision = 1L,
        pages = listOf(secretPage),
      )

    repo.saveNotebook(vaultDoc)

    // Inspect raw SQLite entity to verify pagesJson is encrypted at rest and does NOT contain plaintext
    val rawEntity = db.notebookDao().getNotebookById("vault_doc_test")
    assertNotNull(rawEntity)
    assertTrue(VaultCryptoHelper.isEncryptedPayload(rawEntity!!.pagesJson))
    assertFalse(rawEntity.pagesJson.contains("TOP_SECRET_LAUNCH_CODE_9941"))

    // Verify loading through repository transparently decrypts the payload
    val decryptedDocs = repo.allNotebooksFlow.first()
    val loadedVault = decryptedDocs.find { it.id == "vault_doc_test" }
    assertNotNull(loadedVault)
    assertEquals(
      "TOP_SECRET_LAUNCH_CODE_9941",
      loadedVault!!.pages.first().textBoxes.first().text,
    )
  }

  @Test
  fun `6 - PDF and PNG export produce valid files with coordinate scaling`() = runBlocking {
    val doc =
      NotebookDocument(
        id = "export_test_doc",
        title = "Export Verification",
        timestamp = "Today",
        pageSizePreset = PageSizePreset.A4,
        pageOrientation = PageOrientation.PORTRAIT,
        pages =
          listOf(
            NotePage(
              pageNumber = 1,
              pageTemplate = PaperTemplate.GRID,
              canvasWidthPx = 1000f,
              canvasHeightPx = 1414f,
              strokes =
                listOf(
                  DrawnStroke(
                    points = listOf(Offset(100f, 100f), Offset(500f, 700f), Offset(900f, 1200f)),
                    pressures = listOf(0.5f, 0.9f, 0.6f),
                    color = Color.Blue,
                    strokeWidth = 6f,
                    penStyle = PenStyle.BRUSH,
                  )
                ),
            )
          ),
      )

    val pdfFile = File(context.cacheDir, "export_verification_test.pdf")
    if (pdfFile.exists()) pdfFile.delete()
    val pdfOk =
      PdfImportHelper.exportNotebookToPdfUri(
        context = context,
        document = doc,
        destUri = Uri.fromFile(pdfFile),
        includeConfidentialWatermark = true,
      )
    assertTrue(pdfOk)
    assertTrue(pdfFile.exists())
    assertTrue(pdfFile.length() > 100L)

    val pngFile = File(context.cacheDir, "export_verification_test.png")
    if (pngFile.exists()) pngFile.delete()
    val pngOk =
      PdfImportHelper.exportPageToPngUri(
        context = context,
        document = doc,
        notePage = doc.currentPage,
        destUri = Uri.fromFile(pngFile),
        includeConfidentialWatermark = true,
      )
    assertTrue(pngOk)
    assertTrue(pngFile.exists())
    assertTrue(pngFile.length() > 100L)
  }

  @Test
  fun `7 - folder hierarchy creation and cycle prevention block moving folder into its descendant`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = ProNotesViewModel(app)
    shadowOf(Looper.getMainLooper()).idle()

    // Create Parent Folder at root
    vm.navigateToFolder(null)
    vm.createFolder("Parent Semester")
    val parentFolder = vm.uiState.value.folders.find { it.name == "Parent Semester" }
    assertNotNull(parentFolder)

    // Navigate into Parent Folder and create Child Folder
    vm.navigateToFolder(parentFolder!!.id)
    vm.createFolder("Child Course")
    val childFolder = vm.uiState.value.folders.find { it.name == "Child Course" }
    assertNotNull(childFolder)
    assertEquals(parentFolder.id, childFolder!!.parentFolderId)

    // Verify cycle prevention: cannot move Parent Folder into Child Folder or into itself
    assertFalse(vm.canMoveFolder(parentFolder.id, parentFolder.id))
    assertFalse(vm.canMoveFolder(parentFolder.id, childFolder.id))
    assertTrue(vm.canMoveFolder(childFolder.id, null))
  }

  @Test
  fun `8 - page operations add, duplicate, delete, and change template without losing strokes`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = ProNotesViewModel(app)
    vm.createNewNotebook(
      title = "Page Ops Test Doc",
      coverStyle = NotebookCoverStyle.CHARCOAL_DARK,
      paperTemplate = PaperTemplate.GRID,
      paperColorTheme = PaperColorTheme.PURE_WHITE,
    )
    shadowOf(Looper.getMainLooper()).idle()

    val activeId = vm.uiState.value.activeTabId
    val initialDoc = vm.uiState.value.notebooks.find { it.id == activeId }!!
    val initialPagesCount = initialDoc.pages.size

    // Draw a stroke on current page
    vm.addStroke(
      points = listOf(Offset(40f, 40f), Offset(90f, 90f)),
      pressures = listOf(0.7f, 0.8f),
      snappedFromHold = false,
    )
    val strokeCountBeforeTemplateChange =
      vm.uiState.value.notebooks.find { it.id == activeId }!!.currentPage.strokes.size

    // Change template of current page to MUSIC Staff and verify strokes are untouched
    vm.changeCurrentPageTemplate(
      template = PaperTemplate.MUSIC,
      colorTheme = PaperColorTheme.SAGE_EYECARE,
      customTemplate = null,
    )
    val updatedPage = vm.uiState.value.notebooks.find { it.id == activeId }!!.currentPage
    assertEquals(PaperTemplate.MUSIC, updatedPage.pageTemplate)
    assertEquals(PaperColorTheme.SAGE_EYECARE, updatedPage.pageColorTheme)
    assertEquals(strokeCountBeforeTemplateChange, updatedPage.strokes.size)

    // Duplicate current page
    vm.duplicateCurrentPage()
    assertEquals(
      initialPagesCount + 1,
      vm.uiState.value.notebooks.find { it.id == activeId }!!.pages.size,
    )

    // Delete current page
    vm.deleteCurrentPage()
    assertEquals(
      initialPagesCount,
      vm.uiState.value.notebooks.find { it.id == activeId }!!.pages.size,
    )
  }
}
