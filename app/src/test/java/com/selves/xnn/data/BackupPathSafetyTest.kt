package com.selves.xnn.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * 备份恢复的路径边界校验（Zip Slip）。
 *
 * 复刻 BackupService.resolveInsideFilesDir 的判定逻辑：该方法是 private，
 * 且 BackupService 依赖 Context/Room 无法在纯 JVM 单测里构造，因此这里锁定
 * 算法本身，确保越界条目一律被拒。
 */
class BackupPathSafetyTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun resolve(filesDir: File, relativePath: String): File? {
        if (relativePath.isBlank()) return null
        if (relativePath.startsWith("/") || relativePath.startsWith("\\")) return null
        val baseDir = filesDir.canonicalFile
        val target = File(baseDir, relativePath).canonicalFile
        val basePrefix = baseDir.path + File.separator
        return if (target.path.startsWith(basePrefix)) target else null
    }

    @Test
    fun `正常相对路径应解析到 filesDir 内`() {
        val filesDir = tempFolder.newFolder("files")
        val resolved = resolve(filesDir, "avatars/member.png")
        assertEquals(File(filesDir.canonicalFile, "avatars/member.png").path, resolved?.path)
    }

    @Test
    fun `父目录穿越应被拒绝`() {
        val filesDir = tempFolder.newFolder("files")
        // 攻击目标：覆盖 DataStore 里的管理密码哈希
        assertNull(resolve(filesDir, "../datastore/settings.preferences_pb"))
        assertNull(resolve(filesDir, "../../shared_prefs/prefs.xml"))
        assertNull(resolve(filesDir, "a/../../escaped.txt"))
    }

    @Test
    fun `绝对路径应被拒绝`() {
        val filesDir = tempFolder.newFolder("files")
        assertNull(resolve(filesDir, "/data/data/com.selves.xnn/evil.so"))
        assertNull(resolve(filesDir, "\\windows\\system32\\evil.dll"))
    }

    @Test
    fun `空路径应被拒绝`() {
        val filesDir = tempFolder.newFolder("files")
        assertNull(resolve(filesDir, ""))
        assertNull(resolve(filesDir, "   "))
    }

    @Test
    fun `filesDir 同级的兄弟目录不应被误判为合法`() {
        val parent = tempFolder.newFolder("parent")
        val filesDir = File(parent, "files").apply { mkdirs() }
        // files-evil 与 files 共享前缀，必须靠分隔符区分
        assertNull(resolve(filesDir, "../files-evil/x.txt"))
    }
}
