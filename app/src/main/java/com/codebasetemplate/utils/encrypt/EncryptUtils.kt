package com.codebasetemplate.utils.encrypt

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.codebasetemplate.utils.load.LoadImageDataUtils
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object EncryptUtils {

    private const val VAULT_FOLDER_NAME = "Galleryit"

    private fun getVaultDir(): File {
        val root = Environment.getExternalStorageDirectory()
        val dir = File(root, VAULT_FOLDER_NAME)
        if (!dir.exists()) dir.mkdirs()

        File(dir, ".nomedia").apply {
            if (!exists()) createNewFile()
        }

        return dir
    }

    fun encryptToVault(
        context: Context,
        inputPath: String,
        key: SecretKey
    ): File {
        val input = File(inputPath)
        require(input.exists()) { "Input file not found: $inputPath" }

        val mediaInfo = LoadImageDataUtils.getNameAndMime(context, inputPath)

        val vaultDir = getVaultDir()
        val vaultFile = File(
            vaultDir,
            UUID.randomUUID().toString().replace("-", "")
        )

        val metaBytes = JSONObject().apply {
            put("name", mediaInfo.name)
            put("mime", mediaInfo.mime)
        }.toString().toByteArray()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))

        FileOutputStream(vaultFile).use { fos ->
            fos.write(iv)
            fos.write(ByteBuffer.allocate(4).putInt(metaBytes.size).array())

            CipherOutputStream(fos, cipher).use { cos ->
                cos.write(metaBytes)
                FileInputStream(input).use { it.copyTo(cos) }
            }
        }

        input.delete()
        return vaultFile
    }

    fun restoreFromVault(
        context: Context,
        vaultFile: File,
        key: SecretKey
    ): File {
        val restoreDir = File(getVaultDir(), "restore")
        if (!restoreDir.exists()) restoreDir.mkdirs()

        FileInputStream(vaultFile).use { fis ->
            val iv = ByteArray(12)
            fis.read(iv)

            val metaSizeBuf = ByteArray(4)
            fis.read(metaSizeBuf)
            val metaSize = ByteBuffer.wrap(metaSizeBuf).int

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))

            CipherInputStream(fis, cipher).use { cis ->
                val metaBytes = ByteArray(metaSize)
                var read = 0
                while (read < metaSize) {
                    val r = cis.read(metaBytes, read, metaSize - read)
                    if (r == -1) break
                    read += r
                }

                val meta = JSONObject(String(metaBytes))
                val name = meta.getString("name")
                val mime = meta.getString("mime")

                val outFile = resolveDuplicate(restoreDir, name)

                FileOutputStream(outFile).use { cis.copyTo(it) }

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(outFile.absolutePath),
                    arrayOf(mime),
                    null
                )

                vaultFile.delete()
                return outFile
            }
        }
    }

    private fun resolveDuplicate(dir: File, name: String): File {
        var file = File(dir, name)
        if (!file.exists()) return file

        val base = name.substringBeforeLast(".")
        val ext = name.substringAfterLast(".", "")
        var index = 1

        while (file.exists()) {
            val newName = if (ext.isEmpty()) {
                "$base ($index)"
            } else {
                "$base ($index).$ext"
            }
            file = File(dir, newName)
            index++
        }
        return file
    }
}